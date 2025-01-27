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
public class DefaultTodo extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "default_todo_id")
    private Integer id;

    @Column(name = "content")
    private String content;
}
