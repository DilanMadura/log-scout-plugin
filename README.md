# log-scout-plugin
A lightweight, plug-and-play Spring Boot logging utility for tracing and debugging HTTP requests and responses with consistent TRACE_ID propagation across services.

🔗 Trace ID Management – Automatically generates or reuses TRACE_ID from incoming headers using SLF4J MDC.

📦 Request & Response Logging – Logs inbound and outbound HTTP requests and responses with headers and payloads.

🚨 Centralized Error Tracing – Captures and logs all exceptions consistently, including status codes and messages.

📚 Customizable Enums – Support for REST/SOAP, request types, HTTP methods, and operation phases (INB_REQ, OUT_RES, etc.).

🔧 Easy Integration – Just add the library and call logScout.trace(...) in your controllers or services.

🧼 Safe MDC Cleanup – Ensures TRACE_ID context is cleared correctly after each request or error.


@Autowired
private LogScout logScout;

@GetMapping("/status")
public ResponseEntity<SuccessResponseDto> checkStatus(HttpServletRequest request) {
    logScout.initializeTrace(request);
    try {
        logScout.trace(...); // Log request
        // process
        logScout.trace(...); // Log response
        return ResponseEntity.ok(...);
    } finally {
        logScout.clearTrace();
    }
}
