package kr.sparta.livechat.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import kr.sparta.livechat.dto.UserRegisterRequest;
import kr.sparta.livechat.dto.UserRegisterResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * 인증 및 회원가입 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 회원가입 요청을 검증하고 비밀번호 암호화, 사용자 엔티티 생성 및 저장,
 * 중복 이메일 체크 등의 주요 로직을 담당합니다. 성공적으로 회원가입이 완료되면
 * 사용자 정보를 담은 응답 DTO를 반환합니다.
 * AuthService.java
 *
 * @author kimsehyun
 * @since 2025. 12. 11.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 회원가입을 처리합니다.
	 * 이메일이 이미 존재할 경우 409 Conflict 예외를 발생시킵니다.
	 *
	 * @param request 회원가입 요청 DTO
	 * @return 회원가입된 사용자 정보를 담은 응답 DTO
	 * @throws ResponseStatusException 이메일 중복 시 409 CONFLICT 발생
	 */
	@Transactional
	public UserRegisterResponse registerUser(UserRegisterRequest request) {

		if (request.getRole() == Role.ADMIN) {
			throw new CustomException(ErrorCode.AUTH_FORBIDDEN_ROLE);

		}
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new CustomException(ErrorCode.AUTH_DUPLICATE_EMAIL);
		}

		String encodedPassword = passwordEncoder.encode(request.getPassword());

		User user = User.builder()
			.email(request.getEmail())
			.name(request.getName())
			.password(encodedPassword)
			.role(request.getRole())
			.build();

		userRepository.save(user);

		return UserRegisterResponse.from(user);
	}
}
