package com.ssafy.exhi.domain.todo.config;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.todo.model.entity.DefaultTodo;
import com.ssafy.exhi.domain.todo.repository.DefaultTodoRepository;
import com.ssafy.exhi.domain.todo.repository.TodoRepository;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.domain.virginroad.converter.VirginRoadConverter;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.domain.virginroad.repository.VirginRoadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TodoInitializerTest {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final VirginRoadRepository virginRoadRepository;
    private final DefaultTodoRepository defaultTodoRepository;

    public TodoInitializerTest(UserRepository userRepository, CoupleRepository coupleRepository, VirginRoadRepository virginRoadRepository, DefaultTodoRepository defaultTodoRepository, TodoRepository todoRepository) {
        this.userRepository = userRepository;
        this.coupleRepository = coupleRepository;
        this.virginRoadRepository = virginRoadRepository;
        this.defaultTodoRepository = defaultTodoRepository;
        this.todoRepository = todoRepository;
    }

    @Autowired
    private TodoInitializer todoInitializer;
    private VirginRoad virginRoad;
    private Couple couple;
    private User female;
    private User male;


    @BeforeEach
    public void setUp() {
        female = initUser();
        male = initUser();
        couple = initCouple(male, female);

        virginRoad = VirginRoad.builder()
                .plans(new ArrayList<>())
                .couple(couple)
                .build();
    }

    private Couple initCouple(User male, User female) {
        return Couple.builder()
                .female(female)
                .male(male)
                .build();
    }

    private User initUser() {
        return User.builder()
                .build();
    }

    @Test
    public void test() {

        // given
        Plan plan = Plan.createDefault(ServiceType.DRESS_SHOP, virginRoad);

        // when 버진로드 생성할 때 디폴트로 들어가는거?
        todoInitializer.initializeTodosForPlan(plan);

        // then
    }



}