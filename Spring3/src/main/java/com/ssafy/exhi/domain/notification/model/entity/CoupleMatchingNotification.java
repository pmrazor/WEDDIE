package com.ssafy.exhi.domain.notification.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CoupleMatchingNotification extends Notification {

    @Column(name = "message")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_status")
    private MatchingStatus matchingStatus;

    public void updateMatchingStatus(MatchingStatus matchingStatus) {
        this.matchingStatus = matchingStatus;
    }

}
