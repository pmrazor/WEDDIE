package com.ssafy.exhi.domain.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name="Recommendations", description = "사용자 추천 서비스 API")
public interface RecommendationControllerDocs {

    @Operation(
            summary = "사용자 클러스터링 등록",
            description = "사용자의 나이, MBTI, 예산 등 프로필 정보를 기반으로 적절한 클러스터에 할당합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "클러스터 할당 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
    })
    ResponseEntity<?> addUser(
            @Parameter(description = "JWT 액세스 토큰", required = true)
            @RequestHeader("Authorization") String accessToken
    );

    @Operation(
            summary = "맞춤 플랜 추천",
            description = "사용자가 속한 클러스터의 특성을 기반으로 맞춤형 전시회 플랜을 추천합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천 플랜 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "클러스터 정보를 찾을 수 없음")
    })
    ResponseEntity<?> recommendPlans(
            @Parameter(description = "JWT 액세스 토큰", required = true)
            @RequestHeader("Authorization") String accessToken
    );

    @Operation(
            summary = "클러스터링 스크립트 실행",
            description = "사용자 데이터를 기반으로 Python 클러스터링 스크립트를 실행합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스크립트 실행 성공"),
            @ApiResponse(responseCode = "500", description = "스크립트 실행 실패 또는 타임아웃")
    })
    ResponseEntity<?> runPython();

    @Operation(
            summary = "클러스터링용 JSON 데이터 생성",
            description = "DB의 사용자 데이터를 클러스터링에 필요한 JSON 형식으로 변환하여 파일로 저장합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JSON 파일 생성 성공"),
            @ApiResponse(responseCode = "500", description = "파일 생성 실패")
    })
    ResponseEntity<?> setJson();

    @Operation(
            summary = "클러스터링 결과 처리",
            description = "Python 스크립트가 생성한 클러스터링 결과를 읽어 DB에 저장합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "클러스터링 결과 처리 성공"),
            @ApiResponse(responseCode = "500", description = "결과 처리 실패")
    })
    ResponseEntity<?> getJson();
}
