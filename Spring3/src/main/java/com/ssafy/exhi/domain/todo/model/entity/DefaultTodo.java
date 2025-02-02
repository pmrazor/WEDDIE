package com.ssafy.exhi.domain.todo.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.ServiceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DefaultTodo extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "default_todo_id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private ServiceType serviceType;

    @Column(name = "content")
    private String content;
}
