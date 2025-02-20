package com.ssafy.exhi.base.status;

import com.ssafy.exhi.base.code.BaseErrorCode;
import com.ssafy.exhi.base.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "권한이 없습니다. id를 맞게 선택하셨나요?"),

    // 입력 관련 에러
    NOT_ENOUGH_REQUEST_BODY_ERROR(HttpStatus.BAD_REQUEST, "COMMON404", "요청 바디가 잘못되었습니다."),

    // 멤버 관련 에러5r
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4001", "존재하지않는 계정입니다."),
    USER_NICKNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "MEMBER4002", "닉네임은 필수 입니다."),
    USER_DUPLICATED_LOGIN_ID(HttpStatus.BAD_REQUEST, "MEMBER4003", "아이디가 중복되었습니다."),
    USER_DUPLICATED_NICKNAME(HttpStatus.BAD_REQUEST, "MEMBER4004", "닉네임이 중복되었습니다."),
    USER_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "MEMBER4005", "잘못된 비밀번호입니다."),
    USER_BAD_REQUEST(HttpStatus.BAD_REQUEST, "MEMBER4006", "토큰 값과 요청한 아이디가 일치하지않습니다."),
    INVALID_USER(HttpStatus.BAD_REQUEST, "MEMBER4007", "멤버 값을 잘 확인해주세요 (온보딩, 성별 등)"),

    // 로컬멤버 기본정보
    USER_INFO_NOT_FOUND(HttpStatus.BAD_REQUEST, "USERINFO4001", "유저 기본정보가 존재하지않습니다."),
    USER_INFO_NO_CHANGES(HttpStatus.BAD_REQUEST, "USERINFO4002", "변경된 사항이 없습니다."),

    // 멤버 디테일
    USER_DETAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "USERDETAIL4001", "유저 디테일이 존재하지않습니다."),
    USER_DETAIL_NO_CHANGES(HttpStatus.BAD_REQUEST, "USERDETAIL4002", "변경된 정보가 없습니다."),

    // 멤버 디테일(온보딩) 정보가 이미 있는 경우
    USER_DETAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MEMBER_DETAIL4004", "유저 디테일이 이미 존재합니다. 생성이 아닌 수정을 이용해주세요."),

    // 커플 관련 에러
    COUPLE_NOT_FOUND(HttpStatus.NOT_FOUND, "COUPLE4001", "존재하지 않는 커플입니다."),
    COUPLE_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "COUPLE4002", "이미 커플 관계가 존재합니다."),
    COUPLE_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "COUPLE4003", "존재하지 않는 커플 요청입니다."),
    COUPLE_REQUEST_ALREADY_SENT(HttpStatus.BAD_REQUEST, "COUPLE4004", "이미 커플 요청을 보냈습니다."),
    COUPLE_REQUEST_TO_SELF(HttpStatus.BAD_REQUEST, "COUPLE4005", "자신에게 커플 요청을 보낼 수 없습니다."),
    COUPLE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COUPLE4006", "커플 관계가 아닙니다."),
    COUPLE_SAME_GENDERED(HttpStatus.BAD_REQUEST, "COUPLE4007", "동성간의 연애는 지원하지 않습니다."),

    // 버진로드 관련 에러
    VIRGIN_ROAD_NOT_FOUND(HttpStatus.NOT_FOUND, "VIRGIN_ROAD4001", "존재하지 않는 플래너입니다."),
    VIRGIN_ROAD_DATE_INVALID(HttpStatus.BAD_REQUEST, "VIRGIN_ROAD4002", "잘못된 날짜 형식입니다."),
    VIRGIN_ROAD_TITLE_NOT_EXIST(HttpStatus.BAD_REQUEST, "VIRGIN_ROAD4003", "제목은 필수입니다."),
    VIRGIN_ROAD_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "VIRGIN_ROAD4004", "플래너에 대한 권한이 없습니다."),
    VIRGIN_ROAD_DUPLICATE_DATE(HttpStatus.BAD_REQUEST, "VIRGIN_ROAD4005", "해당 날짜에 이미 플래너가 존재합니다."),
    VIRGIN_ROAD_DATE_PAST(HttpStatus.BAD_REQUEST, "VIRGIN_ROAD4006", "과거 날짜는 선택할 수 없습니다."),
    VIRGIN_ROAD_EXISTS(HttpStatus.BAD_REQUEST, "VIRGIN_ROAD4007", "버진로드는 두 개 이상 생성할 수 없습니다."),
    VIRGIN_ROAD_SERVICE_TYPE_NOT_VALID(HttpStatus.BAD_REQUEST, "VIRGIN_ROAD4008", "버진로드의 서비스 타입이 중복됩니다."),

    // 투두 관련 에러
    TODO_NOT_FOUND(HttpStatus.NOT_FOUND, "TODO4001", "존재하지 않는 투두 계획입니다."),

    // 숍 관련 에러
    SHOP_NOT_FOUND(HttpStatus.NOT_FOUND, "TODO4001", "존재하지 않는 숍ID입니다."),

    // 알림 관련 에러
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION4001", "존재하지 않는 알람입니다."),
    NOTIFICATION_STATUS_ERROR(HttpStatus.NOT_FOUND, "NOTIFICATION4002", "종료된 알림입니다. 수정이 불가능 합니다."),

    // 공지 사항 관련 에러
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE4001", "존재하지 않는 공지입니다.."),

    // SSE 관련 에러
    SSR_SUBSCRIBE_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBSCRIBE4001", "구독 상태를 확인해주세요"),

    // 토큰 관련 에러
    TOKEN_EXPIRED_ERROR(HttpStatus.UNAUTHORIZED, "TOKEN4001", "토큰의 유효기간이 만료되었습니다."),
    TOKEN_MALFORMED_ERROR(HttpStatus.UNAUTHORIZED, "TOKEN4002", "토큰이 변형되었습니다."),
    TOKEN_UNSUPPORTED_ERROR(HttpStatus.BAD_REQUEST, "TOKEN4003", "지원되지 않는 형식의 토큰입니다."),
    TOKEN_SIGNATURE_ERROR(HttpStatus.UNAUTHORIZED, "TOKEN4004", "토큰의 서명이 유효하지 않습니다."),
    TOKEN_ILLEGAL_ARGUMENT_ERROR(HttpStatus.BAD_REQUEST, "TOKEN4005", "토큰 값이 잘못되었습니다."),
    TOKEN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "TOKEN5000", "토큰 처리 중 알 수 없는 에러가 발생했습니다."),
    TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "TOKEN4006", "유저의 토큰정보를 얻어올 수 없습니다. 헤더를 확인하세요"),

    // 데이터베이스 관련 에러
    DATABASE_DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "DB5001", "중복된 리소스가 존재합니다"),
    DATABASE_INVALID_FOREIGN_KEY(HttpStatus.BAD_REQUEST, "DB5002", "잘못된 외래 키 참조입니다"),
    DATABASE_CONNECTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "DB5003", "데이터베이스 연결 오류가 발생했습니다"),
    DATABASE_INVALID_COLUMN_REFERENCE(HttpStatus.BAD_REQUEST, "DB5004", "잘못된 컬럼을 참조했습니다"),
    DATABASE_TABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "DB5005", "테이블을 찾을 수 없습니다"),
    DATABASE_DATA_TRUNCATION_ERROR(HttpStatus.BAD_REQUEST, "DB5006", "데이터 길이가 초과되었거나 타입이 일치하지 않습니다"),
    DATABASE_DEADLOCK_OCCURRED(HttpStatus.CONFLICT, "DB5007", "데드락이 발생했습니다"),
    DATABASE_LOCK_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "DB5008", "락 획득 대기 시간이 초과되었습니다"),
    DATABASE_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB5999", "내부 서버 오류가 발생했습니다"),

    // 플랜 관련 에러
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAN_4001", "존재하지 않는 플랜입니다"),
    INVALID_PLAN_STATUS(HttpStatus.BAD_REQUEST, "PLAN_4002", "아직 타임캡슐을 작성할 수 없는 플랜입니다"),

    // 타임캡슐 관련 에러
    TIMECAPSULE_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "TIME_CAPSULE_4001", "이미 작성된 타임캡슐입니다"),
    TIMECAPSULE_VALIDATE(HttpStatus.BAD_REQUEST, "TIME_CAPSULE_4002", "타임캡슐 작성 조건이 달성되지않았습니다."),

    // 추천(클러스터링) 시스템 관련 에러
    // 1. 클러스터링 관련 오류
    CLUSTERING_FILE_IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CLUSTER4001", "클러스터링 중 파일 입출력 오류가 발생했습니다"),
    CLUSTERING_SCRIPT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CLUSTER4002", "클러스터링 스크립트 실행 오류가 발생했습니다"),
    CLUSTERING_SCRIPT_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "CLUSTER4003", "클러스터링 스크립트 실행 시간이 초과되었습니다"),
    CLUSTERING_PARAMS_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUSTER4004", "클러스터링 파라미터를 찾을 수 없습니다"),
    CLUSTERING_CENTERS_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUSTER4005", "클러스터 중심 정보를 찾을 수 없습니다"),
    CLUSTERING_CLUSTER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CLUSTER4006", "클러스터링 처리 중 오류가 발생했습니다"),
    CLUSTERING_CLUSTER_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUSTER4007", "클러스터 정보를 찾을 수 없습니다"),
    CLUSTERING_USER_DETAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "CLUSTER4008", "사용자 상세정보가 없어 클러스터링이 불가능합니다"),
    CLUSTERING_INVALID_DATA(HttpStatus.BAD_REQUEST, "CLUSTER4009", "클러스터링에 필요한 데이터 형식이 잘못되었습니다"),
    CLUSTERING_NO_DATA(HttpStatus.NOT_FOUND, "CLUSTER4010", "클러스터링에 필요한 데이터가 존재하지 않습니다"),
    // 2. 추천 관련 오류
    RECOMMENDATION_NO_CLUSTER_CENTERS(HttpStatus.NOT_FOUND, "RECOMMEND4001", "클러스터 중심점 정보가 없습니다"),
    RECOMMENDATION_CLUSTER_NOT_FOUND(HttpStatus.NOT_FOUND, "RECOMMEND4002", "사용자의 클러스터 정보를 찾을 수 없습니다"),
    RECOMMENDATION_CLUSTER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RECOMMEND4003", "추천 처리 중 오류가 발생했습니다"),
    RECOMMENDATION_DATA_NOT_READY(HttpStatus.BAD_REQUEST, "RECOMMEND4004", "추천을 위한 데이터가 준비되지 않았습니다"),
    RECOMMENDATION_INVALID_USER(HttpStatus.BAD_REQUEST, "RECOMMEND4005", "추천 대상 사용자 정보가 유효하지 않습니다"),

    // 조언 관련 에러
    ADVICE_API_ERROR(HttpStatus.BAD_REQUEST, "ADVICE4001", "조언 서비스 실행 중 오류가 발생했습니다"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE4001", "파일의 형식이 잘못되었습니다."),
    MAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "MAIL4001", "메일을 찾을 수 없습니다."),
    TEMPLATE_NOT_FOUND(HttpStatus.BAD_REQUEST, "TEMPLATE4001", "템플릿을 찾을 수 없습니다."),
    SHOP_NOT_FOUND_ERROR(HttpStatus.BAD_REQUEST, "SHOP4001", "숍을 찾을 수 없습니다."),
    ITEM_NOT_FOUND_ERROR(HttpStatus.BAD_REQUEST, "ITEM4001", "아이템을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
            .message(message)
            .code(code)
            .isSuccess(false)
            .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
            .message(message)
            .code(code)
            .isSuccess(false)
            .httpStatus(httpStatus)
            .build();
    }
}