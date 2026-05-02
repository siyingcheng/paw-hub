package com.pawhub.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

@Component
@ConditionalOnProperty(name = "debug.log-requests", havingValue = "true")
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final int MAX_BODY_LENGTH = 4096;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        boolean isMultipart = request.getContentType() != null
            && request.getContentType().toLowerCase().contains("multipart");

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();
        chain.doFilter(wrappedRequest, wrappedResponse);
        long elapsed = System.currentTimeMillis() - start;

        logRequest(wrappedRequest, isMultipart);
        logResponse(wrappedResponse, elapsed);

        wrappedResponse.copyBodyToResponse();
    }

    private void logRequest(ContentCachingRequestWrapper request, boolean isMultipart) {
        StringBuilder sb = new StringBuilder("\n>>> REQUEST >>>\n");
        sb.append(request.getMethod()).append(" ").append(request.getRequestURI());
        String qs = request.getQueryString();
        if (qs != null) sb.append("?").append(qs);
        sb.append("\n");

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            sb.append("  ").append(name).append(": ").append(request.getHeader(name)).append("\n");
        }

        if (!isMultipart) {
            byte[] body = request.getContentAsByteArray();
            if (body.length > 0) {
                String bodyStr = new String(body, StandardCharsets.UTF_8);
                if (bodyStr.length() > MAX_BODY_LENGTH) {
                    bodyStr = bodyStr.substring(0, MAX_BODY_LENGTH) + "...(truncated)";
                }
                sb.append("\n").append(bodyStr).append("\n");
            }
        } else {
            sb.append("\n  [multipart body omitted]\n");
        }

        sb.append("<<< REQUEST <<<");
        log.info(sb.toString());
    }

    private void logResponse(ContentCachingResponseWrapper response, long elapsedMs) {
        StringBuilder sb = new StringBuilder("\n<<< RESPONSE <<<\n");
        sb.append("Status: ").append(response.getStatus()).append(" (").append(elapsedMs).append("ms)\n");

        Collection<String> headerNames = response.getHeaderNames();
        for (String name : headerNames) {
            sb.append("  ").append(name).append(": ");
            sb.append(String.join(", ", response.getHeaders(name)));
            sb.append("\n");
        }

        byte[] body = response.getContentAsByteArray();
        if (body.length > 0) {
            String bodyStr = new String(body, StandardCharsets.UTF_8);
            if (bodyStr.length() > MAX_BODY_LENGTH) {
                bodyStr = bodyStr.substring(0, MAX_BODY_LENGTH) + "...(truncated)";
            }
            sb.append("\n").append(bodyStr).append("\n");
        }

        sb.append(">>> RESPONSE >>>");
        log.info(sb.toString());
    }
}
