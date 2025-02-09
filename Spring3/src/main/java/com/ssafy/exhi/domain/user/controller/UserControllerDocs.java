package com.ssafy.exhi.domain.user.controller;

import com.ssafy.exhi.domain.oauth.UserPrincipal;
import com.ssafy.exhi.domain.user.model.dto.UserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Users", description = "사용자 관리 API")
public interface UserControllerDocs {


    @Operation(
            summary = "로그인",
            description = "사용자 ID와 비밀번호로 로그인하여 JWT 토큰을 발급받습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "로그인 성공"
            ),
            @ApiResponse(responseCode = "401", description = "잘못된 비밀번호"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 계정")
    })
    ResponseEntity<?> login(
            @RequestBody UserRequest.LoginDTO user
    );

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자를 등록합니다. 모든 사용자가 접근 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공"
            ),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 사용자 ID")
    })
    ResponseEntity<?> register(
            @RequestBody UserRequest.SignUpDTO user
    );

    @Operation(
            summary = "내 프로필 조회",
            description = "로그인한 사용자의 프로필 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "프로필 조회 성공"
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    ResponseEntity<?> me(
            @Parameter(description = "JWT 액세스 토큰", required = true)
            @RequestHeader("Authorization") String accessToken,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(
            summary = "중복 ID 체크",
            description = "특정 사용자 ID의 존재 여부를 확인합니다. 모든 사용자가 접근 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 조회 성공"
            )
    })
    ResponseEntity<?> checkDuplicateLoginId(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable String loginId
    );

    @Operation(
            summary = "사용자 정보 조회",
            description = "특정 사용자 ID의 존재 여부를 확인합니다. 모든 사용자가 접근 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 조회 성공"
            )
    })
    ResponseEntity<?> getUser(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable String userId
    );

    @Operation(
            summary = "사용자 정보 수정",
            description = "로그인한 사용자의 프로필 정보를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "정보 수정 성공"
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    ResponseEntity<?> updateUser(
            @Parameter(description = "JWT 액세스 토큰", required = true)
            @RequestHeader("Authorization") String accessToken,
            @RequestBody UserRequest.UpdateDTO userRequest
    );

    @Operation(
            summary = "회원 탈퇴",
            description = "로그인한 사용자의 계정을 삭제합니다. 관련된 모든 데이터(회의실, 회의 이력 등)가 함께 삭제됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    ResponseEntity<?> deleteUser(
            @Parameter(description = "JWT 액세스 토큰", required = true)
            @RequestHeader("Authorization") String accessToken
    );

}