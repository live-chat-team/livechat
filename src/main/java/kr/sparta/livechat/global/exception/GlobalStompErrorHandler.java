package kr.sparta.livechat.global.exception;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

/**
 * STOMP 프레임 처리 흐름에서 예외가 발생했을 표준화된 오류 응답을 반환하는 클래스입니다.
 * <p>
 *   예외{@code ex}를 분석하여 {@link WsErrorCode}로 매핑합니다.
 *   {@link WsErrorResponse} 페이로드를 생성합니다.
 *   {@code StompCommand.ERROR} 헤더를 구성하고 content-type을 Json 형식으로 설정합니다.
 *   최종적으로 ERROR 프레임 메시지를 반환합니다.
 * </p>
 *
 * @author 오정빈
 * @version 1.0
 * @since 2025. 12. 18.
 */
@Component
@RequiredArgsConstructor
public class GlobalStompErrorHandler extends StompSubProtocolErrorHandler {

	private final ObjectMapper objectMapper;


	/**
	 * 발생한 예외 {@code ex}를 {@link WsErrorCode}로 변환해서
	 * {@link WsErrorResponse} 및 {@link WsErrorResponse.WsErrorBody}를 생성합니다.
	 * 생성한 응답은 Json 형태로 클라이언트에게 전달됩니다.
	 *
	 * @param clientMessage 예외가 발생한 STOMP 메시지
	 * @param ex STOMP 메시지 처리 중 발생한 예외
	 * @return JSON STOMP ERROR 프레임 메시지
	 */
	@Override
	public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {

		WsErrorCode ws = mapToWsErrorCode(ex);

		WsErrorResponse payload = WsErrorResponse.builder()
			.event("ERROR")
			.message(WsErrorResponse.WsErrorBody.builder()
				.status(ws.getStatus())
				.code(ws.getCode())
				.message(ws.getMessage())
				.timestamp(OffsetDateTime.now())
				.build())
			.build();

		byte[] body;
		try {
			body = objectMapper.writeValueAsBytes(payload);
		} catch (Exception jsonEx) {
			body = ("{\"event\":\"ERROR\",\"message\":{\"status\":4005,\"code\":\"WS_INTERNAL_ERROR\",\"message\":\"서버에 문제가 있습니다.\",\"timestamp\":\""
				+ OffsetDateTime.now() + "\"}}").getBytes(StandardCharsets.UTF_8);
		}

		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
		accessor.setContentType(MimeTypeUtils.APPLICATION_JSON);
		accessor.setLeaveMutable(true);

		accessor.setMessage(ws.getMessage());

		MessageHeaders headers = accessor.getMessageHeaders();
		return MessageBuilder.createMessage(body, headers);
	}

	private WsErrorCode mapToWsErrorCode(Throwable ex) {

		if (ex instanceof CustomException ce) {
			ErrorCode ec = ce.getErrorCode();

			if (ec.name().startsWith("AUTH_")) return WsErrorCode.AUTH_FAILED;

			if (ec == ErrorCode.CHATROOM_ACCESS_DENIED
				|| ec == ErrorCode.PRODUCT_ACCESS_DENIED
				|| ec == ErrorCode.AUTH_FORBIDDEN_ROLE) {
				return WsErrorCode.FORBIDDEN;
			}

			return WsErrorCode.INTERNAL_ERROR;
		}

		return WsErrorCode.INTERNAL_ERROR;
	}
}

