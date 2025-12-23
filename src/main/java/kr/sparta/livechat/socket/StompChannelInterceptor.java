package kr.sparta.livechat.socket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.service.JwtService;
import kr.sparta.livechat.service.SocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * 클라이언트 -> 서버의 STOMP를 처리 하기 전에 실행되는 채널 인터셉터입니다.
 * <p>
 *   {@code CONNECT} 프레임에서 Authorization 헤더를 검사하여 JWT를 검증하고
 *   검증 성공 시 WebSocket 세션에 사용자 {@code Principal}을 등록합니다.
 *
 *  토큰 형식이 올바르지 않으면 {@link CustomException}을 발생시키고
 *  {@link kr.sparta.livechat.global.exception.GlobalExceptionHandler}에서
 *  {@Code mapToWsErrorCode}가 WsErrorCode로 변환합니다.
 *
 *  {@code Bearer }를 제거한 뒤 토큰 유효성을 검증하고
 *  토큰에서 {@code userId}를 추출하여 {@link CustomPrincipal}을 생성합니다.
 *
 *  Interceptor에서 발생한 예외는 {@link kr.sparta.livechat.global.exception.GlobalStompErrorHandler}
 *  통해 처리됩니다.
 * </p>
 *
 * @author 오정빈
 * @version 1.0
 * @since 2025. 12. 18.
 */
@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

	private static final Pattern ROOM_SUBSCRIBE_PATTERN =
		Pattern.compile("^/sub/chat/room/(?<roomId>\\d+)$");
	private static final Pattern ROOM_SYSTEM_SUBSCRIBE_PATTERN =
		Pattern.compile("^/sub/chat/room/(?<roomId>\\d+)/system$");

	private final SocketService socketService;
	private final JwtService jwtService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {

		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {

			String token = accessor.getFirstNativeHeader("Authorization");

			if (token == null) {
				throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN_FORMAT);
			}

			token = token.replace("Bearer ", "");

			jwtService.validateToken(token);

			Long userId = jwtService.getUserIdFromToken(token);
			accessor.setUser(new CustomPrincipal(userId));

			return message;
		}

		if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

			String destination = accessor.getDestination();
			if (destination == null) {
				return message;
			}

			Matcher roomMatcher = ROOM_SUBSCRIBE_PATTERN.matcher(destination);
			Matcher systemMatcher = ROOM_SYSTEM_SUBSCRIBE_PATTERN.matcher(destination);

			// /sub/chat/room/{roomId} 또는 /sub/chat/room/{roomId}/system 만 허용
			if (!roomMatcher.matches() && !systemMatcher.matches()) {
				return message;
			}

			Long roomId;
			if (roomMatcher.matches()) {
				roomId = Long.parseLong(roomMatcher.group("roomId"));
			} else {
				roomId = Long.parseLong(systemMatcher.group("roomId"));
			}

			if (!(accessor.getUser() instanceof CustomPrincipal principal)) {
				throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN_FORMAT);
			}

			Long userId = principal.getUserId();

			if (!socketService.isParticipant(roomId, userId)) {
				throw new CustomException(ErrorCode.CHATROOM_ACCESS_DENIED);
			}
		}
		return message;
	}
}
