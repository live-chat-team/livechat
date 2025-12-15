package kr.sparta.livechat.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 정보를 표현하는 엔티티 클래스입니다.
 * 사용자 이메일, 이름, 비밀번호, 역할 및 생성/수정 시간을 관리합니다.
 * User.java
 *
 * @author kimsehyun
 * @since 2025. 12. 11.
 */
@Entity
@Getter
@Setter(AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 500)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	/**
	 * User 엔티티의 생성자입니다.
	 *
	 * @param email    사용자 이메일
	 * @param name     사용자 이름
	 * @param password 암호화된 비밀번호
	 * @param role     사용자 역할
	 */
	@Builder
	public User(String email, String name, String password, Role role) {
		this.email = email;
		this.name = name;
		this.password = password;
		this.role = role;
	}
}
