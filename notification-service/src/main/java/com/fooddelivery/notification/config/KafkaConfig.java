package com.fooddelivery.notification.config;

import com.fooddelivery.notification.dto.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * KafkaConfig
 * 
 * Configures Kafka consumer for notification events.
 * 
 * KEY DESIGN DECISIONS:
 * 
 * 1. MANUAL ACK MODE:
 *    - We acknowledge messages only after successful processing
 *    - Prevents message loss if processing fails
 *    - Enables proper retry handling
 * 
 * 2. ERROR HANDLING DESERIALIZER:
 *    - Wraps JsonDeserializer to handle malformed messages
 *    - Bad messages don't crash the consumer
 *    - Logs error and continues processing
 * 
 * 3. RETRY WITH BACKOFF:
 *    - Failed messages are retried 3 times
 *    - 1 second delay between retries
 *    - After max retries, message is logged and skipped
 * 
 * 4. CONCURRENCY:
 *    - 3 concurrent consumers for parallel processing
 *    - Matches partition count for optimal throughput
 */
@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${notification.kafka.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${notification.kafka.retry.backoff-ms:1000}")
    private long retryBackoffMs;

    /**
     * Consumer Factory
     * 
     * Creates Kafka consumers with proper deserialization and error handling.
     * Uses ErrorHandlingDeserializer to gracefully handle malformed messages.
     */
    @Bean
    public ConsumerFactory<String, NotificationEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        
        // Kafka broker connection
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        
        // Offset management
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        // Performance tuning
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        
        // Deserializer configuration
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        
        // JSON deserializer settings
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NotificationEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Kafka Listener Container Factory
     * 
     * Configures how Kafka listeners process messages:
     * - Manual acknowledgment for reliability
     * - Concurrent consumers for throughput
     * - Error handler with retry logic
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // Manual acknowledgment - we control when offset is committed
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        
        // 3 concurrent consumers for parallel processing
        factory.setConcurrency(3);
        
        // Error handler with retry
        factory.setCommonErrorHandler(errorHandler());
        
        return factory;
    }

    /**
     * Error Handler
     * 
     * Handles exceptions during message processing:
     * - Retries failed messages with backoff
     * - After max retries, logs error and moves on
     * - Prevents poison pills from blocking the queue
     */
    @Bean
    public DefaultErrorHandler errorHandler() {
        // Fixed backoff: retry maxRetryAttempts times with retryBackoffMs delay
        FixedBackOff backOff = new FixedBackOff(retryBackoffMs, maxRetryAttempts);
        
        DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, exception) -> {
            // This runs after all retries are exhausted
            log.error("Failed to process message after {} retries. Topic: {}, Partition: {}, Offset: {}, Error: {}",
                    maxRetryAttempts,
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    exception.getMessage());
        }, backOff);
        
        // Don't retry these exceptions (they won't succeed on retry)
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                NullPointerException.class
        );
        
        return errorHandler;
    }
}
