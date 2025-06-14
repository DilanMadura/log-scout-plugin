package com.dmjtech.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * CorrelationIdAndHeaderFilter captures and stores a TRACE_ID and all HTTP headers into MDC,
 * making them available for logging purposes.
 * This filter ensures consistent traceability across logs within a request lifecycle.
 *
 * @author Dilan
 */
@Component
@Order(1) // Ensures this filter runs early in the filter chain
public class CorrelationIdAndHeaderFilter implements Filter {

    public static final String TRACE_ID = "TRACE_ID";
    public static final String HTTP_HEADERS = "HTTP_HEADERS";
    private static final String TRACE_ID_HEADER_NAME = "X-Trace-Id";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        boolean traceIdSet = false;

        try {
            String traceId = MDC.get(TRACE_ID);
            System.out.println("Current TRACE_ID: " + traceId);
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString();
            }

            MDC.put(TRACE_ID, traceId);
            traceIdSet = true;

            if (request instanceof HttpServletRequest httpRequest) {
                MDC.put(HTTP_HEADERS, toJson(extractHeaders(httpRequest)));
            }

            chain.doFilter(request, response);

        } finally {
            if (traceIdSet) {
                MDC.remove(TRACE_ID);
            }
            MDC.remove(HTTP_HEADERS);
        }
    }

    private Map<String, List<String>> extractHeaders(HttpServletRequest request) {
        Map<String, List<String>> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames != null && headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            List<String> values = Collections.list(request.getHeaders(name));
            headers.put(name, values);
        }

        return headers;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
