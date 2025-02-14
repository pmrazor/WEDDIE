package com.ssafy.exhi.domain.plan.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.timecapsule.model.entity.TimeCapsule;
import com.ssafy.exhi.domain.todo.model.entity.DefaultTodo;
import com.ssafy.exhi.domain.todo.model.entity.Todo;
import com.ssafy.exhi.domain.tip.model.entity.Tip;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
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

    // 시작 날짜
    @Column(name = "start_date")
    private LocalDateTime startTime;

    // 당일날짜
    @Column(name = "plan_time")
    private LocalDateTime planTime;

    // 끝 날짜
    @Column(name = "end_time")
    private LocalDateTime endTime;

    // 전, 중, 타입캡슐필요, 완전 끝
    @Column(name = "plan_status")
    @Enumerated(EnumType.STRING)
    private PlanStatus planStatus;

    // 플랜들
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Todo> todos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "virgin_road_id")
    private VirginRoad virginRoad;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
    private List<TimeCapsule> timeCapsules = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tip_id", referencedColumnName = "tip_id")
    private Tip tip;


    /* 비즈니스 로직 */

    // 서비스 로직에서 호출할 메서드, 엔티티에서 날짜를 계산하여 설정
    public void calculateStartAndEndDates(int duration) {
        if (planTime != null) {
            // planTime에서 duration 만큼 빼고 더하기
            this.startTime = planTime.minusDays(duration); // startDate는 planTime에서 30일 전
            this.endTime = planTime.plusDays(duration); // endDate는 planTime에서 30일 후
        }
    }

    // 플랜 Status를 업데이트하는 메서드, planTime 기준으로 계싼
    public void updatePlanStatus(LocalDateTime currentDateTime, int duration) {
        // planTime을 기준으로 상태를 결정
        if (planTime.isAfter(currentDateTime.plusDays(duration))) {
            this.planStatus = PlanStatus.BEFORE_START; // planTime이 30일 후면 BEFORE_START
        } else if (planTime.isBefore(currentDateTime.minusDays(duration))) {
            this.planStatus = PlanStatus.FINISHED; // planTime이 30일 전이면 FINISHED
        } else if (planTime.isAfter(currentDateTime) && planTime.isBefore(currentDateTime.plusDays(duration))) {
            this.planStatus = PlanStatus.IN_PROGRESS; // planTime이 현재보다 크고 30일 이내면 IN_PROGRESS
        } else {
            this.planStatus = PlanStatus.NOT_TIME_CAPSULE; // planTime이 현재보다 크고 30일 이내면 NOT_TIME_CAPSULE
        }

        log.info("Plan status updated to: {}", this.planStatus);
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
            defaultTodoList.stream().map(
                todo -> todo.toTodo(this)
            ).toList()
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
