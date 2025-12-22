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

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

	private final ChatMessageService chatMessageService;

	@MessageMapping("/chat/message")
	public void send(MessageSendRequest request, Principal principal) {
		if (!(principal instanceof CustomPrincipal customPrincipal)) {
			throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN_FORMAT);
		}

		Long writerId = customPrincipal.getUserId();
		chatMessageService.sendMessage(writerId, request);
	}
}


