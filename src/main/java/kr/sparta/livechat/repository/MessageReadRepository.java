package kr.sparta.livechat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.sparta.livechat.domain.entity.MessageRead;

/**
 * MessageRead 엔티티에 대한 데이터 접근을 담당하는 Repository 인터페이스입니다.
 * <p>
 * 본 레포지토리는 메시지 읽음 처리와 관련된 데이터 접근을 위해 마련되었으며,
 * 실제 읽음 처리(읽음 저장, 읽음 이벤트 전파 등)는 WebSocket 기반의 실시간 흐름에서
 * 처리되는 것을 전제로 합니다.
 * </p>
 * <p>
 * 현재 메시지 목록 조회 단계에서는 읽음 상태를 직접 조회하거나 활용하지 않으며,
 * WebSocket 담당 영역에서 읽음 처리 기능이 구현될 경우 확장하여 사용될 수 있습니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 19.
 */
public interface MessageReadRepository extends JpaRepository<MessageRead, Long> {
}
