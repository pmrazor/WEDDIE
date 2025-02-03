package com.ssafy.exhi.domain.plan.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.todo.model.entity.DefaultTodo;
import com.ssafy.exhi.domain.todo.model.entity.Todo;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Plan extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Integer id;

    @Column(name = "shop_name", nullable = true)
    private String shopName;

    @Column(name = "service_type")
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @Column(name = "visible")
    private boolean visible;

    @Column(name = "plan_time")
    private LocalDateTime planTime;

    @Column(name = "plan_status")
    @Enumerated(EnumType.STRING)
    private PlanStatus planStatus;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Todo> todos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "virgin_road_id")
    private VirginRoad virginRoad;

    /* 비즈니스 로직 */

    public void updateVirginRoad(VirginRoad virginRoad) {
        if (this.virginRoad != null) {
            this.virginRoad.getPlans().remove(this);
        }

        this.virginRoad = virginRoad;
    }

    public void updateVisibility(PlanRequest.UpdateDTO updateDTO) {
        if (!this.serviceType.equals(updateDTO.getServiceType())) {
            log.error("버진로드 서비스 타입 불일치!!!");
            throw new ExceptionHandler(ErrorStatus.VIRGIN_ROAD_NOT_FOUND);
        }
        this.visible = updateDTO.isVisible();
    }

    public static Plan createDefault(ServiceType serviceType, VirginRoad virginRoad) {
        return Plan.builder()
                .virginRoad(virginRoad)
                .serviceType(serviceType)
                .visible(false) // 디폴트 값 설정 (예: isVisible = false)
                .planStatus(PlanStatus.BEFORE_START)
                .build();
    }

    public void setDefaultTodos(ServiceType serviceType, Map<ServiceType, List<DefaultTodo>> defaultTodos) {
        if (!defaultTodos.containsKey(serviceType)) return;

        List<DefaultTodo> defaultTodoList = defaultTodos.get(serviceType);

        this.todos.addAll(
                defaultTodoList.stream().map(
                        todo -> todo.toTodo(this)
                ).toList()
        );
    }

    /* 비즈니스 로직 */

}
