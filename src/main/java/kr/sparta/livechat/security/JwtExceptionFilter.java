package kr.sparta.livechat.security;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.sparta.livechat.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

/**
 * JWT 인증/인가 과정에서 발생하는 예외 처리 필터입니다.
 * JWT 검증 과정에서 발생하는 CustomException 를 잡아냅니다.
 * 잡은 예외는 HandlerExceptionResolver를 통해 GlobalExceptionHandler로 전달되어
 * 오류 응답을 생성하도록 합니다.
 * JwtExceptionFilter.java
 *
 * @author kimsehyun
 * @since 2025. 12. 16.
 */
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

	private final HandlerExceptionResolver handlerExceptionResolver;

	/**
	 * 필터 체인의 로직을 수행합니다.
	 * filterChain.doFilter() 호출시 JwtAuthenticationFilter 에서
	 * CustomException이 발생하면, HandlerExceptionResolver 통해
	 * GlobalExceptionHandler로 전달 합니다.
	 * @param request HTTP 요청
	 * @param response HTTP 응답
	 * @param filterChain 다음 필터로 요청 전달하는 체인
	 * @throws ServletException 서블릿 관련 예외 발생시
	 * @throws IOException 입출력 관련 예외 발생시
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (CustomException e) {
			handlerExceptionResolver.resolveException(request, response, null, e);
		}
	}
}
