package com.ssafy.exhi.domain.todo.repository;

import com.ssafy.exhi.domain.todo.model.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Integer> {
    Optional<Todo> findTodoById(Integer id);

    List<Todo> findByPlanId(Integer planId);

}
