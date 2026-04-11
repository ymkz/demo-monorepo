package dev.ymkz.demo.api.config.mybatis;

import lombok.RequiredArgsConstructor;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class MyBatisConfig {

    private final MyBatisMetricsInterceptor metricsInterceptor;

    @Bean
    ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return configuration -> configuration.addInterceptor(metricsInterceptor);
    }
}
