package com.fugitivalamadrid.api.userapi.ratelimit;

import lombok.Getter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple rate limiter using AtomicInteger and synchronized to simulate production constraints.
 * Uses a sliding window approach with atomic operations for thread safety.
 */
@Getter
public class RateLimiter {
    private final int maxRequests;
    private final long windowSizeMillis;
    private final AtomicInteger requestCount;
    private final AtomicLong windowStart;
    private final Object lock = new Object();

    /**
     * Creates a new rate limiter.
     *
     * @param maxRequests maximum number of requests allowed in the time window
     * @param windowSizeMillis size of the time window in milliseconds
     */
    public RateLimiter(int maxRequests, long windowSizeMillis) {
        if (maxRequests <= 0 || windowSizeMillis <= 0) {
            throw new IllegalArgumentException("maxRequests and windowSizeMillis must be positive");
        }
        this.maxRequests = maxRequests;
        this.windowSizeMillis = windowSizeMillis;
        this.requestCount = new AtomicInteger(0);
        this.windowStart = new AtomicLong(System.currentTimeMillis());
    }

    /**
     * Attempts to acquire a permit for a request.
     *
     * @return true if the request is allowed, false if rate limit is exceeded
     */
    public boolean tryAcquire() {
        long currentTime = System.currentTimeMillis();
        long currentWindowStart = windowStart.get();

        // Check if we need to reset the window
        if (currentTime - currentWindowStart >= windowSizeMillis) {
            synchronized (lock) {
                // Double-check inside synchronized block to avoid race conditions
                if (currentTime - windowStart.get() >= windowSizeMillis) {
                    requestCount.set(0);
                    windowStart.set(currentTime);
                }
            }
        }

        // Try to increment request count atomically
        int currentCount;
        int newCount;
        do {
            currentCount = requestCount.get();
            if (currentCount >= maxRequests) {
                return false; // Rate limit exceeded
            }
            newCount = currentCount + 1;
        } while (!requestCount.compareAndSet(currentCount, newCount));

        return true;
    }

    /**
     * Gets the current request count in the active window.
     *
     * @return current request count
     */
    public int getCurrentRequestCount() {
        return requestCount.get();
    }

    /**
     * Gets the remaining requests allowed in the current window.
     *
     * @return remaining requests
     */
    public int getRemainingRequests() {
        return Math.max(0, maxRequests - requestCount.get());
    }

    /**
     * Gets the time until the window resets in milliseconds.
     *
     * @return time until reset in milliseconds
     */
    public long getTimeUntilReset() {
        long currentTime = System.currentTimeMillis();
        long windowStartTime = windowStart.get();
        long windowEnd = windowStartTime + windowSizeMillis;
        return Math.max(0, windowEnd - currentTime);
    }
}
