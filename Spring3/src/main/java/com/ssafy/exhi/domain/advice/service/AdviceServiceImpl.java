package com.ssafy.exhi.domain.advice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import com.ssafy.exhi.domain.user.model.entity.UserDetail;
import com.ssafy.exhi.domain.user.repository.UserDetailRepository;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdviceServiceImpl implements AdviceService {

    private final UserDetailRepository userDetailRepository;
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    @Value("${PERPLEXITY_API_KEY}")
    private String apiKey;

    private final String url = "https://api.perplexity.ai/chat/completions";

    private RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML));
        restTemplate.getMessageConverters().add(converter);
        return restTemplate;
    }

    @Override
    // messageType true = 감성 조언 , false = 이성 조언
    public String getAdvice(Integer userId, boolean messageType) {
        try {
            UserDetail user = userDetailRepository.findByUserId(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_DETAIL_NOT_FOUND));

            Couple couple = coupleRepository.findCoupleByUserId(userId).orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND));

            UserDetail fiance = userRepository.findById(user.getGender().equals(Gender.MALE) ? couple.getFemale().getId() : couple.getMale().getId())
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND))
                .getUserDetail();

            RestTemplate restTemplate = createRestTemplate();

            ServiceType serviceType = planRepository.findByVirginRoadCoupleIdDesc(couple.getId()).get(0).getServiceType();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");

            message = putMessage(messageType, message, serviceType, user, fiance);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llama-3.1-sonar-huge-128k-online");
            requestBody.put("messages", Collections.singletonList(message));

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

    public Map<String, Object> putMessage(boolean messageType, Map<String, Object> message, ServiceType serviceType, UserDetail user, UserDetail fiance) {
        if (messageType) {
            message.put("content", String.format(
                "안녕 당신은 웨딩 플래너 중에서도 가장 훌륭하고 정중하고 상냥한 웨딩 플래너 전문가입니다."
                        + "서비스 유형이 " + serviceType + "인 일정 전에 신랑 신부에게 전달해줄 상냥한 3줄의 메세지를 보내주세요.\n\n"
                    + "참고 : WEDDING_HALL : 웨딩홀 , STUDIO : 웨딩 화보 촬영 , DRESS_SHOP : 드레스 투어, MAKEUP_STUDIO : 메이크업 스튜디오,"
                    + "HANBOK : 맞춤 한복 예약, TAILOR SHOP : 남성 예복 맞춤, SNAP : 결혼식 당일 영상 촬영할 업체 예약 , INVITATION : 청첩장 준비,"
                    + "FACIAL_CARE : 결혼식 전 피부 케어와 다이어트 등, HONEYMOON : 신혼 여행 준비, WEDDING_DAY : 결혼식 당일 입니다."
                    + "사용자의 MBTI를 고려하여 서비스 유형 일정 진행에 대한 도움말을 사용자에게만 보내주세요. 절대로 메세지만 반환해주세요. 다른 말은 하지 말아 주세요.\n\n"

                    + "사용자 MBTI: %s , 사용자 성별: %s \n"

                    + "서비스 유형이 STUDIO (웨딩 화보 촬영) 이고 MBTI 가 ENFP 이고, 사용자 성별이 여성인 경우 메세지 응답 예시:\n\n"
                    + "아래에 예시를 보여드리겠습니다. 아래의 경우들에 나오는 메세지처럼 메세지만을 Markdown 형식으로 반환해 주세요. \n"

                    + "활기 넘치는 우리 신부님, 오늘은 당신의 상상력과 열정이 현실이 되는 마법 같은 날이에요! 🤗🤗\n"
                    + "거울 앞에 설 때마다 온 세상이 당신을 중심으로 빛나고 있다고 상상해보세요.\n"
                    + "레이스 하나, 비즈 하나가 당신의 꿈과 모험을 이야기하고 있어요.\n"
                    + "드레스를 입은 당신의 모습에 감동받을 소중한 이들의 다양한 반응을 상상해보세요.😍\n"
                    + "웃음, 환호, 그리고 감동의 눈물까지!\n"
                    + "오늘, 당신은 모두의 마음을 밝히는 빛나는 불꽃이 될 거예요. ✨✨\n"
                    + "이 특별한 순간, 당신의 독특한 매력으로 세상을 더욱 아름답게 물들여주세요!\n\n"

                    + "서비스 유형이 WEDDING_DAY (결혼식 당일) 이고 MBTI 가 ISTP 이고, 사용자 성별이 남성인 경우 메세지 응답 예시 : \n\n"
                    + "아래에 예시를 보여드리겠습니다. 아래의 경우들에 나오는 메세지처럼 메세지만을 Markdown 형식으로 반환해 주세요. \n"

                    + "실용적이고 침착한 신랑님, 이제 곧 인생의 중요한 순간이 다가옵니다. 🤗🤗"
                    + "긴장되더라도, 그것은 자연스러운 반응일 뿐이에요."
                    + "이 순간을 차분히 관찰하고 느껴보세요."
                    + "신부님과 처음 만났을 때부터 지금까지, 둘의 관계가 어떻게 발전했는지 되돌아보세요."
                    + "오늘, 그 유대감은 더욱 견고해질 것입니다. 😊😊"
                    + "당신의 행동으로 보여주는 진심, 그리고 손끝으로 전하는 따뜻함이 평생의 동반자에게 가장 큰 힘이 될 거예요 ",

                user.getMbti(), user.getGender(), fiance.getMbti(), fiance.getGender(), serviceType
            ));
            return message;
        }

        message.put("content", String.format(
            "안녕 당신은 웨딩 플래너 중에서도 가장 꼼꼼하고 섬세하고 준비성이 좋은 웨딩 플래너 전문가입니다."
                    + "서비스 유형이 " + serviceType + "인 일정 전에 신랑 신부에게 전달해줄 섬세한 3줄의 메세지를 보내주세요.\n"
                + "참고 : WEDDING_HALL : 웨딩홀 , STUDIO : 웨딩 화보 촬영 , DRESS_SHOP : 드레스 투어, MAKEUP_STUDIO : 메이크업 스튜디오,"
                + "HANBOK : 맞춤 한복 예약, TAILOR SHOP : 남성 예복 맞춤, SNAP : 결혼식 당일 영상 촬영할 업체 예약 , INVITATION : 청첩장 준비,"
                + "FACIAL_CARE : 결혼식 전 피부 케어와 다이어트 등, HONEYMOON : 신혼 여행 준비, WEDDING_DAY : 결혼식 당일 입니다."
                + "사용자의 MBTI를 고려하여 서비스 유형 일정 진행에 대한 도움말을 사용자에게만 보내주세요. 절대로 메세지만 반환해주세요. 다른 말은 하지 말아 주세요.\n\n"

                + "사용자 MBTI: %s, 사용자 성별 : %s, 상대방 MBTI: %s, 상대방 성별 : %s \n\n"

                + "서비스 유형이 DRESS_SHOP (웨딩드레스 투어) 이고 MBTI 가 ISTP 인 남성이고, 상대방은 MBTI 가 ENFP 인 여성인 메세지 응답 예시:\n\n"
                + "아래에 예시를 보여드리겠습니다. 아래의 경우들에 나오는 메세지처럼 메세지만을 Markdown 형식으로 반환해 주세요. \n"

                + "안녕하세요, 신랑님. 내일은 신부님의 웨딩드레스 투어가 있는 날입니다. 👰\n"
                + "실용적인 관점에서 몇 가지 체크리스트를 알려드리겠습니다. \n"
                + "편안한 신발을 준비해주세요. 신부님이 여러 드레스를 입어볼 때 오래 서 있어야 할 수 있습니다.\n"
                + "카메라나 스마트폰을 충전해오세요. 신부님의 모습을 기록하고 싶으실 겁니다.\n"
                + "예산을 미리 상의해두세요. 드레스 가격대를 정해두면 선택에 도움이 됩니다.\n"
                + "신부님의 액세서리나 소품을 가져오세요. 전체적인 모습을 확인하는 데 유용합니다.\n"
                + "객관적인 의견을 제시해주세요. ENFP 신부님은 여러 옵션에 흥분할 수 있으니, 당신의 냉철한 판단이 도움될 것입니다.\n"
                + "신부님의 창의성과 당신의 실용성이 만나 최고의 선택을 하실 수 있을 거예요. 즐거운 투어 되세요!👋👋"

                + "서비스 유형이 STUDIO (웨딩 화보 촬영) 이고 MBTI 가 ENFP 인 여성이고, 상대방 MBTI 가 INFJ 인 남성의 메세지 응답 예시: \n\n"
                + "아래에 예시를 보여드리겠습니다. 아래의 경우들에 나오는 메세지처럼 메세지만을 Markdown 형식으로 반환해 주세요. \n"

                + "안녕하세요, 신부님. 내일 웨딩 화보 촬영을 앞두고 계시네요. 📸\n"
                + "성공적인 촬영을 위해 다음 사항들을 준비해주세요:\n"
                + "웨딩드레스와 액세서리를 미리 점검하세요. 얼룩이나 손상이 없는지 확인해주세요.\n"
                + "메이크업과 헤어 용품을 준비하세요. 긴 촬영 동안 수정이 필요할 수 있습니다.\n"
                + "편안한 신발과 옷을 별도로 준비하세요. 이동 시 착용할 수 있습니다.\n"
                + "간식과 물을 챙기세요. 에너지 유지에 도움이 됩니다.\n"
                + "포즈나 컨셉에 대해 미리 생각해보세요.\n"
                + "당신의 창의적인 아이디어를 INFJ 신랑님과 공유하면 좋을 것 같아요.\n"
                + "촬영 장소와 일정을 다시 한 번 확인하세요.\n"
                + "당신의 열정과 신랑님의 섬세함이 어우러져 아름다운 사진이 나올 거예요. 편안한 마음으로 즐겁게 임하세요!👋👋",

            user.getMbti(), user.getGender(), fiance.getMbti(), fiance.getGender(), serviceType
        ));
        return message;
    }
}
