package com.fooddelivery.notification.service;

import com.fooddelivery.notification.dto.NotificationEvent;
import com.fooddelivery.notification.model.NotificationLog;
import com.fooddelivery.notification.model.NotificationLog.NotificationStatus;
import com.fooddelivery.notification.repository.NotificationRepository;
import com.fooddelivery.notification.service.channel.LogNotificationChannel;
import com.fooddelivery.notification.service.channel.NotificationChannel;
import com.fooddelivery.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationServiceImpl notificationService;
    private LogNotificationChannel logChannel;

    @BeforeEach
    void setUp() {
        logChannel = new LogNotificationChannel();
        List<NotificationChannel> channels = List.of(logChannel);
        notificationService = new NotificationServiceImpl(notificationRepository, channels);
    }

    @Test
    void processNotification_Success() {
        // Given
        NotificationEvent event = createTestEvent("ORDER_CREATED", "ORD-001");
        
        when(notificationRepository.existsByReferenceIdAndEventType(any(), any())).thenReturn(false);
        when(notificationRepository.save(any(NotificationLog.class))).thenAnswer(invocation -> {
            NotificationLog log = invocation.getArgument(0);
            log.setId(UUID.randomUUID());
            return log;
        });

        // When
        NotificationLog result = notificationService.processNotification(event);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SUCCESS);
        assertThat(result.getEventType()).isEqualTo("ORDER_CREATED");
        assertThat(result.getReferenceId()).isEqualTo("ORD-001");
        
        verify(notificationRepository, times(2)).save(any(NotificationLog.class));
    }

    @Test
    void processNotification_DuplicateEvent_Skipped() {
        // Given
        NotificationEvent event = createTestEvent("ORDER_CREATED", "ORD-001");
        
        when(notificationRepository.existsByReferenceIdAndEventType("ORD-001", "ORDER_CREATED"))
                .thenReturn(true);
        when(notificationRepository.save(any(NotificationLog.class))).thenAnswer(invocation -> {
            NotificationLog log = invocation.getArgument(0);
            log.setId(UUID.randomUUID());
            return log;
        });

        // When
        NotificationLog result = notificationService.processNotification(event);

        // Then
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SKIPPED);
        assertThat(result.getErrorMessage()).contains("Duplicate");
    }

    @Test
    void processNotification_NullEvent_ThrowsException() {
        assertThatThrownBy(() -> notificationService.processNotification(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void processNotification_MissingEventType_ThrowsException() {
        NotificationEvent event = NotificationEvent.builder()
                .referenceId("ORD-001")
                .build();

        assertThatThrownBy(() -> notificationService.processNotification(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Event type is required");
    }

    @Test
    void processNotification_MissingReferenceId_ThrowsException() {
        NotificationEvent event = NotificationEvent.builder()
                .eventType("ORDER_CREATED")
                .build();

        assertThatThrownBy(() -> notificationService.processNotification(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reference ID is required");
    }

    private NotificationEvent createTestEvent(String eventType, String referenceId) {
        return NotificationEvent.builder()
                .eventType(eventType)
                .referenceId(referenceId)
                .userId("user-123")
                .message("Test notification message")
                .metadata(Map.of("key", "value"))
                .timestamp(Instant.now())
                .traceId("trace-123")
                .build();
    }
}
