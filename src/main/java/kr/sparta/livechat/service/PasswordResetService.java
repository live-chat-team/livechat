package kr.sparta.livechat.service;

import java.security.SecureRandom;
import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * 비밀번호 재설정 관련 비즈니스 로직을 담당하는 서비스 클래스입니다.
 * <p>
 * 이메일 인증 코드를 Redis에 저장하고, 인증 코드 검증 후 비밀번호를 변경하는 책임을 가집니다.
 * 해당 서비스는 로그인 이전 단계에서 호출되며, SecurityContext에 의존하지 않습니다.
 * </p>
 *
 * @author 재원
 * @since 2025. 12. 24.
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

	private final Duration CODE_TTL = Duration.ofMinutes(3);
	private final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final StringRedisTemplate redisTemplate;
	private final MailService mailService;
	private final SecureRandom random = new SecureRandom();

	/**
	 * 비밀번호 재설정 인증 코드 발송 요청 처리
	 *
	 * @param email 비밀번호 재설정을 요청한 사용자 이메일
	 */
	public void requestReset(String email) {
		userRepository.findByEmail(email)
			.orElseThrow(() -> new CustomException(ErrorCode.AUTH_USER_NOT_FOUND));

		String cooldownKey = cooldownKey(email);
		if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
			throw new CustomException(ErrorCode.TOO_MANY_REQUEST);
		}
		String code = generate6DigitCode();
		redisTemplate.opsForValue().set(codeKey(email), code, CODE_TTL);
		redisTemplate.opsForValue().set(cooldownKey, "1", RESEND_COOLDOWN);
		mailService.sendPasswordResetCode(email, code);
	}

	/**
	 * 인증 코드 검증 및 비밀번호 변경 처리
	 *
	 * @param email       비밀번호 재설정 대상 사용자 이메일
	 * @param code        이메일로 전송된 인증 코드
	 * @param newPassword 새 비밀번호
	 */
	@Transactional
	public void confirmReset(String email, String code, String newPassword) {
		String key = codeKey(email);
		String savedCode = redisTemplate.opsForValue().get(key);

		if (savedCode == null) {
			throw new CustomException(ErrorCode.AUTH_RESET_CODE_EXPIRED);
		}

		if (!savedCode.equals(code)) {
			throw new CustomException(ErrorCode.AUTH_RESET_CODE_INVALID);
		}
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new CustomException(ErrorCode.AUTH_USER_NOT_FOUND));
		String encoded = passwordEncoder.encode(newPassword);
		user.updatePassword(encoded);
		redisTemplate.delete(key);
	}

	private String generate6DigitCode() {
		return String.format("%06d", random.nextInt(1_000_000));
	}

	private String codeKey(String email) {
		return "pwreset:code:" + email;
	}

	private String cooldownKey(String email) {
		return "pwreset:cooldown:" + email;
	}
}
