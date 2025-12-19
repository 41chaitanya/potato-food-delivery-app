package com.fooddelivery.notification.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TracingConfig
 * 
 * Configures distributed tracing using Micrometer.
 * 
 * WHY TRACING IS IMPORTANT:
 * 
 * 1. END-TO-END VISIBILITY:
 *    - Track a notification from event creation to delivery
 *    - Correlate logs across Order → Kafka → Notification services
 * 
 * 2. DEBUGGING:
 *    - When a notification fails, trace back to the source event
 *    - Identify latency bottlenecks
 * 
 * 3. MONITORING:
 *    - Measure notification processing time
 *    - Alert on high latency or failures
 * 
 * TRACE PROPAGATION:
 * - Producer services include traceId in Kafka message
 * - We extract and use it for correlation
 * - All logs include traceId for easy searching
 */
@Configuration
public class TracingConfig {

    /**
     * ObservedAspect
     * 
     * Enables @Observed annotation for automatic span creation.
     * Methods annotated with @Observed will be traced automatically.
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}
