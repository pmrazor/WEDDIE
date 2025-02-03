package com.ssafy.exhi.domain.todo.model.entity;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.todo.model.dto.TodoRequest;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Todo {
    // 상태 투두
    @Id
    @GeneratedValue
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

    public void patch(TodoRequest.UpdateDTO updateDTO) {
        this.content = updateDTO.getContent();
    }

    public void check(TodoRequest.CheckDTO checkDTO) {
        this.isCompleted = checkDTO.getIsCompleted();
    }
}
