package kr.sparta.livechat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.sparta.livechat.dto.message.GetChatMessageListResponse;
import kr.sparta.livechat.security.CustomUserDetails;
import kr.sparta.livechat.service.MessageService;
import lombok.RequiredArgsConstructor;

/**
 * 채팅 메시지 조회 API 요청을 처리하는 컨트롤러 클래스입니다.
 * <p>
 * 채팅방 메시지 목록 조회는 커서(cursor) 기반 페이징으로 제공되며, 인증된 사용자만 접근할 수 있습니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 23.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms/{chatRoomId}")
public class MessageController {

	private final MessageService messageService;

	/**
	 * 특정 채팅방의 메시지 목록을 커서 기반으로 조회합니다.
	 * <p>
	 * 요청한 사용자가 해당 채팅방 참여자가 아닌 경우 조회가 제한됩니다.
	 * </p>
	 *
	 * @param userDetails 인증된 사용자 정보
	 * @param chatRoomId  채팅방 식별자
	 * @param cursor      조회 시작 커서(없으면 최신부터 조회)
	 * @param size        조회 개수
	 * @return 메시지 목록 응답 DTO
	 */
	@GetMapping("/messages")
	public ResponseEntity<GetChatMessageListResponse> getChatMessageList(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long chatRoomId,
		@RequestParam(required = false) Long cursor,
		@RequestParam(required = false) Integer size
	) {
		GetChatMessageListResponse response = messageService.getMessageList(
			chatRoomId,
			cursor,
			size,
			userDetails.getUserId()
		);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}

