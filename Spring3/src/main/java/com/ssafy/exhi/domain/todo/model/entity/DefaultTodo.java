package com.ssafy.exhi.domain.todo.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.user.model.entity.Gender;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "default_todo_id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private ServiceType serviceType;

    @Column(name = "content")
    private String content;

    @Column(name = "gender")
    private Gender gender;

    public Todo toTodo(Plan plan) {
        return Todo.builder()
            .content(content)
            .serviceType(serviceType)
            .todoType(TodoType.valueOf(this.gender.toString()))
            .isCompleted(false)
            .plan(plan)
            .build();
    }
}
