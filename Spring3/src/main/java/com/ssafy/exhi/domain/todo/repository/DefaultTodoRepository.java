package com.ssafy.exhi.domain.todo.repository;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.todo.model.entity.DefaultTodo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DefaultTodoRepository extends JpaRepository<DefaultTodo, Integer> {
    List<DefaultTodo> getDefaultTodoByServiceType(ServiceType serviceType);

}
