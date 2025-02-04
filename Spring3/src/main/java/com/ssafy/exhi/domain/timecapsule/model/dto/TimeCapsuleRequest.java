package com.ssafy.exhi.domain.timecapsule.model.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TimeCapsuleRequest {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO {

        // 작성한 Plan 의 Id
        // 나중에 여기서 Shop 가져와서 뿌려주면 되겠지?
        private Integer planId;

        // 작성한 유저 id ( 남녀 각자 적을거니까 )
        private Integer userId;

        // 작성한 유저의 coupleId
        private Integer coupleId;

        // 좋았던 점
        @Size(max = 255, message = "좋았던 점은 최대 100자까지 입력 가능합니다.")
        private String goodContent;

        // 좋았던 추억 사진
        private String goodImage;

        // 다툰 부분
        @Size(max = 255, message = "다퉜던 점은 최대 100자까지 입력 가능합니다.")
        private String badContent;

        // 다퉜던 추억 사진
        private String badImage;
    }
}
