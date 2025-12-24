package kr.sparta.livechat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.sparta.livechat.domain.entity.Message;

/**
 * Message 엔티티에 대한 데이터 접근을 담당하는 레포지토리 인터페이스입니다.
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 19.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
	/**
	 * 특정 채팅방의 속한 메세지 목록을 Slice 형태로 조회합니다.
	 *
	 * @param roomId 조회하고자 하는 채팅방 고유 식별자ID
	 * @param pageable 페이징 및 정렬 정보를 담은 객체
	 * @return 메시지 목록과 다음 페이지 존재 여부를 포함하는 객체
	 * @author kimsehyun
	 *  @since 2025. 12. 22.
	 */
	Slice<Message> findByRoomId(Long roomId, Pageable pageable);

	long countByRoom_Id(Long roomId);

	/**
	 * 특정 메시지가 특정 채팅방에 속하는지 확인합니다.
	 */
	@Query("SELECT COUNT(m) > 0 FROM Message m WHERE m.id = :messageId AND m.room.id = :roomId")
	boolean existsByIdAndRoomId(@Param("messageId") Long messageId, @Param("roomId") Long roomId);

	/**
	 * 특정 채팅방에서 주어진 메시지 ID 이하의 모든 메시지를 조회합니다.
	 *
	 * 읽음 처리 시 {@code lastReadMessageId}까지의 메시지들을 한 번에 조회하고
	 * MessageRead 기록을 생성하는 데 사용합니다.
	 */
	List<Message> findByRoom_IdAndIdLessThanEqualOrderByIdAsc(Long roomId, Long lastReadMessageId);
}
