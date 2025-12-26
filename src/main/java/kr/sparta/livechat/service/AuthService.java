package kr.sparta.livechat.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.sparta.livechat.dto.user.UserLoginRequest;
import kr.sparta.livechat.dto.user.UserLoginResponse;
import kr.sparta.livechat.dto.user.UserRegisterRequest;
import kr.sparta.livechat.dto.user.UserRegisterResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증 및 회원가입,로그인 , 로그아웃 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 회원가입 요청을 검증하고 비밀번호 암호화, 사용자 엔티티 생성 및 저장,
 * 중복 이메일 체크 등의 주요 로직을 담당합니다. 성공적으로 회원가입이 완료되면
 * 사용자 정보를 담은 응답 DTO를 반환합니다.
 * 비밀번호 암호화, JWT 생성/검증 블랙리스트 토큰 관리 로직을 다른 서비스에 위임합니다.
 * AuthService.java
 *
 * @author kimsehyun
 * @since 2025. 12. 11.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final TokenBlacklistService tokenBlacklistService;

	/**
	 * 회원가입을 처리합니다.
	 * 이메일이 이미 존재할 경우 409 Conflict 예외를 발생시킵니다.
	 * ADMIN으로 가입시 예외 발생
	 * 이메일이 양식에 안맞을경우 에외 발생
	 * 비밀번호가 양식에 안맞을경우 예외 발생
	 *
	 * @param request 회원가입 요청 DTO
	 * @return 회원가입된 사용자 정보를 담은 응답 DTO
	 * @throws CustomException 이메일 중복 시 409 CONFLICT 발생
	 */
	@Transactional
	public UserRegisterResponse registerUser(UserRegisterRequest request) {
		if (request.getRole() == Role.ADMIN) {
			throw new CustomException(ErrorCode.AUTH_FORBIDDEN_ROLE);
		}

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new CustomException(ErrorCode.AUTH_DUPLICATE_EMAIL);
		}

		if (request.getEmail() == null || !request.getEmail().matches
			("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
			throw new CustomException(ErrorCode.AUTH_INVALID_EMAIL_FORMAT);
		}

		if (request.getPassword() == null || !request.getPassword().matches
			("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).{8,}$")) {
			throw new CustomException(ErrorCode.AUTH_INVALID_PASSWORD_FORMAT);
		}

		User user = User.builder()
			.email(request.getEmail())
			.name(request.getName())
			.password(passwordEncoder.encode(request.getPassword()))
			.role(request.getRole())
			.build();

		user.updateProfileImage(
			"https://livechat-s3-bucket-storage.s3.ap-northeast-2.amazonaws.com/default_profile_image.jpg");

		userRepository.save(user);
		return UserRegisterResponse.from(user);
	}

	/**
	 * 로그인을 처리하고 JWT 토큰을 발급합니다.
	 * 1. 이메일로 사용자 존재 여부를 확인합니다.
	 * 2. 비밀번호 일치 여부를 확인합니다.
	 * 3. 인증 성공 시 사용자 ID/Role 기반으로 Access Token을 생성합니다.
	 *
	 * @param request 로그인 요청 DTO
	 * @return JWT Access Token을 담은 응답 DTO
	 * @throws CustomException 사용자 정보를 찾을 수 없거나 비밀번호가 일치하지 않을 경우 에러코드 발생
	 */
	@Transactional
	public UserLoginResponse login(UserLoginRequest request) {
		if (request.getEmail() == null || request.getEmail().isBlank()) {
			throw new CustomException(ErrorCode.AUTH_EMAIL_REQUIRED);
		}

		if (request.getPassword() == null || request.getPassword().isBlank()) {
			throw new CustomException(ErrorCode.AUTH_PASSWORD_REQUIRED);
		}
		User user = userRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new CustomException(ErrorCode.AUTH_USER_NOT_FOUND));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new CustomException(ErrorCode.AUTH_INVALID_CREDENTIALS);
		}

		String accessToken = jwtService.createAccessToken(user.getId(), user.getRole());
		String refreshToken = jwtService.createRefreshToken(user.getId());

		return UserLoginResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	/**
	 * 로그아웃 요청을 처리하여 인증 토큰을 무효화합니다.
	 * 현재 사용중인 Access Token의 남은 시간을 계산하고,
	 * 해당 시간 동안 토큰을 블랙리스트에 추가하여 무효화 합니다.
	 * 해당 토큰이 만료 전까지 재사용 않도록 차단합니다.
	 *
	 * @param accessToken 로그아웃 요청 시 Header에서 추출한 Access Token
	 */
	public void logout(String accessToken) {
		log.info("로그아웃 요청. Access Token 블랙리스트 처리 시작");

		if (accessToken != null && !accessToken.isEmpty()) {
			Long expirationTimeMs = jwtService.getExpirationFromToken(accessToken);
			if (expirationTimeMs != null) {
				long remainingTimeMs = expirationTimeMs - System.currentTimeMillis();
				if (remainingTimeMs > 0) {
					tokenBlacklistService.addToBlacklist(accessToken, remainingTimeMs);
				}
			}
		}
	}

	/**
	 * 주이진 토큰이 블랙리스트에 동록되어 무효화 상태인지 확인합니다.
	 * @param token 확인할 Access Token
	 * @return 토근이 블랙리스트에 등록시 true, 아니면 false
	 */
	public boolean isTokenBlacklisted(String token) {
		return tokenBlacklistService.isBlacklisted(token);
	}
}
