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
            @ApiResponse(responseCode = "400", description = "CLUSTERING_USER_DETAIL_NOT_FOUND - 사용자 상세정보가 없어 클러스터링이 불가능합니다"),
            @ApiResponse(responseCode = "401", description = "TOKEN_ERROR - 토큰 관련 오류"),
            @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND - 존재하지 않는 계정입니다\nRECOMMENDATION_NO_CLUSTER_CENTERS - 클러스터 중심점을 찾을 수 없습니다"),
            @ApiResponse(responseCode = "500", description = "CLUSTERING_CLUSTER_ERROR - 클러스터링 처리 중 오류가 발생했습니다")
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
            @ApiResponse(responseCode = "400", description = "RECOMMENDATION_DATA_NOT_READY - 추천을 위한 데이터가 준비되지 않았습니다"),
            @ApiResponse(responseCode = "401", description = "TOKEN_ERROR - 토큰 관련 오류"),
            @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND - 존재하지 않는 계정입니다\nCOUPLE_NOT_FOUND - 커플 정보를 찾을 수 없습니다\nRECOMMENDATION_CLUSTER_NOT_FOUND - 사용자의 클러스터 정보를 찾을 수 없습니다"),
            @ApiResponse(responseCode = "500", description = "RECOMMENDATION_CLUSTER_ERROR - 추천 처리 중 오류가 발생했습니다")
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
            @ApiResponse(responseCode = "500", description = "CLUSTERING_SCRIPT_ERROR - 클러스터링 스크립트 실행 오류가 발생했습니다"),
            @ApiResponse(responseCode = "408", description = "CLUSTERING_SCRIPT_TIMEOUT - 클러스터링 스크립트 실행 시간이 초과되었습니다")
    })
    ResponseEntity<?> runPython();

    @Operation(
            summary = "클러스터링 입력 데이터 생성",
            description = "DB의 사용자 프로필 데이터를 클러스터링 입력용 JSON 형식으로 변환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "input_data.json 생성 완료"),
            @ApiResponse(responseCode = "404", description = "CLUSTERING_NO_DATA - 클러스터링에 필요한 데이터가 존재하지 않습니다\nCLUSTERING_USER_DETAIL_NOT_FOUND - 사용자 상세정보가 없어 클러스터링이 불가능합니다"),
            @ApiResponse(responseCode = "500", description = "CLUSTERING_FILE_IO_ERROR - 클러스터링 중 파일 입출력 오류가 발생했습니다")
    })
    ResponseEntity<?> setJson();

    @Operation(
            summary = "클러스터링 결과 DB 적용",
            description = "클러스터링으로 생성된 결과(centers.json, params.json)를 DB에 저장합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "클러스터링 결과 DB 저장 완료"),
            @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND - 존재하지 않는 계정입니다\nCOUPLE_NOT_FOUND - 커플 정보를 찾을 수 없습니다\nCLUSTERING_FILE_IO_ERROR - 클러스터링 파일을 찾을 수 없습니다"),
            @ApiResponse(responseCode = "500", description = "CLUSTERING_CLUSTER_ERROR - 클러스터링 처리 중 오류가 발생했습니다")
    })
    ResponseEntity<?> getJson();
}
