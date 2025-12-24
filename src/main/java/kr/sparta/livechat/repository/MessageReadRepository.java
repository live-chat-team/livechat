package kr.sparta.livechat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.sparta.livechat.domain.entity.MessageRead;

/**
 * MessageRead 엔티티에 대한 데이터 접근을 담당하는 Repository 인터페이스입니다.
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 19.
 */
public interface MessageReadRepository extends JpaRepository<MessageRead, Long> {
	boolean existsByMessageIdAndUserId(Long messageId, Long userId);
}
