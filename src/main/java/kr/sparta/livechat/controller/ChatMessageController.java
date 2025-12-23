package kr.sparta.livechat.controller;

import kr.sparta.livechat.dto.socket.MessageSendRequest;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.service.ChatMessageService;
import kr.sparta.livechat.socket.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * {@code /pub/chat/message} 경로로 STOMP SEND 프레임을 전송하면 처리하는 Controller 클래스입니다.
 *
 * WebSocket 연결 시 설정된 인증 정보를 기반으로 사용자 식별하고
 * 메시지 전송 요청을 {@link ChatMessageController} 처리하도록 합니다.
 *
 * @author 오정빈
 * @since 2025. 12. 22.
 */
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

	private final ChatMessageService chatMessageService;

	/**
	 * {@code /pub/chat/message}로 전송한 STOMP 메세지를 수신하고
	 * 인증된 사용자만 메시지를 전송할 수 있습니다.
	 * 인증 정보는 {@link Principal} 형태로 전달됩니다.
	 */
	@MessageMapping("/chat/message")
	public void send(MessageSendRequest request, Principal principal) {
		if (!(principal instanceof CustomPrincipal customPrincipal)) {
			throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN_FORMAT);
		}

		Long writerId = customPrincipal.getUserId();
		chatMessageService.sendMessage(writerId, request);
	}
}


