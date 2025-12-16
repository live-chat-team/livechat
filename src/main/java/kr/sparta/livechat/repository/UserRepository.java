package kr.sparta.livechat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.sparta.livechat.entity.User;

/**
 * user 엔티티에 대한 데이터베이스 접근을 담당하는 레포지토리 인터페이스입니다.
 * 이메일 중복 체크, 이메일 기반 사용자 조회 등의 사용자 관련 데이터 처리 기능을 제공합니다.
 * UserRepository.java
 *
 * @author kimsehyun
 * @since 2025. 12. 11.
 */
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * 이메일을 사용하여 사용자가 존재하는지 확인합니다.
	 * 회원가입 시 이메일 중복 여부를 검증하는 데 사용됩니다.
	 *
	 * @param email 확인할 이메일
	 * @return 해당 이메일을 가진 사용자가 존재하면 true, 없으면 false
	 */
	boolean existsByEmail(String email);

	/**
	 * 이메일을 기준으로 사용자를 조회합니다.
	 * 로그인 기능 또는 인증 과정에서 사용자의 정보를 가져올 때 활용됩니다.
	 *
	 * @param email 조회할 사용자 이메일
	 * @return 사용자 정보가 존재하면 Optional에 담아 반환, 없으면 빈 Optional 반환
	 */
	Optional<User> findByEmail(String email);
}
