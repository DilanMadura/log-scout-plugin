package com.dmjtech.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : Dilan Jayaneththi
 * @mailto : ddmdilan@mail.com
 * @created : 6/14/2025, Saturday, 11:32 AM,
 * @project : log-tracer-plugin
 * @package : com.dmjtech.application.config
 **/
@Configuration
public class LogScoutAutoConfiguration {
    @ConditionalOnMissingBean
    public CorrelationIdAndHeaderFilter correlationIdFilter() {
        return new CorrelationIdAndHeaderFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public LogScout logScout() {
        return new LogScout();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
