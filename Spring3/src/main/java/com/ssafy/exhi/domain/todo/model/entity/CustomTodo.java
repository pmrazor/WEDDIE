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
public class CustomTodo extends BaseEntity {
    @Id
    @Column(name = "custom_todo_id")
    @GeneratedValue
    private Integer id;

    @Column(name = "content")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private ServiceType serviceType;

}
