package kr.sparta.livechat.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import kr.sparta.livechat.entity.User;
import lombok.Getter;

/**
 * User 엔티티 정보를 UserDetails 형식으로 반환하는 클래스입니다.
 * 사용자 ID, 이메일, 비밀번호, 역할을 포함하고,
 * JwtAuthenticationFilter에서 인증 객체 생성후 Security Context에 저장합니다.
 * CustomUserDetails.java
 *
 * @author kimsehyun
 * @since 2025. 12. 16.
 */
@Getter
public class CustomUserDetails implements UserDetails {

	private final Long userId;
	private final String email;
	private final String password;
	private final Collection<? extends GrantedAuthority> authorities;

	/**
	 * User 엔티티를 받아 CustomUserDetails 객체를 생성합니다.
	 * 엔티티의 Role를 SimpleGrantedAuthority 반환하여 저장합니다.
	 */
	public CustomUserDetails(User user) {
		this.userId = user.getId();
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.authorities = Collections.singletonList(
			new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
		);
	}

	/**
	 * 사용자 권환 목록을 반환합니다.
	 * @return 사용자의 권한 목록
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	/**
	 * 사용자의 비밀번호를 반환합니다.
	 * @return 비밀번호
	 */
	@Override
	public String getPassword() {
		return password;
	}

	/**
	 * 사용자 이름으로 사용될 값을 반환합니다.
	 * @return 사용자 이메일
	 */
	@Override
	public String getUsername() {
		return email;
	}
}
