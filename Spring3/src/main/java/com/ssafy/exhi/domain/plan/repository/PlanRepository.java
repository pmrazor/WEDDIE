package com.ssafy.exhi.domain.plan.repository;

import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.todo.model.entity.TodoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findPlanById(Integer id);

    @Query("select distinct p from Plan p " +
        "left join fetch p.todos t " +
        "where p.id = :planId " +
        "and t.todoType in :todoTypes")
    Plan findPlanByIdWithFilteredTodos(
        @Param("planId") Integer planId,
        @Param("todoTypes") List<TodoType> todoTypes
    );

    // 유저 ID에 해당하는 플랜을 가져오는 쿼리 메서드
    List<Plan> findByVirginRoad_Couple_Id(Integer coupleId);  // VirginRoad가 UserId를 가지고 있다고 가정
}
