package kr.sparta.livechat.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;

/**
 * UserRepository에 대한 테스트 클래스입니다.
 * {@code @DataJpaTest}를 사용하여 JPA 관련 빈들만 로드하며,
 * {@code @AutoConfigureTestDatabase(replace = Replace.NONE)} 설정을 통해
 * 인메모리 DB 대체 없이 실제(또는 설정된) 데이터베이스 연결을 사용하여 테스트합니다.
 * UserRepositoryTest.java
 *
 * @author kimsehyun
 * @since 2025. 12. 12.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	/**
	 * 테스트용 user 엔티티를 생성하는 헬퍼 메서드입니다.
	 * @param email 생성할 사용자의 이메일
	 * @return User 엔티티
	 */
	private User createTestUser(String email) {
		return User.builder()
			.email(email)
			.name("TestUser")
			.password("encodedPassword")
			.role(Role.BUYER)
			.build();
	}

	// 1. 기본 CRUD 및 ID 자동 할당 테스트

	/**
	 * 새로운 user 엔티티를 저장하고 ID가 자동 할당되며, 저장된 데이터를 조회하여 검증합니다.
	 */
	@Test
	@DisplayName("save_SuccessTest")
	void save_SuccessTest() {
		// Given
		User user = createTestUser("test@save.com");

		// When
		User savedUser = userRepository.save(user);

		// Then
		assertThat(savedUser).isNotNull();
		assertThat(savedUser.getId()).isNotNull();

		Optional<User> foundUser = userRepository.findById(savedUser.getId());
		assertThat(foundUser).isPresent();
		assertThat(foundUser.get().getEmail()).isEqualTo("test@save.com");
	}

	//2.findByEmail 테스트

	/**
	 * 이메일로 사용자를 조회하여 Optional<User> 객체에 데이터가 올바르게 담겨 반환되는지 검증합니다.
	 */
	@Test
	@DisplayName("findByEmail_SuccessTest")
	void findByEmail_SuccessTest() {
		// Given
		String email = "find@email.com";
		User user = createTestUser(email);
		userRepository.save(user);

		// When
		Optional<User> foundUser = userRepository.findByEmail(email);

		// Then
		assertThat(foundUser).isPresent();
		assertThat(foundUser.get().getEmail()).isEqualTo(email);
	}

	/**
	 * 존재하지 않는 이메일로 조회 시, 빈 optional 이 반환되는지 검증합니다.
	 */
	@Test
	@DisplayName("findByEmail_Fail_UserNotFoundTest")
	void findByEmail_Fail_UserNotFoundTest() {
		// Given
		String nonExistentEmail = "notfound@email.com";

		// When
		Optional<User> foundUser = userRepository.findByEmail(nonExistentEmail);

		// Then
		assertThat(foundUser).isEmpty();
	}

	//3. 회원가입 중복 체크 테스트

	/**
	 * DB에 존재하는 이메일로 호출 시, true를 반환하는지 검증합니다.
	 */
	@Test
	@DisplayName("existsByEmail_Success_ReturnsTrueTest")
	void existsByEmail_Success_ReturnsTrueTest() {
		// Given
		String duplicateEmail = "duplicate@check.com";
		User user = createTestUser(duplicateEmail);
		userRepository.save(user);

		// When
		boolean exists = userRepository.existsByEmail(duplicateEmail);

		// Then
		assertThat(exists).isTrue();
	}

	/**
	 * DB에 존재하지 않는 이메일로 호출 시, false를 반환하는지 검증합니다.
	 */
	@Test
	@DisplayName("existsByEmail_Success_ReturnsFalseTest")
	void existsByEmail_Success_ReturnsFalseTest() {
		// Given
		String newEmail = "new@check.com";

		// When
		boolean exists = userRepository.existsByEmail(newEmail);

		// Then
		assertThat(exists).isFalse();
	}

	// 4. 사용자 유일성 제약 조건 테스트

	/**
	 * 동일한 이메일을 가진 두 번째 사용자를 저장하려고 할 때
	 * 예외처리가 발생하는지 검증
	 */
	@Test
	@DisplayName("save_Fail_EmailUniqueViolationTest")
	void save_Fail_EmailUniqueViolationTest() {
		// Given
		String email = "violation@db.com";
		User user1 = createTestUser(email);
		User user2 = createTestUser(email);

		userRepository.save(user1);

		// When & Then
		// 두 번째 사용자 저장 예회처리가 발생해야 함
		assertThrows(DataIntegrityViolationException.class, () -> {
			userRepository.saveAndFlush(user2);
		});
	}
}