package kr.sparta.livechat.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.UserRepository;
import kr.sparta.livechat.service.AuthService;
import kr.sparta.livechat.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP 요청에 대해 JWT 토큰의 유효성을 검증하고
 * 유효할 경우 인증 정보를 Spring Security Context에 저장하는 필터입니다.
 * JwtAuthenticationFilter.java
 *
 * @author kimsehyun
 * @since 2025. 12. 16.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final AuthService authService;
	private final UserRepository userRepository;

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String AUTHORIZATION_HEADER = "Authorization";

	/**
	 * 필터 체인의 로직을 수행합니다.
	 * @param request HTTP 요청
	 * @param response HTTP 응답
	 * @param filterChain 다음 필터로 요청 전달하는 체인
	 * @throws ServletException 서블릿 관련 예외 발생시
	 * @throws IOException 입출력 예외 발생시
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		String requestURI = request.getRequestURI();

		if (requestURI.equals("/api/auth/register") || requestURI.equals("/api/auth/login")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = resolveToken(request);

		if (token != null) {
			if (authService.isTokenBlacklisted(token)) {
				log.warn("블랙리스트에 등록된 토큰으로 접근 시도: {}", token);
				throw new CustomException(ErrorCode.AUTH_TOKEN_BLACKLISTED);
			}

			if (jwtService.validateToken(token)) {
				Long userId = jwtService.getUserIdFromToken(token);

				if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					User user = userRepository.findById(userId)
						.orElseThrow(() -> {
							log.warn("JWT userId에 해당하는 사용자가 DB에 존재하지 않음: {}", userId);
							return new CustomException(ErrorCode.AUTH_USER_NOT_FOUND);
						});

					UserDetails userDetails = new CustomUserDetails(user);
					Authentication authentication = new UsernamePasswordAuthenticationToken(
						userDetails,
						null,
						userDetails.getAuthorities()
					);
					SecurityContextHolder.getContext().setAuthentication(authentication);
					log.debug("인증 성공: 사용자 ID {}", userId);
				}
			}
		}
		filterChain.doFilter(request, response);
	}

	/**
	 * HTTP Header에서 JWT 토큰을 추출하는 메서드 입니다.
	 * @param request 현재 HTTP 요청
	 * @return 추출된 JWT 토큰 문자열, 없으면 null 반환
	 */
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
		if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
			return bearerToken.substring(BEARER_PREFIX.length());
		}

		return null;
	}
}
