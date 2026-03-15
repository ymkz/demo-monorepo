package dev.ymkz.demo.api.infrastructure.logging;

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

class WideEventLoggingFilterTest {

    private WideEventLoggingFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new WideEventLoggingFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/books");
        when(response.getStatus()).thenReturn(200);
    }

    @AfterEach
    void tearDown() {
        EventsCollector.clear();
    }

    @Test
    void doFilterInternal_正常終了時にEventsCollectorのThreadLocalがクリアされること() throws Exception {
        // given
        doAnswer(invocation -> {
                    assertThat(EventsCollector.getRequestId()).isNotEmpty();
                    return null;
                })
                .when(chain)
                .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        // when
        filter.doFilterInternal(request, response, chain);

        // then
        assertThat(EventsCollector.getRequestId()).isEmpty();
    }

    @Test
    void doFilterInternal_チェーン内で例外が発生してもEventsCollectorのThreadLocalがクリアされること() throws Exception {
        // given
        doThrow(new RuntimeException("Test exception"))
                .when(chain)
                .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        // when & then
        assertThatThrownBy(() -> filter.doFilterInternal(request, response, chain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test exception");

        assertThat(EventsCollector.getRequestId()).isEmpty();
    }

    @Test
    void doFilterInternal_シリアライズ例外が発生してもEventsCollectorのThreadLocalがクリアされること() throws Exception {
        // given
        when(response.getStatus()).thenThrow(new RuntimeException("Serialization error"));

        doAnswer(invocation -> null)
                .when(chain)
                .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        // when & then
        assertThatThrownBy(() -> filter.doFilterInternal(request, response, chain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Serialization error");

        assertThat(EventsCollector.getRequestId()).isEmpty();
    }

    @Test
    void doFilterInternal_複数回のリクエストでThreadLocalがリークしないこと() throws Exception {
        // given
        doAnswer(invocation -> {
                    String requestId = EventsCollector.getRequestId();
                    assertThat(requestId).isNotEmpty();
                    return null;
                })
                .when(chain)
                .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        for (int i = 0; i < 3; i++) {
            filter.doFilterInternal(request, response, chain);

            assertThat(EventsCollector.getRequestId())
                    .withFailMessage("リクエスト %d 回目の後にThreadLocalがクリアされていません", i + 1)
                    .isEmpty();
        }
    }
}
