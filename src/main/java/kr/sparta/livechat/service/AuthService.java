package kr.sparta.livechat.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import kr.sparta.livechat.dto.UserRegisterRequest;
import kr.sparta.livechat.dto.UserRegisterResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
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

		// 1. 이메일 중복 체크
		if (userRepository.existsByEmail(
			request.getEmail())) {
			throw new ResponseStatusException(
				HttpStatus.CONFLICT,
				"이미 사용 중인 이메일 주소입니다."
			);
		}
		// 2. ADMIN 역할 등록 방지 체크
		if (request.getRole() == Role.ADMIN) {
			throw new ResponseStatusException(
				HttpStatus.FORBIDDEN,
				"ADMIN 역할은 일반 회원가입을 통해 등록할 수 없습니다."
			);
		}

		// 3. 비밀번호 암호화
		String encodedPassword = passwordEncoder.encode(request.getPassword());

		// 4. 사용자 엔티티 생성
		User user = User.builder()
			.email(request.getEmail())
			.name(request.getName())
			.password(encodedPassword)
			.role(request.getRole())
			.build();

		// 5. DB 저장
		userRepository.save(user);

		// 6. 응답 DTO 반환
		return UserRegisterResponse.from(user);
	}
}
