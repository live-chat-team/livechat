package kr.sparta.livechat.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.sparta.livechat.domain.entity.Message;

/**
 * Message 엔티티에 대한 데이터 접근을 담당하는 레포지토리 인터페이스입니다.
 * <p>
 * 채팅방에 속한 메시지를 조회하거나 개수를 집계하는 기능을 제공하며, 메시지 목록 조회 시에는 페이지 기반 조회와 커서 기반 조회를 모두 지원합니다.
 * </p>
 * <p>
 * 커서 기반 조회는 특정 메시지 ID를 기준으로 그 이전에 전송된 메시지들을 안정적으로 조회하기 위한 용도로 사용되며,
 * 스크롤 기반 메시지 목록 조회 구현을 고려한 설계입니다.
 * </p>
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

	Slice<Message> findByRoom_Id(Long chatRoomId, Pageable pageable);

	Slice<Message> findByRoom_IdAndIdLessThan(Long roomId, Long beforeMessageId, Pageable pageable);
}
