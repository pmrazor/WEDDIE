package com.ssafy.exhi.domain.tip.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.tip.model.dto.TipRequest;
import com.ssafy.exhi.domain.tip.model.dto.TipResponse;
import com.ssafy.exhi.domain.tip.model.model.Tip;
import com.ssafy.exhi.domain.tip.repository.TipRepository;
import com.ssafy.exhi.domain.tip.service.TipService;
import com.ssafy.exhi.util.JWTUtil;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TipController {

    private final JWTUtil jwtUtil;
    private final TipService tipService;
    private final TipRepository tipRepository;

    /**
     * @param planId
     * @return
     */
    @GetMapping("/plan/{planId}/tip")
    public ResponseEntity<?> getTips(
        @RequestHeader(name = "Authorization") String accessToken,
        @PathVariable("planId") Integer planId
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        List<TipResponse.DetailResultDTO> response = tipService.getTips(planId, userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    /**
     * 관리자용
     *
     * @return
     */
    @PostMapping("/tip")
    public ResponseEntity<?> createTip(
        @RequestHeader(name = "Authorization") String accessToken,
        @RequestBody @Valid TipRequest.CreateDTO createDTO
    ) {
        // TODO 관리자 인가 로직 추가 필요
        TipResponse.DetailResultDTO response = tipService.createTip(createDTO);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PostConstruct
    @Transactional
    public void init() {
        List<Tip> tips = List.of(
            new Tip(null, ServiceType.WEDDING_HALL, "웨딩홀 예약은 결혼 준비에서 가장 중요한 단계입니다. 성수기인 봄, 가을에는 예약 경쟁이 치열하므로 최소 1년 전에는 투어를 진행하고 계약을 완료하는 것이 좋아요. 특히 원하는 날짜와 골든타임이라 불리는 주말 점심시간을 선점하려면 서두르는 것이 중요합니다."),
            new Tip(null, ServiceType.WEDDING_HALL, "최소 보증 인원과 추가 비용 보증인원 미리 계산해서 가면 좋아요. 예식 홀 마다 최소 보증 인원이 다르기 때문에 최소 ~ 최대 인원수를 미리 고려해서 예식장을 알아보고 인원수에 적합한 홀 크기를 고르시는게 중요합니다."),
            new Tip(null, ServiceType.WEDDING_HALL, "웨딩홀 투어를 진행하며 실제 분위기가 밝은지 어두운지, 식사 메뉴가 뷔페인지 코스인지, 주차 공간과 하객 동선, 위치 등을 꼼꼼히 확인하세요. 특히 꽃장식, 음향, 조명, 포토테이블 등 추가 서비스 사항들을 꼼꼼히 확인하세요."),
            new Tip(null, ServiceType.WEDDING_HALL, "웨딩홀 예약 시 비용 항목을 명확히 확인하세요. 평일이나 비성수기를 선택하면 비용을 절약할 수 있습니다. 계약서에 포함된 항목과 추가 요금 발생 여부를 꼼꼼히 검토하세요."),
            new Tip(null, ServiceType.WEDDING_HALL, "요즘에는 폐백이 줄어드는 추세지만 어른들의 의견을 물어보는 것도 중요합니다. 폐백을 진행하게 된다면 폐백 절값을 준비할 수 있게 미리 폐백 여부를 양가에 알려드리는 게 좋습니다."),
            new Tip(null, ServiceType.WEDDING_HALL, "축의대 인원은 신부 측, 신랑 측 각 최소 2인을 섭외해주시는 게 좋습니다. 하객분들이 몰리는 시간대에 금액을 체크하는 데 실수가 생길 수 있어 최소 3명은 섭외하는 걸 추천드려요."),

            // 스튜디오
            new Tip(null, ServiceType.STUDIO, "촬영은 본식 3 ~ 5개월 전에 진행하는 경우가 많으며, 촬영한 사진은 바로 완성되지 않기 때문에 사진을 최종적으로 받을 때까지 두 달에서 길게는 세 달 이상이 걸릴 수 있어요. 웨딩홀에서 사용하고 싶다면 스튜디오와 촬영 날짜를 가능한 빨리 예약하는 것이 중요합니다."),
            new Tip(null, ServiceType.STUDIO, "웨딩촬영은 결혼식 전 두 사람의 소중한 순간을 사진으로 남길 수 있는 특별한 시간입니다. 원하는 스튜디오와 촬영 날짜를 확보하기 위해서는 본식 날짜가 확정된 직후에 바로 예약하는 게 좋아요. 특히 인기있는 스튜디오나 야외 촬영 장소는 예약이 빨리 마감되기 때문에 서두르세요!"),
            new Tip(null, ServiceType.STUDIO, "스튜디오 촬영은 사진 위주로 돌아가기에 생각보다 진한 메이크업을 하는 걸 추천드려요. 헤어와 악세사리는 샵에서 제공하는 경우가 있으니 미리 알아보고 가면 좋아요."),
            new Tip(null, ServiceType.STUDIO, "야외 촬영을 하시나요? 야외촬영은 계절과 날씨에 따라 결과물이 크게 달라지기 때문에 신중하게 일정을 잡아야 해요. 봄과 가을은 야외촬영의 최적기! 봄의 화사한 꽃과 가을의 단풍은 배경으로 완벽합니다. 더운 여름과 추운 겨울은 피하는 것이 좋아요. 땀이나 추위로 인해 촬영이 불편할 수 있습니다. 인기 촬영지 예약은 1년 전부터 시작하기도 해요. 제주도나 남해 등 유명 촬영지는 서둘러 예약하는 게 좋아요."),
            new Tip(null, ServiceType.STUDIO, "촬영의 완성도를 높이기 위해 디테일 챙기기. 컨셉 미리 정하기: 원하는 촬영 스타일(로맨틱, 빈티지, 캐주얼 등)을 미리 정해두면 만족도가 높아져요. 커플 아이템 활용: 커플 슈즈나 소품을 활용하면 더욱 자연스러운 분위기를 연출할 수 있습니다. 개인 소품 준비하기: 결혼반지, 특별한 의미가 담긴 액세서리 등 나만의 소품을 활용해보세요. 메이크업 수정 준비: 평소 사용하는 립스틱이나 메이크업 제품을 챙겨가 헬퍼에게 전달하세요. 부케와 장식 소품 활용: 스튜디오에 준비된 소품이나 부케를 적극 활용하면 촬영이 더욱 풍성해질 수 있습니다."),
            new Tip(null, ServiceType.STUDIO, "촬영 날짜를 정할 때는 신랑의 맞춤 정장이 완성된 시점 이후로 스케줄을 잡는 것이 좋아요. 맞춤 정장은 신랑의 체형에 딱 맞기 때문에 사진의 완성도를 더욱 높여줄 수 있습니다. 스튜디오에서 제공하는 대여 정장도 있지만, 맞춤 정장을 활용하면 더욱 멋진 결과물을 얻을 수 있어요."),
            new Tip(null, ServiceType.STUDIO, "여러 스튜디오의 포트폴리오를 구경하면서 캡쳐를 해보세요. 모아놓고 보면 본인이 선호하는 분위기, 배경, 보정, 구도/각도를 파악할 수 있습니다."),
            new Tip(null, ServiceType.STUDIO, "스튜디오의 추가금을 체크하세요. 로드신 / 옥상신, 오후 촬영 시 스튜디오 및 헬퍼 추가 비용, 샘플이 여러가지라면 샘플별 추가금 여부, 원본 혹은 수정본 필수 구매여부 or 포함 여부, 촬영시간이 이른 오전 시간이라면 얼리 메이크업 비용 등을 체크하면 좋아요."),
            new Tip(null, ServiceType.STUDIO, "스튜디오의 스타일은 크게 3가지로 나눌 수 있습니다. 1. 인물 중심 차분한 필터 섞인 느낌 2. 자연 배경 중심 화사하고 러블리한 느낌 3. 배경 많고 화보 느낌의 컨셉 부자 느낌"),
            new Tip(null, ServiceType.STUDIO, "웃는 얼굴을 연습할 땐 눈까지 웃는 연습을 하면 더 자연스럽고 예쁜 사진을 찍을 수 있답니다. 사진을 촬영해가면서 가장 자연스러운 웃는 얼굴을 연습해보세요."),

            // 드레스 샵
            new Tip(null, ServiceType.DRESS_SHOP, "드레스샵과 메이크업샵 선택은 웨딩홀 예약 직후 바로 진행해야 하는 중요한 단계입니다. 최소 본식 5-6개월 전에 시작해서 샵을 정하는 것이 권장됩니다."),
            new Tip(null, ServiceType.DRESS_SHOP, "주말에는 본식 드레스가 빠져 있는 경우가 많기 때문에, 예식이 없는 평일에 방문하면 더 다양한 드레스를 선택할 수 있어요."),
            new Tip(null, ServiceType.DRESS_SHOP, "드레스 가봉 시 추가 비용이 발생할 수 있어요. 드레스 종류, 장식 옵션에 따라 추가금이 붙기도 합니다."),
            new Tip(null, ServiceType.DRESS_SHOP, "웨딩촬영과 본식 날짜에 맞춰 여유 있는 일정을 잡아야 합니다. 드레스 가봉은 보통 2~3회 정도 진행되며, 최종 가봉은 본식 직전에 마무리해야 해요."),
            new Tip(null, ServiceType.DRESS_SHOP, "드레스 샵의 수용인원을 체크하면 좋아요."),
            new Tip(null, ServiceType.DRESS_SHOP, "화면에선 아름다워 보였던 디자인도 실제로 입었을 때는 착용감이나 핏이 다르게 느껴질 수 있습니다."),
            new Tip(null, ServiceType.DRESS_SHOP, "피팅 시 마음에 들지 않는 드레스를 입혀준다면, 커튼이 열리기 전에 상의해서 바꿔 입으세요."),
            new Tip(null, ServiceType.DRESS_SHOP, "드레스샵을 여러군데 방문할 예정이라면 가장 마음에 마음에 드는 샵을 마지막에 방문해보세요."),
            new Tip(null, ServiceType.DRESS_SHOP, "가봉할 때는 메이크업을 평소보다 진하게 하고, 원하는 헤어스타일을 미리 하고 가는 걸 추천드려요."),
            new Tip(null, ServiceType.DRESS_SHOP, "드레스 상태를 꼼꼼히 점검하는 것도 중요합니다. 특히 실크 드레스의 경우 소재의 상태나 오염 여부를 세심하게 살펴보세요."),
            new Tip(null, ServiceType.DRESS_SHOP, "계약 시 드레스 종류 및 옵션, 드레스 업그레이드 비용, 티아라/ 귀걸이 옵션, 메이크업 서비스 범위 등을 잘 체크하세요."),

            // 한복
            new Tip(null, ServiceType.HANBOK, "피로연에서 양가 어머님께서 입으실 한복은 결혼식 분위기를 더욱 돋보이게 하는 중요한 요소 중 하나입니다."),
            new Tip(null, ServiceType.HANBOK, "한복 선택 순서에 대한 현실적인 팁: 시어머니께서 먼저 방문하시면 좋습니다."),
            new Tip(null, ServiceType.HANBOK, "한복은 너무 화려하기보다는 고급스러움을 강조하는 것이 좋아요."),
            new Tip(null, ServiceType.HANBOK, "마지막 피팅 때는 작은 부분까지 꼼꼼하게 확인하고, 예식 당일 편안하게 착용할 수 있는지도 꼭 체크하세요."),

            // 테일러샵
            new Tip(null, ServiceType.TAILOR_SHOP, "남성 예복을 맞추기 전에 데일리로 사용할지, 경조사용으로 사용할지 예복의 용도를 미리 정하면 좋아요."),
            new Tip(null, ServiceType.TAILOR_SHOP, "남성 예복을 맞춤 정장을 한다면 최소 3달 전에 시작해야 여유로워요."),
            new Tip(null, ServiceType.TAILOR_SHOP, "남성 예복 맞춤 시 원단 바꿔치기 사기에 주의하세요."),

            // 페이셜케어
            new Tip(null, ServiceType.FACIAL_CARE, "웨딩관리는 결혼준비 과정에서 예비신부님들 사이에서 가장 인기 있는 선택지 중 하나입니다."),
            new Tip(null, ServiceType.FACIAL_CARE, "등드름 관리: 웨딩드레스로 드러나는 등이 깨끗하고 매끄럽게 보이도록 도와줍니다."),
            new Tip(null, ServiceType.FACIAL_CARE, "얼굴 윤곽 관리: 촬영과 본식에서 얼굴이 더욱 또렷하고 작아 보이도록 관리합니다."),

            // 메이크업
            new Tip(null, ServiceType.MAKEUP_STUDIO, "드레스샵과 메이크업샵 선택은 웨딩홀 예약 직후 바로 진행해야 하는 중요한 단계입니다."),
            new Tip(null, ServiceType.MAKEUP_STUDIO, "마음에 드는 이미지들을 캡처해 상담 시 보여주면, 스타일리스트와의 소통이 훨씬 원활해집니다."),
            new Tip(null, ServiceType.MAKEUP_STUDIO, "메이크업 리허설 일정도 본식 일정에 맞춰 여유롭게 잡아주세요."),

            // 스냅
            new Tip(null, ServiceType.SNAP, "해당 스냅업체의 밝은 홀 / 어두운 홀 경험 유무를 체크하세요."),
            new Tip(null, ServiceType.SNAP, "스냅의 분위기는 크게 세피아 / 피치 / 화이트 톤으로 나누어집니다."),
            new Tip(null, ServiceType.SNAP, "작가 추가 비용, 대표 작가 지정 비용, 신부 대기실, 피로연장 등 촬영 장소 옵션 비용을 잘 살펴보세요."),

            // 청첩장
            new Tip(null, ServiceType.INVITATION, "샘플을 받아볼 수 있는 곳들은 샘플을 받아서 재질과 디자인을 확인해보세요."),
            new Tip(null, ServiceType.INVITATION, "요즘은 모바일 청첩장의 선호도가 높아서 보증 인원보다 종이 청첩장을 전하는 수가 적습니다. 때문에 종이 청첩장은 보통 보증 인원에 맞춰서 준비를 하면 좋습니다. 식권이 제공되지 않는 웨딩홀의 경우, 청첩장 준비할 때 식권도 같이 준비하시면 수월하기 때문에 같이 준비하면 좋습니다. 식권의 경우는 보증인원보다 여유롭게 준비하는 것을 추천드립니다."),
            new Tip(null, ServiceType.INVITATION, "종이 청첩장으로 청첩장 모임을 가질 예정이라면 결혼식 4개월 전부터 청첩장 준비 하는걸 추천드립니다.")
        );

        tipRepository.saveAll(tips);
    }
}
