package com.ssafy.exhi.domain.todo.model.entity;

import com.ssafy.exhi.domain.plan.model.entity.Plan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Todo {
    // 상태 투두
    @Id
    @GeneratedValue
    @Column(name = "todo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_todo_id")
    private CustomTodo customTodo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_todo_id")
    private DefaultTodo defaultTodo;

    @Column(name = "is_completed")
    private boolean isCompleted;
}
