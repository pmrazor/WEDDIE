package com.ssafy.exhi.domain.todo.model.entity;

import com.ssafy.exhi.domain.ServiceType;
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

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_todo_id")
    private CustomTodo customTodo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_todo_id")
    private DefaultTodo defaultTodo;


    @Column(name = "is_completed")
    private boolean isCompleted;
}
/*
버진로드를 생성해요
그러면 12번의 플랜 추가 쿼리가 나가고
그 플랜에 대해서 12 * 투두 갯수 의 투두 추가 쿼리가 나가요??
이거맞아요?
 */