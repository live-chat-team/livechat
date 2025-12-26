package kr.sparta.livechat.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * SMTP 이메일 발송을 담당하는 서비스 클래스입니다.
 * <p>
 * 비밀번호 재설정 인증 코드 메일 발송을 제공합니다.
 * </p>
 *
 * @author 재원
 * @since 2025. 12. 24.
 */
@Service
@RequiredArgsConstructor
public class MailService {

	private final JavaMailSender mailSender;

	/**
	 * 비밀번호 재설정 인증 코드를 이메일로 발송합니다.
	 *
	 * @param toEmail 수신자 이메일
	 * @param code    6자리 인증 코드
	 */
	public void sendPasswordResetCode(String toEmail, String code) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setSubject("[LiveChat] 비밀번호 재설정 인증 코드");
		message.setText(buildPasswordResetText(code));
		mailSender.send(message);
	}

	private String buildPasswordResetText(String code) {
		return """
			[LiveChat] 비밀번호 재설정 인증 코드 안내

			인증 코드: %s

			- 인증 코드는 3분간 유효합니다.
			- 본인이 요청하지 않았다면 이 메일을 무시해주세요.
			""".formatted(code);
	}
}
