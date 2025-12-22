package kr.sparta.livechat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.sparta.livechat.dto.chatroom.CreateChatRoomRequest;
import kr.sparta.livechat.dto.chatroom.CreateChatRoomResponse;
import kr.sparta.livechat.security.CustomUserDetails;
import kr.sparta.livechat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;

/**
 * ChatRoomController 클래스입니다.
 * <p>
 * 상품에 대한 상담 채팅방 생성 요청을 처리하는 컨트롤러입니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 19.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatRoomController {
	private final ChatRoomService chatRoomService;

	/**
	 * 상품에 대한 상담 채팅방을 생성합니다.
	 * <p>
	 * 구매자만 채팅방을 생성할 수 있으며, 판매중인 상품에 한해서만 상담 채팅방 생성이 가능합니다.
	 * 요청시 채팅방 생성과 함께 구매자/판매자가 참여자로 등록되고, 첫 메시지가 함께 저장됩니다.
	 *
	 * @param productId   상담 대상 상품 식별자
	 * @param request     채팅방 생성 요청 DTO
	 * @param userDetails 인증된 사용자 정보
	 * @return 생성된 채팅방 정보 및 최초 메시지를 포함한 응답 DTO
	 */
	@PostMapping("/products/{productId}/chat-rooms")
	public ResponseEntity<CreateChatRoomResponse> createChatRoom(
		@PathVariable Long productId,
		@Valid @RequestBody CreateChatRoomRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		CreateChatRoomResponse response = chatRoomService.createChatRoom(
			productId,
			userDetails.getUserId(),
			request.getContent()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
