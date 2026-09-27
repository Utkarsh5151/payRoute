package com.payroute.platform.resilience.ratelimit;

import com.payroute.platform.common.api.ErrorResponse;
import com.payroute.platform.common.util.JsonUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RedisRateLimiterFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimiterFilter.class);
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    private final StringRedisTemplate redisTemplate;
    // In-memory fallback if Redis is unavailable
    private final ConcurrentHashMap<String, RateLimitCounter> fallbackMap = new ConcurrentHashMap<>();

    @Autowired
    public RedisRateLimiterFilter(@Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientKey = resolveClientKey(request);
        boolean allowed = isAllowed(clientKey);

        if (!allowed) {
            log.warn("Rate limit exceeded for client: {}", clientKey);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorResponse err = new ErrorResponse(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "RATE_LIMIT_EXCEEDED",
                    "Rate limit of " + MAX_REQUESTS_PER_MINUTE + " requests per minute exceeded.",
                    request.getRequestURI()
            );

            response.getWriter().write(JsonUtils.toJson(err));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(String key) {
        if (redisTemplate != null) {
            try {
                String redisKey = "rate_limit:" + key;
                Long count = redisTemplate.opsForValue().increment(redisKey);
                if (count != null && count == 1) {
                    redisTemplate.expire(redisKey, WINDOW_DURATION);
                }
                return count != null && count <= MAX_REQUESTS_PER_MINUTE;
            } catch (Exception ex) {
                log.debug("Redis rate limit check failed, falling back to in-memory: {}", ex.getMessage());
            }
        }

        // In-memory fallback
        long now = System.currentTimeMillis();
        RateLimitCounter counter = fallbackMap.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart > 60_000) {
                return new RateLimitCounter(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        return counter.count.get() <= MAX_REQUESTS_PER_MINUTE;
    }

    private String resolveClientKey(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return "user:" + authHeader.hashCode();
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return "ip:" + xForwardedFor.split(",")[0].trim();
        }

        return "ip:" + request.getRemoteAddr();
    }

    private static class RateLimitCounter {
        final long windowStart;
        final AtomicInteger count;

        RateLimitCounter(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
