package com.ssafy.exhi.domain.todo.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
}
