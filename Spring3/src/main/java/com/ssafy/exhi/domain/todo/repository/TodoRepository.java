package com.ssafy.exhi.domain.todo.repository;

import com.ssafy.exhi.domain.todo.model.entity.Todo;
import com.ssafy.exhi.domain.todo.model.entity.TodoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Integer> {
    Optional<Todo> findTodoById(Integer id);

    List<Todo> findByPlanId(Integer planId);

    List<Todo> findByPlanIdAndTodoTypeIn(
            @Param("planId") Integer planId,
            @Param("todoTypes") List<TodoType> todoTypes
    );

    void delete(Todo todo);
}
