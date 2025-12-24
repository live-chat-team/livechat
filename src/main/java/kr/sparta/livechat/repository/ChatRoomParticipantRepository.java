package kr.sparta.livechat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.sparta.livechat.domain.entity.ChatRoomParticipant;

/**
 * ChatRoomParticipant 엔티티에 대한 데이터 접근을 담당하는 Repository 인터페이스입니다.
 * <p>
 * 채팅방 참여 여부 및 참여 역할 확인 등 채팅방 접근 제어를 위한 조회기능을 제공합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 19.
 */
public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {

	boolean existsByRoomIdAndUserId(Long roomId, Long userId);

}
