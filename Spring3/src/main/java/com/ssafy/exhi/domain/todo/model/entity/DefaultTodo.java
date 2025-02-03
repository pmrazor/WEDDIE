package com.ssafy.exhi.domain.todo.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
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

    public Todo toTodo(Plan plan) {
        return Todo.builder()
                .content(content)
                .serviceType(serviceType)
                .isCompleted(false)
                .plan(plan)
                .build();
    }
}
