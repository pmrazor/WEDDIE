package com.ssafy.exhi.domain.advice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import com.ssafy.exhi.domain.user.model.entity.UserDetail;
import com.ssafy.exhi.domain.user.repository.UserDetailRepository;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AdviceServiceImpl implements AdviceService {

    private final UserDetailRepository userDetailRepository;
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    @Value("${PERPLEXITY_API_KEY}")
    private String apiKey;

    private final String url = "https://api.perplexity.ai/chat/completions";

    public AdviceServiceImpl(UserDetailRepository userDetailRepository, CoupleRepository coupleRepository, UserRepository userRepository) {
        this.userDetailRepository = userDetailRepository;
        this.coupleRepository = coupleRepository;
        this.userRepository = userRepository;
    }

    private RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML));
        restTemplate.getMessageConverters().add(converter);
        return restTemplate;
    }

    @Override
    public String getAdvice(Integer userId, ServiceType serviceType) {

        try {
            UserDetail user = userDetailRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_DETAIL_NOT_FOUND));

            Couple couple = coupleRepository.findCoupleByUserId(userId).orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND));

            UserDetail fiance = userRepository.findById(user.getGender().equals(Gender.MALE) ? couple.getFemale().getId() : couple.getMale().getId())
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND))
                .getUserDetail();

            RestTemplate restTemplate = createRestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", String.format(
                "당신은 결혼 준비 전문가이며, 다양한 MBTI 유형과 서비스 일정에 맞는 조언을 제공합니다. "
                    + "적절한 멘트와 조언을 한 문장에 30자 이내로, 현재 진행하는 일정에 관해서만 간단하게 제공해주세요."
                    + "사용자와 상대방의 성향을 고려하여, 결혼 준비 과정에서 도움이 될 수 있는 멘트와 실용적인 조언을 제공해주세요.\n\n"
                    + "- 사용자 MBTI: %s\n"
                    + "- 사용자 성별: %s\n"
                    + "- 상대방 MBTI: %s\n"
                    + "- 상대방 성별: %s\n"

                    + "- 현재 진행하는 일정 (서비스 유형)은 %s 입니다.\n\n"

                    + "현재 진행하는 일정에 맞춰 아래 내용을 포함해주세요.\n"
                    + "1. 상대방에게 해주면 좋은 말 (예비 배우자가 기분 좋고 편안하게 일정에 참여할 수 있도록 도와줄 멘트)\n"
                    + "2. 일정 진행 시 도움이 되는 팁 (촬영, 드레스 선택, 신혼여행 준비 등 각 서비스에 맞는 실용적인 조언)\n\n"

                    + "- 서비스 유형 이 STUDIO (웨딩 화보 촬영) 일 때의 예제 :\n"
                    + "  상대방에게 해주면 좋은 말: '예비 신부님과 즐거운 촬영날! 카메라 앞에서 다소 어색하더라도 활짝 웃어보이면 좋아요!'\n"
                    + "  일정 진행 시 도움이 되는 팁: '촬영 시간이 길어질 수 있으니 미리 간단한 간식을 챙기면 좋고, 옷을 갈아입기 편하게 편한 복장을 입고 가세요.'\n\n"
                    + "이제 위 정보를 바탕으로, 적절한 멘트와 조언을 한 문장에 30자 이내로, 현재 진행하는 일정에 관해서만 간단하게 제공해주세요.",
                user.getMbti(), user.getGender(), fiance.getMbti(), fiance.getGender(), serviceType
            ));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llama-3.1-sonar-small-128k-online");
            requestBody.put("messages", Arrays.asList(message));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );

            String responseBody = response.getBody();
            log.info("{}", responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> messageContent = (Map<String, Object>) firstChoice.get("message");

            String content = (String) messageContent.get("content");
            return content;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get advice: " + e.getMessage(), e);
        }
    }
}
