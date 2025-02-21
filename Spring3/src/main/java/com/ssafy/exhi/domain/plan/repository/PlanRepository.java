package com.ssafy.exhi.domain.plan.repository;

import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.todo.model.entity.TodoType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanRepository extends JpaRepository<Plan, Integer> {
    Optional<Plan> findPlanById(Integer id);

    @Query("SELECT DISTINCT p FROM Plan p " +
        "LEFT JOIN FETCH p.todos t " +
        "WHERE p.id = :planId " +
        "AND t.todoType in :todoTypes")
    Plan findPlanByIdWithFilteredTodos(
        @Param("planId") Integer planId,
        @Param("todoTypes") List<TodoType> todoTypes
    );

    // 유저 ID에 해당하는 플랜을 가져오는 쿼리 메서드
    @Query("""
        SELECT DISTINCT p FROM Plan p
        LEFT JOIN FETCH p.virginRoad v
        WHERE v.couple.id = :coupleId
        ORDER BY
            CASE
                WHEN p.planStatus = 'IN_PROGRESS' THEN 1
                WHEN p.planStatus = 'BEFORE_START' THEN 2
                ELSE 3
            END,
            p.planTime
    """)
    List<Plan> findByVirginRoadCoupleId(@Param("coupleId") Integer coupleId);  // VirginRoad가 UserId를 가지고 있다고 가정

    @Query("""
        SELECT DISTINCT p FROM Plan p
        LEFT JOIN FETCH p.virginRoad v
        WHERE v.couple.id = :coupleId
                AND p.visible = true
        ORDER BY
            CASE
                WHEN p.planStatus = 'IN_PROGRESS' THEN 1
                WHEN p.planStatus = 'BEFORE_START' THEN 2
                ELSE 3
            END,
            p.planTime desc
    """)
    List<Plan> findByVirginRoadCoupleIdDesc(@Param("coupleId") Integer coupleId);

    List<Plan> findByPlanStatusIsIn(List<PlanStatus> planStatuses);

    List<Plan> findPlansByPlanStatusNot(PlanStatus planStatus);

}
