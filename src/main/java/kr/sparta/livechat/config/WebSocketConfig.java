package kr.sparta.livechat.config;

import kr.sparta.livechat.global.exception.GlobalStompErrorHandler;
import kr.sparta.livechat.socket.StompChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * 웹소켓 설정을 담당하는 Configuration 클래스입니다.
 * 클라이언트가 WebSocke 연결을 시도하는 엔드포인트를 등록합니다. (/ws/chat)
 * /pub: 클라이언트 -> 서버로 보내는 목적지 prefix
 * /sub: 서버 -> 클라이언트로 보내는 목적지 prefix
 *
 * @author 오정빈
 * @since 2025. 12. 17.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final StompChannelInterceptor stompCannelInterceptor;
	private final GlobalStompErrorHandler globalStompErrorHandler;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws/chat")
			.setAllowedOriginPatterns("*");
		registry.setErrorHandler(globalStompErrorHandler);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/pub");
		registry.enableSimpleBroker("/sub");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompCannelInterceptor);
	}

}
