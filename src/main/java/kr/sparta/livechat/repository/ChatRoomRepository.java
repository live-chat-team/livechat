package kr.sparta.livechat.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.sparta.livechat.domain.entity.ChatRoom;
import kr.sparta.livechat.domain.role.ChatRoomStatus;
import kr.sparta.livechat.domain.role.RoleInRoom;

/**
 * ChatRoom 엔티티에 대한 데이터 접근을 담당하는 Repository 인터페이스입니다.
 * <p>
 * 채팅방 생성 시 중복 OPEN 채팅방 존재 여부 확인, 채팅방 조회 등 도메인 요구사항을 위한 조회 기능을 제공합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 19.
 */
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	/**
	 * 특정 상품에 대해 특정 구매자가 OPEN 상태의 채팅방을 이미 보유하고 있는지 확인합니다.
	 *
	 * @param productId  상담 대상 상품 식별자
	 * @param buyerId    구매자 사용자 식별자
	 * @param status     확인할 채팅방 상태 (생성 시 OPEN)
	 * @param roleInRoom 확인할 방 내 역할 (생성 시 BUYER)
	 * @return 조건에 해당하는 채팅방이 존재하면 {@code true}, 아니면 {@code false}
	 */
	@Query("""
			select count(r) > 0
			from ChatRoom r
			join r.participants p
			where r.product.id = :productId
			  and r.status = :status
			  and p.user.id = :buyerId
			  and p.roleInRoom = :roleInRoom
		""")
	boolean existsRoomByProductAndBuyerAndStatus(
		@Param("productId") Long productId,
		@Param("buyerId") Long buyerId,
		@Param("status") ChatRoomStatus status,
		@Param("roleInRoom") RoleInRoom roleInRoom
	);

	@EntityGraph(attributePaths = {"participants", "participants.user", "product"})
	Page<ChatRoom> findByParticipantsUserId(Long userId, Pageable pageable);

	@EntityGraph(attributePaths = {"participants", "participants.user", "product"})
	Optional<ChatRoom> findById(Long chatRoomId);
}

