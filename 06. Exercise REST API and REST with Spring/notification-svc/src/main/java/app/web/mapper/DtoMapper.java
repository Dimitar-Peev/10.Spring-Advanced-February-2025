package app.web.mapper;

import app.model.Notification;
import app.model.NotificationPreference;
import app.model.NotificationType;
import app.web.dto.NotificationPreferenceResponse;
import app.web.dto.NotificationResponse;
import app.web.dto.NotificationTypeRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    // Mapping logic: прехвърляме един тип данни към друг
    public static NotificationType fromNotificationTypeRequest(NotificationTypeRequest dto) {

        return switch (dto) {
            case EMAIL -> NotificationType.EMAIL;
        };
    }

    // Build dto from entity
    public static NotificationPreferenceResponse fromNotificationPreference(NotificationPreference entity) {

        return NotificationPreferenceResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .type(entity.getType())
                .enabled(entity.isEnabled())
                .contactInfo(entity.getContactInfo())
                .build();
    }

    public static NotificationResponse fromNotification(Notification entity) {

        // DTO building!
        return NotificationResponse.builder()
                .subject(entity.getSubject())
                .status(entity.getStatus())
                .createdOn(entity.getCreatedOn())
                .type(entity.getType())
                .build();
    }
}
