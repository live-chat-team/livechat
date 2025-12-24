package kr.sparta.livechat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.sparta.livechat.domain.entity.ChatRoomSummary;

/**
 * ChatRoomSummaryRepository 인터페이스입니다.
 * <p>
 * 상담 종료 시 상담 요약 정보 대한 데이터 접근을 담당하는 레포지토리 인터페이스입니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 21.
 */
public interface ChatRoomSummaryRepository extends JpaRepository<ChatRoomSummary, Long> {
	boolean existsByRoomId(Long roomId);
}
