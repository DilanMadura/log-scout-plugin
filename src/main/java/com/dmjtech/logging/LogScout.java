package com.dmjtech.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.dmjtech.logging.CorrelationIdAndHeaderFilter.TRACE_ID;

/**
 * @author : Dilan Jayaneththi
 * @mailto : ddmdilan@mail.com
 * @created : 6/14/2025, Saturday, 11:38 AM,
 * @project : log-tracer-plugin
 * @package : com.dmjtech.logging
 **/
@Component
public class LogScout {
    @Autowired
    private ObjectMapper objectMapper;

    public enum Operation {INB_REQ, INB_RES, OUT_REQ, OUT_RES, INTERNAL}

    public enum Protocol {REST, SOAP}

    public enum HttpClient {GET, POST, PATCH, UPDATE, DELETE}

    enum Status {SUCCESS, FAILED}

    private static final Logger logger = LoggerFactory.getLogger(LogScout.class);

    public void initializeTrace(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put(TRACE_ID, traceId);
    }

    public void clearTrace() {
        MDC.clear();
    }

    public void ensureTraceInitialized(HttpServletRequest request) {
        if (MDC.get(CorrelationIdAndHeaderFilter.TRACE_ID) == null) {
            this.initializeTrace(request);
        }
    }

    public void trace(String file, String methodName, Operation operation, Object payload) {
        this.printLog(file, methodName, operation, null, null, null, payload, 0, null);
    }

    public void trace(String file, String methodName, Operation operation, String message) {
        this.printLog(file, methodName, operation, null, null, null, null, 0, message);
    }

    public void trace(String file, String methodName, Operation operation, Object payload, String message) {
        this.printLog(file, methodName, operation, null, null, null, payload, 0, message);
    }

    public void trace(String file, String methodName, Operation operation, Protocol protocol, HttpClient httpClient, Object payload) {
        this.printLog(file, methodName, operation, protocol, httpClient, null, payload, 0, null);
    }

    public void trace(String file, String methodName, Operation operation, Protocol protocol, HttpClient httpClient, String requestURI, Object payload) {
        this.printLog(file, methodName, operation, protocol, httpClient, requestURI, payload, 0, null);
    }

    public void trace(String file, String methodName, Operation operation, Protocol protocol, Object payload, int statusCode) {
        this.printLog(file, methodName, operation, protocol, null, null, payload, statusCode, null);
    }

    public void trace(String file, String methodName, Operation operation, Protocol protocol, Object payload, int statusCode, String message) {
        this.printLog(file, methodName, operation, protocol, null, null, payload, statusCode, message);
    }


    private void printLog(String file, String methodName, Operation operation, Protocol protocol, HttpClient httpClient, String requestURI, Object payload, int statusCode, String message) {
        if (operation.equals(Operation.INB_REQ) || operation.equals(Operation.OUT_REQ)) {
            logger.info("TRACE_ID: {}, FILE: {}, METHOD: {}, OPERATION: {}, PROTOCOL: {}, HTTP_CLIENT: {}, URL: {}, HEADERS: {}, REQUEST: {}",
                    MDC.get(CorrelationIdAndHeaderFilter.TRACE_ID),
                    file,
                    methodName,
                    operation,
                    protocol,
                    httpClient,
                    requestURI,
                    MDC.get("HTTP_HEADERS"),
                    toJson(payload));
        }

        if (operation.equals(Operation.INB_RES) || operation.equals(Operation.OUT_RES)) {
            if (this.isSuccess(statusCode)) {
                logger.info("TRACE_ID: {}, FILE: {}, METHOD: {}, OPERATION: {}, PROTOCOL: {}, HEADERS: {}, RESPONSE: {}, STATUS_CODE: {}, STATUS: {}",
                        MDC.get(CorrelationIdAndHeaderFilter.TRACE_ID),
                        file,
                        methodName,
                        operation,
                        protocol,
                        MDC.get("HTTP_HEADERS"),
                        toJson(payload),
                        statusCode,
                        Status.SUCCESS
                );
            } else {
                logger.error("TRACE_ID: {}, FILE: {}, METHOD: {}, OPERATION: {}, PROTOCOL: {}, HEADERS: {}, RESPONSE: {}, STATUS_CODE: {}, STATUS: {}, ERROR_MESSAGE: {}",
                        MDC.get(CorrelationIdAndHeaderFilter.TRACE_ID),
                        file,
                        methodName,
                        operation,
                        protocol,
                        MDC.get("HTTP_HEADERS"),
                        toJson(payload),
                        statusCode,
                        Status.FAILED,
                        message
                );
            }
        }

        if (operation.equals(Operation.INTERNAL)) {
            logger.info("TRACE_ID: {}, FILE: {}, METHOD: {}, OPERATION: {}, PAYLOAD: {}, MESSAGE: {}",
                    MDC.get(CorrelationIdAndHeaderFilter.TRACE_ID),
                    file,
                    methodName,
                    operation,
                    toJson(payload),
                    message);
        }
    }

    private boolean isSuccess(int statusCode) {
        return statusCode < 300;
    }

    private String toJson(Object object) {
        if (object != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                return "Could not serialize payload";
            }
        } else {
            return null;
        }
    }
}