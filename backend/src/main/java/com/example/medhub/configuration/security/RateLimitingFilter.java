package com.example.medhub.configuration.security;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.core.env.Environment;
import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ProxyManager<byte[]> proxyManager;
    private final BucketConfiguration bucketConfiguration;
    private final Environment environment;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip rate limiting in test environment
        if (Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        
        // Only rate limit login and signup attempts
        if (path.endsWith("/signin") || path.endsWith("/signup")) {
            String clientIp = getClientIp(request);
            String keyString = path + ":" + clientIp;
            byte[] key = keyString.getBytes();
            
            ConsumptionProbe probe = proxyManager.builder().build(key, bucketConfiguration).tryConsumeAndReturnRemaining(1);
            
            if (!probe.isConsumed()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many attempts. Please try again in " + probe.getNanosToWaitForRefill() / 1_000_000_000 + " seconds.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
