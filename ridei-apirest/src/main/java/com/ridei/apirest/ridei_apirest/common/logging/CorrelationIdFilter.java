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
 * CorrelationIdFilter
 *
 * <p>
 *     Assigns a unique correlation ID (UUID) to every incoming HTTP request.
 *     The correlation ID is stored in the MDC (Mapped Diagnostic Context),
 *     making it automatically available in all Log entries during that request's lifecycle.
 * </p>
 *
 * <p>
 *     Also adds the correlation ID to the HTTP response headers as "X-Correlation-Id".
 *     This allows clients and developers to trace and correlate logs and API responses.
 * </p>
 */
@Slf4j
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String MDC_CORRELATION_ID_KEY = "correlationId";

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
