package com.dm.DM_Backend.global.exception;

import lombok.Getter;

@Getter
public enum ReturnCode {
    SUCCESS(000, "SUECCESS_001", "요청에 성공하였습니다."),
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),

    // Page 관련 에러
    PAGE_REQUEST_FAIL(401, "PAGE_001", "적절하지 않은 페이지 요청입니다."),

    // Auth 관련 에러 (400번대)
    INVALID_AUTH_TOKEN(401, "AUTH_001", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(401, "AUTH_002", "만료된 토큰입니다."),
    SOCIAL_ONLY_ACCOUNT(401, "AUTH_003", "소셜 로그인으로만 가입된 계정입니다. 비밀번호를 먼저 설정해주세요."),
    INVALID_PASSWORD(401, "AUTH_004", "비밀번호가 일치하지 않습니다."),
    INVALID_EMAIL(404, "AUTH_005", "해당 이메일로 가입된 정보가 없습니다."),
    UNAUTHORIZED(401, "AUTH_006", "인증된 사용자 정보가 없습니다."),
    REDIRECT_URL_NOT_FOUND(400, "AUTH_007", "리다이렉트 URL을 찾을 수 없습니다."),
    PASSWORD_REQUIRED(400, "AUTH_008", "비밀번호는 필수 입력값입니다."),
    EMAIL_REQUIRED(400, "AUTH_009", "이메일은 필수 입력값입니다."),
    CURRENT_PASSWORD_NOT_MATCH(400, "AUTH_010", "현재 비밀번호가 일치하지 않습니다."),
    PASSWORD_CHANGE_FAILED(500, "AUTH_011", "비밀번호 변경에 실패했습니다."),
    EMAIL_NOT_VERIFIED(400, "AUTH_012", "이메일 인증을 완료 후 회원가입 진행해주세요."),
    SOCIAL_ACCOUNT_ALREADY_IN_USE(400, "AUTH_013", "이미 사용중인 소셜 계정입니다."),

    // User 관련 에러
    USER_NOT_FOUND(404, "USER_001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(409, "USER_002", "이미 사용중인 이메일입니다."),
    INVALID_LOGIN_TYPE(400, "USER_003", "지원하지 않는 로그인 타입입니다."),
    INVALID_ROLE(400, "USER_004", "권한이 없습니다."),
    PASSWORD_RESET_NOT_VERIFIED(400, "USER_005", "비밀번호 재설정 인증이 필요합니다."),
    INVALID_PHONE_NUMBER(400, "USER_006", "해당 전화번호로 등록된 계정을 찾을 수 없습니다."),
    ALREADY_CONNECTED_SOCIAL_ACCOUNT(400, "USER_007", "이미 연결된 소셜 계정입니다."),
    NOT_CONNECTED_SOCIAL_ACCOUNT(400, "USER_008", "연결되지 않은 소셜 계정입니다."),
    ALREADY_HAS_PASSWORD(400, "USER_009", "이미 비밀번호가 설정되어 있습니다."),
    CANNOT_DISCONNECT_LAST_LOGIN_METHOD(400, "USER_010", "마지막 로그인 수단은 연결을 해제할 수 없습니다."),
    DISCONNECT_FAIL(500, "USER_011", "소셜 계정 연동 해제에 실패했습니다."),
    INVALID_SOCIAL_CONNECTION(400, "USER_012", "기존 정보를 찾지 못하여 소셜 연동이 실패했습니다."),
    PASSWORD_VERIFICATION_REQUIRED(400, "USER_013", "비밀번호 확인이 필요합니다."),
    INVALID_VERIFICATION_CODE(400, "USER_014", "인증코드가 올바르지 않습니다."),
    INVALID_RESET_TOKEN(400, "USER_014", "비밀번호 재설정 토큰이 올바르지 않습니다."),

    // Report 관련 에러
    REPORT_NOT_FOUND(404, "REPORT_001", "해당 신고를 찾을 수 없습니다."),
    REPORTER_NOT_FOUND(404, "REPORT_002", "신고자를 찾을 수 없습니다."),
    REPORTED_NOT_FOUND(404, "REPORT_003", "신고 대상자를 찾을 수 없습니다."),
    REPORTER_SELF_REPORT(400, "REPORT_004", "자기 자신을 신고할 수 없습니다."),

    // Storage 관련 에러
    FILE_NOT_FOUND(404, "STORAGE_001", "파일을 찾을 수 없습니다."),
    FILE_UPLOAD_ERROR(500, "STORAGE_002", "파일 업로드에 실패했습니다."),
    FILE_DELETE_ERROR(500, "STORAGE_003", "파일 삭제에 실패했습니다."),

    // Email 관련 에러
    EMAIL_SEND_FAIL(500, "EMAIL_001", "이메일 전송에 실패했습니다."),
    EMAIL_VERIFICATION_NOT_FOUND(404, "EMAIL_002", "인증 코드가 존재하지 않습니다."),
    EMAIL_VERIFICATION_EXPIRED(400, "EMAIL_003", "만료된 인증 코드입니다."),
    EMAIL_VERIFICATION_NOT_MATCH(400, "EMAIL_004", "인증 코드가 일치하지 않습니다."),

    // SMS 관련 에러
    SMS_SEND_FAILED(500, "SMS_001", "SMS 발송에 실패했습니다."),
    SMS_CODE_EXPIRED(400, "SMS_002", "만료된 인증번호입니다."),
    SMS_RESEND_TOO_EARLY(400, "SMS_003", "재전송 대기 시간이 아직 남았습니다."),
    SMS_DAILY_LIMIT_EXCEEDED(400, "SMS_004", "하루 최대 전송 횟수를 초과했습니다."),
    INVALID_SNS_VERIFICATION_CODE(400, "SMS_005", "인증번호가 일치하지 않습니다."),
    PHONE_NUMBER_NOT_VERIFIED(400, "SMS_006", "휴대폰 번호는 인증을 완료 후 추가할 수 있습니다."),
    ALREADY_VERIFIED_PHONE_NUMBER(400, "SMS_007", "이미 인증된 휴대폰 번호입니다."),

    // ChatRoom 관련 에러
    CHATROOM_NOT_FOUND(404, "CHATROOM_001", "채팅방을 찾을 수 없습니다."),
    CHATROOM_ALREADY_EXISTS(404, "CHATROOM_002", "이미 존재하는 채팅방입니다."),
    CHATROOM_LIMIT_EXCEEDED(404, "CHATROOM_003", "채팅방 참여자 수가 초과했습니다."),
    MEMBER_ALREADY_EXISTS(404, "CHATROOM_004", "이미 채팅방에 존재하는 사용자입니다."),
    INVALID_KICK_MEMBER(404, "CHATROOM_005", "강퇴할 수 없는 사용자입니다."),
    INVALID_DELEGATE_MEMBER(404, "CHATROOM_006", "위임할 수 없는 사용자입니다."),

    // ChatRoomMember 관련 에러
    CHATROOM_MEMBER_NOT_FOUND(404, "CHATROOM_001", "채팅방을 찾을 수 없습니다."),

    // ChatMessage 관련 에러
    CHATMESSAGE_NOT_FOUND(404, "CHATMESSAGE_001", "채팅 메시지를 찾을 수 없습니다."),

    // Post 관련 에러
    POST_NOT_FOUND(404, "POST_001", "게시글을 찾을 수 없습니다."),
    POST_ALREADY_SAVED(404, "POST_002", "이미 저장한 게시글입니다."),
    POST_ALREADY_LIKED(404, "POST_003", "이미 좋아요를 누른 게시글입니다."),

    // Comment 관련 에러
    COMMENT_NOT_FOUND(404, "COMMENT_001", "댓글을 찾을 수 없습니다."),
    COMMENT_ALREADY_LIKED(404, "COMMENT_002", "이미 좋아요를 누른 댓글입니다."),

    // Friend 관련 에러
    FRIEND_LIMIT_EXCEEDED(400, "FRIEND_002", "친구 수가 초과되었습니다."),
    FRIEND_REQUEST_NOT_FOUND(404, "FRIEND_003", "친구 요청을 찾을 수 없습니다."),
//    ALREADY_REQUESTED(400, "FRIEND_004", "이미 친구 요청을 보냈습니다."),
    CANNOT_FRIEND_SELF(400, "FRIEND_005", "자기 자신을 친구로 추가할 수 없습니다."),
    BLOCKED_BY_USER(400, "FRIEND_006", "사용자에 의해 차단되었습니다."),
    REQUESTER_NOT_FOUND(404, "FRIEND_007", "요청자를 찾을 수 없습니다."),
    RECEIVER_NOT_FOUND(404, "FRIEND_008", "수신자를 찾을 수 없습니다."),
    UNAUTHORIZED_ACCESS(401, "FRIEND_009", "승낙/취소할 권한이 없습니다."),
    ALREADY_HANDLED(400, "FRIEND_010", "이미 수락된 요청입니다."),
    ALREADY_FRIEND(400, "FRIEND_011", "이미 친구입니다."),
    ALREADY_RECEIVED_REQUEST(400, "FRIEND_012", "이미 친구 요청을 받았습니다."),
    INVALID_USER_STATE(400, "FRIEND_013", "상대가 유효하지 않은 회원 상태입니다."),
    NOT_FRIEND(400, "FRIEND_014", "친구가 아닙니다."),
    FRIEND_NOT_FOUND(404, "FRIEND_015", "해당 정보를 찾을 수 없습니다."),

    // Follow 관련 에러
    ALREADY_REQUESTED(404, "FOLLOW_001", "이미 팔로우 요청을 보냈습니다."),
    REQUEST_NOT_FOUND(404, "FOLLOW_002", "팔로우 요청을 찾을 수 없습니다."),
    ALREADY_FOLLOW(404, "FOLLOW_003", "이미 팔로우 중인 사용자입니다."),
    FOLLOWER_NOT_FOUND(404, "FOLLOW_004", "팔로워를 찾을 수 없습니다."),
    FOLLOW_NOT_FOUND(404, "FOLLOW_005", "팔로우한 사용자 찾을 수 없습니다."),

    //board 관련 에러
    BOARD_NOT_FOUND(400,"BOARD_001","게시글을 찾을 수 없습니다."),

    //boardComment 관련 에러
    BOARDCOMMENT_NOT_FOUND(400,"BOARDCOMMENT_001","게시글의 댓글을 찾을 수 없습니다."),

    // 알림 관련 에러
    NOTIFICATION_NOT_FOUND(404, "NOTIFICATION_001", "해당 알림을 찾을 수 없습니다."),
    MAP_TO_JSON_FAILED(500, "NOTIFICATION_002", "맵을 JSON으로 변환하는데 실패했습니다."),
    NOT_AUTHORIZED(401, "NOTIFICATION_003", "권한이 없습니다."),
    NAVI_VALIDATE_ERROR(400, "NOTIFICATION_004", "navigationData에 필수 키가 없습니다"),
    ARGS_VALIDATE_ERROR(400, "NOTIFICATION_005", "메시지 인자가 부족합니다."),

    // Business 에러 (500번대)
    INTERNAL_ERROR(500, "SYS_001", "내부 시스템 에러"),
    API_CALL_FAILED(500, "SYS_002", "외부 API 호출에 실패했습니다."),

    // 포스트 엘라스틱 서치 에러 (
    DATA_CONVERSION_ERROR(500, "SEARCH_001", "데이터 변환 중 오류가 발생했습니다"),
    SEARCH_POST_ERROR(500, "SEARCH_002", "엘라스틱 서치 검색 중 에러 발생");

    private final int status;
    private final String code;
    private final String message;


    ReturnCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
