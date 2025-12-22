package kr.sparta.livechat.global.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 애플리케이션 전역에서 발생할 수 있는 오류 상황을 정의한 Enum 클래스입니다.
 * <p>
 * 각 ErrorCode는 HTTP 상태코드 {@link HttpStatus}, 서비스 고유 에러코드 문자열, 클라이언트에게 전달될 메시지를 포함합니다.
 * GlobalExceptionHandler에서 발생한 예외를 일관된 응답 형식으로 변환할 때 사용합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 11.
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

	// 공통으로 사용하는 에러코드
	COMMON_BAD_PAGINATION(
		HttpStatus.BAD_REQUEST, "COMMON_BAD_PAGINATION", "page 또는 size값이 유효하지 않습니다."),
	COMMON_INTERNAL_ERROR(
		HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버에 문제가 있습니다."),

	// 인증/인가에서 사용하는 에러코드
	AUTH_INVALID_TOKEN_FORMAT(
		HttpStatus.BAD_REQUEST, "AUTH_INVALID_TOKEN_FORMAT", "토큰 형식이 유효하지 않거나 잘못된 서명입니다."),
	AUTH_TOKEN_EXPIRED(
		HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_EXPIRED", "만료된 JWT 토큰입니다. 재로그인이 필요합니다."),
	AUTH_TOKEN_UNSUPPORTED(
		HttpStatus.BAD_REQUEST, "AUTH_TOKEN_UNSUPPORTED", "지원되지 않는 JWT 토큰입니다."),
	AUTH_DUPLICATE_EMAIL(
		HttpStatus.CONFLICT, "AUTH_DUPLICATE_EMAIL", "이미 사용 중인 이메일 주소입니다."),
	AUTH_FORBIDDEN_ROLE(
		HttpStatus.FORBIDDEN, "AUTH_FORBIDDEN_ROLE", "ADMIN 역할은 일반 회원가입을 통해 등록할 수 없습니다."),
	AUTH_USER_NOT_FOUND(
		HttpStatus.NOT_FOUND, "AUTH_USER_NOT_FOUND", "사용자 정보가 일치하지 않습니다."),
	AUTH_INVALID_CREDENTIALS(
		HttpStatus.UNAUTHORIZED, "AUTH_MISMATCH", "이메일 또는 비밀번호가 일치하지 않습니다."),
	AUTH_TOKEN_BLACKLISTED(
		HttpStatus.FORBIDDEN, "AUTH_TOKEN_BLACKLISTED", "이미 로그아웃 처리된 토큰입니다."),

	// 상품 관리에서 사용할 에러코드
	PRODUCT_INVALID_INPUT(
		HttpStatus.BAD_REQUEST, "PRODUCT_INVALID_INPUT", "입력한 데이터가 양식에 맞지 않습니다."),
	PRODUCT_ACCESS_DENIED(
		HttpStatus.FORBIDDEN, "PRODUCT_ACCESS_DENIED", "해당 상품에 관한 권한이 없습니다."),
	PRODUCT_NOT_FOUND(
		HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "해당 상품을 찾을 수 없습니다."),
	PRODUCT_ALREADY_EXISTS(
		HttpStatus.CONFLICT, "PRODUCT_ALREADY_EXISTS", "해당 상품은 이미 등록되었습니다."),
	PRODUCT_ALREADY_DELETED(
		HttpStatus.CONFLICT, "PRODUCT_ALREADY_DELETED", "해당 상품은 이미 삭제되었습니다."),

	// 채팅방 관리에서 사용할 에러코드
	CHATROOM_INVALID_STATUS(
		HttpStatus.BAD_REQUEST, "CHATROOM_INVALD_STATUS", "요청한 채팅방 상태값이 유효하지 않습니다."),
	CONTENT_MUST_NOT_BLANK(
		HttpStatus.BAD_REQUEST, "CONTENT_MUST_NOT_BLANK", "상담 메시지를 입력해주세요."),
	CHATROOM_ACCESS_DENIED(
		HttpStatus.FORBIDDEN, "CHATROOM_ACCESS_DENIED", "채팅방에 대한 권한이 없습니다."),
	CHATROOM_CREATE_ACCESS_DENIED(
		HttpStatus.FORBIDDEN, "CHATROOM_CREATE_ACCESS_DENIED", "채팅방 생성에 관한 권한이 없습니다."),
	CHATROOM_NOT_FOUND(
		HttpStatus.NOT_FOUND, "CHATROOM_NOT_FOUND", "해당 채팅방을 찾을 수 없습니다."),
	PRODUCT_SELLER_NOT_FOUND(
		HttpStatus.NOT_FOUND, "PRODUCT_SELLER_NOT_FOUND", "판매자 정보를 조회할 수 없습니다."),
	CHATROOM_ALREADY_EXISTS(
		HttpStatus.CONFLICT, "CHATROOM_ALREADY_EXISTS", "해당 상품에 대한 상담방이 이미 존재합니다."),
	CHATROOM_ALREADY_CLOSED(
		HttpStatus.CONFLICT, "CHATROOM_ALREADY_CLOSED", "이미 종료된 채팅방입니다."),
	PRODUCT_NOT_AVAILABLE_FOR_CHAT(
		HttpStatus.CONFLICT, "PRODCUT_NOT_AVAILABLE_FOR_CHAT", "판매중인 상품만 상담방을 생성할 수 있습니다."),

	// 프로필 이미지 수정에서 사용할 에러코드
	PROFILE_INVALID_FORMAT(
		HttpStatus.BAD_REQUEST, "PROFILE_INVALID_FORMAT", "이미지 파일만 업로드 가능합니다.(jpg, jpeg, png, gif)"),
	PROFILE_SIZE_EXCEEDED(
		HttpStatus.BAD_REQUEST, "PROFILE_SIZE_EXCEEDED", "파일 크기는 5MB를 초과할 수 없습니다."),
	PROFILE_INVALID_DATA(
		HttpStatus.BAD_REQUEST, "PROFILE_INVALID_DATA", "프로필 수정 요청 데이터가 유효하지 않습니다."),
	PROFILE_UPDATE_FORBIDDEN(
		HttpStatus.FORBIDDEN, "PROFILE_UPDATE_FORBIDDEN", "본인의 프로필만 수정할 수 있습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

}
