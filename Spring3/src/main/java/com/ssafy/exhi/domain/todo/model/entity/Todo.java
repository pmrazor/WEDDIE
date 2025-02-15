package com.ssafy.exhi.domain.todo.model.entity;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.todo.model.dto.TodoRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Todo {
    // 상태 투두
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private ServiceType serviceType;

    @Column(name = "content")
    private String content;

    @Column(name = "is_completed")
    private boolean isCompleted;

    // 투두 타입 남자 : 0 , 여자 : 1 , 공동 : 2
    @Column(name = "todo_type")
    private TodoType todoType;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage")
    private StageType stage;



    public void patch(TodoRequest.UpdateDTO updateDTO) {
        this.content = updateDTO.getContent();
        this.todoType = updateDTO.getTodoType();
    }

    public void check(TodoRequest.CheckDTO checkDTO) {
        this.isCompleted = checkDTO.getIsCompleted();
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }
}
