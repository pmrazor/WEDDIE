package com.ssafy.exhi.domain.plan.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.TimeWrapper;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.timecapsule.model.entity.TimeCapsule;
import com.ssafy.exhi.domain.tip.model.entity.Tip;
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
@Setter
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

    @Column(name = "start_date")
    private LocalDateTime startTime;

    @Column(name = "plan_time")
    private LocalDateTime planTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "plan_status")
    @Enumerated(EnumType.STRING)
    private PlanStatus planStatus;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Todo> todos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "virgin_road_id")
    private VirginRoad virginRoad;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
    private List<TimeCapsule> timeCapsules = new ArrayList<>();

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tip_id", referencedColumnName = "tip_id")
    private Tip tip;

    /* 비즈니스 로직 */
    public void validateCreateTimeCapsule(TimeWrapper time) {
        if (!time.now().isAfter(planTime)) {
            throw new ExceptionHandler(ErrorStatus.TIMECAPSULE_VALIDATE);
        }
    }

    private static final int DURATION = 30;

    // calculate start and end dates using planTime and duration
    public void calculateStartAndEndDates() {
        if (planTime != null) {
            this.startTime = planTime.minusDays(DURATION);  // 시작 시간은 planTime에서 30일 전
            this.endTime = planTime.plusDays(DURATION);     // 종료 시간은 planTime에서 30일 후
        }
    }

    // planStatus 를 변경
    public void updatePlanStatus(LocalDateTime time) {
        if (startTime == null || endTime == null) return;

        // 진행된 일정이고 예약일이 지났다면 NOT_TIME_CAPSULE 로 업데이트 ( 이후 status 는 타임캡슐 작성 후 FINISHED 로 자동 변환 )
        if (time.isAfter(planTime.plusDays(1))) {
            planStatus = PlanStatus.NOT_TIME_CAPSULE;
            return;
        }

        // 예약일 - 30 을 지나가면 IN_PROGRESS 진입
        if (time.isAfter(startTime) && time.isBefore(planTime)) {
            planStatus = PlanStatus.IN_PROGRESS;
        }
    }

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
            .todos(new ArrayList<>())
            .build();
    }

    public void setDefaultTodos(ServiceType serviceType, Map<ServiceType, List<DefaultTodo>> defaultTodos) {
        if (!defaultTodos.containsKey(serviceType)) return;
        List<DefaultTodo> defaultTodoList = defaultTodos.get(serviceType);
        this.todos.addAll(
                defaultTodoList.stream().map(todo -> todo.toTodo(this)).toList()
        );
    }

    public void updateTimeCapsuleStatus() {
        long count = this.timeCapsules.size();
        if (count == 2) {
            this.planStatus = PlanStatus.FINISHED;
        }
    }

    /* 비즈니스 로직 */
}
