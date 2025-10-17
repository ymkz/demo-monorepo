package dev.ymkz.demo.api.infrastructure.datasource;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.stereotype.Component;

@Intercepts({
    @Signature(
            type = Executor.class,
            method = "update",
            args = {MappedStatement.class, Object.class}),
    @Signature(
            type = Executor.class,
            method = "query",
            args = {
                MappedStatement.class,
                Object.class,
                org.apache.ibatis.session.RowBounds.class,
                org.apache.ibatis.session.ResultHandler.class
            })
})
@Component
@RequiredArgsConstructor
public class MyBatisMetricsInterceptor implements Interceptor {

    private final MeterRegistry registry;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        String statementId = ms.getId();

        Timer.Sample sample = Timer.start(registry);
        try {
            Object result = invocation.proceed();
            sample.stop(Timer.builder("mybatis.query.time")
                    .tag("statement", statementId)
                    .tag("type", ms.getSqlCommandType().name())
                    .register(registry));
            registry.counter("mybatis.query.count", "statement", statementId).increment();
            return result;
        } catch (Throwable e) {
            registry.counter("mybatis.query.errors", "statement", statementId).increment();
            throw e;
        }
    }
}
