package kr.sparta.livechat.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

}
