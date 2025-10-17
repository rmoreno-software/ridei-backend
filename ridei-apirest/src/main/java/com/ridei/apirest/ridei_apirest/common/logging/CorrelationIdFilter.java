package com.ridei.apirest.ridei_apirest.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that generates and manages a correlation ID for each HTTP request.
 *
 * <p>This filter ensures that every incoming request has a unique identifier that can
 * be used for tracing logs across different services and components. The correlation
 * ID is propagated in the HTTP response headers and stored in the {@link MDC} for logging.</p>
 *
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *     <li>Check if the incoming request already contains a correlation ID header
 *         (<code>X-Correlation-Id</code>).</li>
 *     <li>If missing, generate a new unique correlation ID using {@link UUID}.</li>
 *     <li>Add the correlation ID to the {@link MDC} so all log entries for this request
 *         include it.</li>
 *     <li>Add the correlation ID header to the HTTP response.</li>
 *     <li>Clear the MDC after request processing to prevent memory leaks in asynchronous contexts.</li>
 *     <li>Log the start of each request with method, URI, and correlation ID.</li>
 * </ul>
 *
 * <p>This filter extends {@link OncePerRequestFilter}, ensuring that it is executed
 * only once per request, even in the presence of request dispatchers and forwards.</p>
 *
 * <p><b>Example HTTP Flow:</b></p>
 * <pre>
 * Incoming request headers:
 * X-Correlation-Id: (optional)
 *
 * Filter behavior:
 * - Read or generate correlation ID
 * - Add it to MDC
 * - Log request start
 * - Continue filter chain
 * - Add X-Correlation-Id to response headers
 * - Clear MDC
 *
 * Response headers:
 * X-Correlation-Id: &lt;correlation-id&gt;
 * </pre>
 *
 * <p>This pattern is commonly used in distributed systems to trace requests across
 * multiple microservices.</p>
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
@Slf4j
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    /**
     * HTTP header used for passing the correlation ID between services and clients.
     */
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    /**
     * MDC key under which the correlation ID is stored for logging.
     */
    private static final String MDC_CORRELATION_ID_KEY = "correlationId";

    /**
     * Filters each incoming HTTP request, ensuring a correlation ID is present and logged.
     *
     * @param request     the incoming {@link HttpServletRequest}
     * @param response    the outgoing {@link HttpServletResponse}
     * @param filterChain the {@link FilterChain} to pass the request/response to
     * @throws ServletException if an internal error occurs in the servlet
     * @throws IOException      if an I/O error occurs during request processing
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
        throws ServletException, IOException {

        try {
            // Check if the request already contains a correlation ID header
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);

            // Generate a new one if missing
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            // Store in MDC (used by the logging framework)
            MDC.put(MDC_CORRELATION_ID_KEY, correlationId);

            // Add the correlation ID to the response
            response.addHeader(CORRELATION_ID_HEADER, correlationId);

            // Log the start of the request with the correlation ID
            log.info("Incoming request [{}] {} {}", correlationId, request.getMethod(), request.getRequestURI());

            // Continue with the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Always clear MDC to prevent memory leaks in async contexts
            MDC.remove(MDC_CORRELATION_ID_KEY);
        }
    }
}
