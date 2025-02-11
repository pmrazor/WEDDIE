package com.ssafy.exhi.domain.recommendation.controller;

import com.ssafy.exhi.domain.recommendation.model.dto.RecommendationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Recommendations", description = "사용자 맞춤형 추천 서비스 API")
public interface RecommendationControllerDocs {

    // === 사용자용 API ===
    @Operation(
            summary = "사용자 클러스터 할당",
            description = "사용자의 프로필 정보(나이, MBTI, 예산 등)를 기반으로 유사한 성향의 클러스터에 할당합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "클러스터 할당 성공"),
            @ApiResponse(responseCode = "400", description = "필수 프로필 정보 누락 (나이, MBTI 등)"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
            @ApiResponse(responseCode = "500", description = "클러스터 할당 중 서버 오류 발생")
    })
    ResponseEntity<?> addUser(
            @Parameter(description = "Bearer {JWT 토큰}", required = true, example = "Bearer eyJhbGciOiJ...")
            @RequestHeader("Authorization") String accessToken
    );

    @Operation(
            summary = "맞춤형 플랜 추천",
            description = "사용자의 클러스터 정보와 선호도를 기반으로 최적화된 전시회 플랜을 추천합니다."
    )
    @ApiResponses({
            @ApiResponse(
                responseCode = "200", 
                description = "추천 플랜 조회 성공",
                content = @Content(schema = @Schema(implementation = RecommendationResponse.RecommendedPlansDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "404", description = "클러스터 정보 또는 버진로드 없음"),
            @ApiResponse(responseCode = "500", description = "추천 시스템 서버 오류")
    })
    ResponseEntity<?> recommendPlans(
            @Parameter(description = "Bearer {JWT 토큰}", required = true, example = "Bearer eyJhbGciOiJ...")
            @RequestHeader("Authorization") String accessToken
    );

    // === 관리자용 API ===
    @Operation(
            summary = "클러스터링 실행",
            description = "전체 사용자 데이터를 기반으로 Python 기반 K-means 클러스터링을 수행합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "클러스터링 수행 완료"),
            @ApiResponse(responseCode = "500", description = "스크립트 실행 오류 또는 30분 초과")
    })
    ResponseEntity<?> runPython();

    @Operation(
            summary = "클러스터링 입력 데이터 생성",
            description = "DB의 사용자 프로필 데이터를 클러스터링 입력용 JSON 형식으로 변환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "input_data.json 생성 완료"),
            @ApiResponse(responseCode = "500", description = "JSON 파일 생성 중 오류 발생")
    })
    ResponseEntity<?> setJson();

    @Operation(
            summary = "클러스터링 결과 DB 적용",
            description = "클러스터링으로 생성된 결과(centers.json, params.json)를 DB에 저장합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "클러스터링 결과 DB 저장 완료"),
            @ApiResponse(responseCode = "500", description = "JSON 파싱 오류 또는 DB 저장 실패")
    })
    ResponseEntity<?> getJson();
}
