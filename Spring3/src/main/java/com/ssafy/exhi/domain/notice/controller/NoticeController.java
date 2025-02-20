package com.ssafy.exhi.domain.notice.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.notice.model.dto.NoticeRequest;
import com.ssafy.exhi.domain.notice.model.dto.NoticeResponse;
import com.ssafy.exhi.domain.notice.service.MinioImageService;
import com.ssafy.exhi.domain.notice.service.NoticeService;
import com.ssafy.exhi.util.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notice")
public class NoticeController {
    private final MinioImageService minioService;
    private final NoticeService noticeService;
    private final JWTUtil jwtUtil;

    /**
     * 관리자만 생성 가능
     *
     * @return
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNotice(
            @RequestHeader("Authorization") String accessToken,
            @ModelAttribute @Valid NoticeRequest.CreateDTO dto
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        // TODO 관리자 인증 추가 필요

        dto.setUserId(userId);
        log.info("createNotice");

        NoticeResponse.SimpleResultDTO response = noticeService.createNotice(dto);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    /**
     * 이미지 조회하기
     *
     * @param filename
     * @return
     */
    @GetMapping("/image/{filename}")
    public ResponseEntity<String> getImageUrl(@PathVariable String filename) {
        String imageUrl = minioService.getImageUrl(filename);
        return ResponseEntity.ok(imageUrl);
    }

    /**
     * 공지사항 목록 조회
     *
     * @return
     */
    @GetMapping
    public ResponseEntity<?> getNotices(
            Pageable pageable
    ) {
        NoticeResponse.PageDTO response = noticeService.getNotices(pageable);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<?> getNotice(
            @PathVariable("noticeId") Integer noticeId
    ) {
        NoticeResponse.DetailResultDTO response = noticeService.getNotice(noticeId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
    /**
     * {
     *     "isSuccess": true,
     *     "code": "COMMON200",
     *     "message": "성공입니다.",
     *     "result": {
     *         "contents": [
     *
     *             {
     *                 "id": 5,
     *                 "name": "국풍 한복",
     *                 "mainImageUrl": "https://example.com/images/hanbok-main.jpg",
     *                 "serviceType": "HANBOK",
     *                 "address": {
     *                     "city": "서울특별시",
     *                     "district": "종로구",
     *                     "latitude": 37.5725,
     *                     "longitude": 126.9873
     *                 },
     *                 "minPrice": {
     *                     "amount": 350000,
     *                     "currency": "KRW"
     *                 }
     *             }
     *         ],
     *         "listSize": 5,
     *         "isFirstPage": true,
     *         "isLastPage": false,
     *         "totalPages": 2,
     *         "totalElements": 10
     *     }
     * }
     */

    /**
     * 최신 공지를 카테고리별로 보여주는 API
     *
     * @return
     */
    @GetMapping("/main")
    public ResponseEntity<?> getNewestNotices(
            Pageable pageable
    ) {
        NoticeResponse.PageDTO response = noticeService.getNewestNotices(pageable);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

}
