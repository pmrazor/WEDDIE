package com.ssafy.exhi.domain.conference.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.category.model.entity.Category;
import com.ssafy.exhi.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Conference extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "conference_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conference_category_id")
    private Category category;

    @Column(name = "call_start_time")
    private LocalDateTime callStartTime;

    @Column(name = "call_end_time")
    private LocalDateTime callEndTime;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "isActive")
    private Boolean isActive;
}
