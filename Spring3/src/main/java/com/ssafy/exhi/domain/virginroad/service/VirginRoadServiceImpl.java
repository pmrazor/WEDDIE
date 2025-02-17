package com.ssafy.exhi.domain.virginroad.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.TimeWrapper;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.todo.model.entity.DefaultTodo;
import com.ssafy.exhi.domain.todo.model.entity.Todo;
import com.ssafy.exhi.domain.todo.repository.DefaultTodoRepository;
import com.ssafy.exhi.domain.todo.repository.TodoRepository;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.domain.virginroad.converter.VirginRoadConverter;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadRequest;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadResponse;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.domain.virginroad.repository.VirginRoadRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import com.ssafy.exhi.util.ServiceTypeScheduler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VirginRoadServiceImpl implements VirginRoadService {

    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final VirginRoadRepository virginRoadRepository;
    private final DefaultTodoRepository defaultTodoRepository;
    private final TodoRepository todoRepository;
    private final ServiceTypeScheduler serviceTypeScheduler;
    private final TimeWrapper timeWrapper;
    
    /**
     * 플랜과 버진로드를 동시에 생성하기
     * 버진로드가 비어있는 값은 디폴트로 생성 (visible이 false로 설정)
     *
     * @param createDTO
     * @return
     */
    @Override
    public VirginRoadResponse.SimpleResultDTO createVirginRoad(VirginRoadRequest.CreateDTO createDTO) {
        User user = findUserByUserId(createDTO.getUserId());
        Couple couple = findCoupleByUserId(createDTO.getUserId());
        existVirginRoad(couple);

        log.info("{}", createDTO);

        List<PlanRequest.CreateDTO> updatedPlans = setPlanDefaultDates(createDTO);
        createDTO.setPlans(updatedPlans);

        VirginRoad virginRoad = VirginRoadConverter.toEntity(createDTO, couple);

        List<ServiceType> visibleServiceTypes = updatedPlans.stream()
            .filter(PlanRequest.CreateDTO::isVisible)
            .map(PlanRequest.CreateDTO::getServiceType)
            .collect(Collectors.toList());

        Map<ServiceType, List<DefaultTodo>> defaultTodos = getDefaultTodos(user, visibleServiceTypes);

        log.debug("default todos -> {}", defaultTodos);
        virginRoad.setDefaultPlans(defaultTodos);

        // 버진로드 저장
        VirginRoad saved = virginRoadRepository.save(virginRoad);

        return VirginRoadConverter.toDTO(saved);
    }

    private List<PlanRequest.CreateDTO> setPlanDefaultDates(VirginRoadRequest.CreateDTO createDTO) {
        LocalDate marriageDate = coupleRepository.findCoupleByUserId(createDTO.getUserId())
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND)).getMarriageDate();
        LocalDate today = LocalDate.now();
        long totalDays = ChronoUnit.DAYS.between(today, marriageDate);

        Map<ServiceType, Double> scheduleDays = serviceTypeScheduler.getServiceTypeSchedule();

        List<PlanRequest.CreateDTO> updatedPlans = new ArrayList<>();

        for (PlanRequest.CreateDTO cur : createDTO.getPlans()) {
            Plan plan = PlanConverter.toEntity(cur);

            double percentage = scheduleDays.getOrDefault(cur.getServiceType(), 0.3);

            long daysBeforeWedding = (long) (totalDays * percentage);
            LocalDate planDate = marriageDate.minusDays(daysBeforeWedding);
            plan.setPlanTime(planDate.atStartOfDay());

            PlanRequest.CreateDTO updatedPlan = PlanConverter.toCreateDTO(plan, planDate);

            updatedPlans.add(updatedPlan);
        }

        return updatedPlans;
    }

    private Map<ServiceType, List<DefaultTodo>> getDefaultTodos(User user, List<ServiceType> visibleServiceTypes) {
        return defaultTodoRepository
            .getDefaultTodoByServiceTypeIn(visibleServiceTypes)
            .stream()
            .collect(Collectors.groupingBy(DefaultTodo::getServiceType));
    }

    /**
     * 버진로드를 한번에 보여준다.
     * 중요한 것은 모둔 결혼 단계의 상태를 보여주기
     *
     * @param userId
     * @return
     */
    @Override
    public VirginRoadResponse.SimpleResultDTO getVirginRoad(Integer userId) {
        Couple couple = findCoupleByUserId(userId);
        VirginRoad virginRoad = findVirginRoadByCouple(couple);

        for (Plan plan : virginRoad.getPlans()) {
            plan.calculateStartAndEndDates();
            plan.updatePlanStatus(timeWrapper.now());
        }

        return VirginRoadConverter.toDTO(virginRoad);
    }

    /**
     * 버진로드의 계획들을 모두 업데이트
     *
     * @param updateDTO
     * @return
     */
    @Override
    public VirginRoadResponse.SimpleResultDTO updateVirginRoad(VirginRoadRequest.UpdateDTO updateDTO) {
        // 1. 사용자의 버진로드를 들고오기
        Couple couple = findCoupleByUserId(updateDTO.getUserId());
        VirginRoad virginRoad = findVirginRoadByCouple(couple);

        // 2. 플랜 업데이트 및 visible 변경 체크
        List<Todo> toSaveTodo = new ArrayList<>();
        List<Todo> toDeleteTodo = new ArrayList<>();

        for (PlanRequest.UpdateDTO planDTO : updateDTO.getPlans()) {
            Plan plan = virginRoad.findPlanByServiceType(planDTO.getServiceType());

            // visible 수정됐다면
            if (plan.isVisible() != planDTO.isVisible()) {

                // 새로 들어온 plan 이 visible = true 라면 todo 에 default todo 추가해주기
                if (planDTO.isVisible()) {
                    List<DefaultTodo> defaultTodos = defaultTodoRepository.getDefaultTodoByServiceType(planDTO.getServiceType());
                    toSaveTodo.addAll(defaultTodos.stream().map(
                        todo -> todo.toTodo(plan)
                    ).toList());
                }
                // 기존에 있던 플랜이 제거됐다면, todo 테이블에서 제거해주기
                else {
                    toDeleteTodo.addAll(todoRepository.findByPlanId(plan.getId()));
                }
            }
            plan.updateVisibility(planDTO);
        }
        if (!toDeleteTodo.isEmpty()) todoRepository.deleteAll(toDeleteTodo);
        if (!toSaveTodo.isEmpty()) todoRepository.saveAll(toSaveTodo);

        return VirginRoadConverter.toDTO(virginRoad);
    }

    @Override
    public void finishVirginRoad(Integer userId) {
        Couple couple = findCoupleByUserId(userId);
        VirginRoad virginRoad = findVirginRoadByCouple(couple);

        virginRoad.finishVirginRoad();
    }

    /**
     * 버진로드를 삭제한다.
     *
     * @param userId
     */
    @Override
    public void deleteVirginRoad(Integer userId) {
        Couple couple = findCoupleByUserId(userId);
        VirginRoad virginRoad = findVirginRoadByCouple(couple);

        virginRoadRepository.delete(virginRoad);
    }

    private User findUserByUserId(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND)
            );
    }

    private Couple findCoupleByUserId(Integer userId) {
        return coupleRepository.findCoupleByUserId(userId).orElseThrow(
            () -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND)
        );
    }

    private VirginRoad findVirginRoadByCouple(Couple couple) {

        return virginRoadRepository.getVirginRoadByCouple(couple).orElseThrow(
            () -> new ExceptionHandler(ErrorStatus.VIRGIN_ROAD_NOT_FOUND)
        );
    }

    private void existVirginRoad(Couple couple) {
        if (virginRoadRepository.existsVirginRoadByCouple(couple)) {
            throw new ExceptionHandler(ErrorStatus.VIRGIN_ROAD_EXISTS);
        }
    }
}
