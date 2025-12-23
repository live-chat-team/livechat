	package kr.sparta.livechat.socket;

	import kr.sparta.livechat.global.exception.WsErrorCode;
	import lombok.RequiredArgsConstructor;

	import java.security.Principal;

	/**
	 * STOMP에서 인증된 사용자를 식별하기 위한 Principal 구현체입니다.
	 * <p>
	 *   STOMP CONNECT 단계에서 토큰 검증이 완료되면  Principal을 세션에 주입합니다.
	 * </p>
	 *
	 * @author 오정빈
	 * @version 1.0
	 * @since 2025. 12. 18.
	 */
	@RequiredArgsConstructor
	public class CustomPrincipal implements Principal {

		private final Long userId;

		@Override
		public String getName() {
			return String.valueOf(userId);
		}

		public Long getUserId() {
			return userId;
		}
	}
