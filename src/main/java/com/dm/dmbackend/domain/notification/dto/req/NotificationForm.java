package com.dm.dmbackend.domain.notification.dto.req;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationForm {
    @Column(columnDefinition = "jsonb") // PostgreSQL의 jsonb 타입 사용
    @Type(JsonType.class)
    private List<Long> notificationIdList;
}
