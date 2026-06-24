package dev.ymkz.demo.api.shared.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;

class WideEventLoggingFilterTest {

    private WideEventLoggingFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private EventLogContext eventLog;

    @BeforeEach
    void setUp() {
        eventLog = new EventLogContext();
        ObjectProvider<EventLogContext> eventLogProvider = mock();
        when(eventLogProvider.getIfAvailable()).thenReturn(eventLog);

        filter = new WideEventLoggingFilter(eventLogProvider);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/books");
        when(response.getStatus()).thenReturn(200);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void doFilterInternal_正常終了時にMDCのhttp_request_idがクリアされること() throws Exception {
        // given
        doAnswer(invocation -> {
                    assertThat(MDC.get("http.request.id")).isNotEmpty();
                    eventLog.set("book.search.total_results", 1);
                    eventLog.addEvent("book_search_executed", null);
                    return null;
                })
                .when(chain)
                .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        // when
        filter.doFilterInternal(request, response, chain);

        // then
        assertThat(MDC.get("http.request.id")).isNull();
        assertThat(eventLog.snapshot().fields()).containsEntry("book.search.total_results", 1);
        assertThat(eventLog.snapshot().events()).hasSize(1);
    }

    @Test
    void doFilterInternal_チェーン内で例外が発生してもMDCのhttp_request_idがクリアされること() throws Exception {
        // given
        doThrow(new RuntimeException("Test exception"))
                .when(chain)
                .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        // when & then
        assertThatThrownBy(() -> filter.doFilterInternal(request, response, chain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test exception");

        assertThat(MDC.get("http.request.id")).isNull();
        assertThat(eventLog.snapshot().error()).isNotNull();
    }

    @Test
    void doFilterInternal_ログ出力準備中に例外が発生してもMDCのhttp_request_idがクリアされること() throws Exception {
        // given
        when(response.getStatus()).thenThrow(new RuntimeException("Serialization error"));

        doAnswer(invocation -> null)
                .when(chain)
                .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        // when & then
        assertThatThrownBy(() -> filter.doFilterInternal(request, response, chain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Serialization error");

        assertThat(MDC.get("http.request.id")).isNull();
    }

    @Test
    void snapshot_内部のMapとListを直接公開しないこと() {
        // given
        eventLog.set("book.search.total_results", 1);
        eventLog.addEvent("book_search_executed", null);

        // when
        var snapshot = eventLog.snapshot();

        // then
        assertThatThrownBy(() -> snapshot.fields().put("another", 2)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> snapshot.events().add(new WideEventLog.Event("now", "another", null)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
