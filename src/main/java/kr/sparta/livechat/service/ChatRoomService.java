package kr.sparta.livechat.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.sparta.livechat.domain.entity.ChatRoom;
import kr.sparta.livechat.domain.entity.ChatRoomParticipant;
import kr.sparta.livechat.domain.entity.Message;
import kr.sparta.livechat.domain.entity.Product;
import kr.sparta.livechat.domain.role.ChatRoomStatus;
import kr.sparta.livechat.domain.role.MessageType;
import kr.sparta.livechat.domain.role.ProductStatus;
import kr.sparta.livechat.domain.role.RoleInRoom;
import kr.sparta.livechat.dto.chatroom.ChatRoomListItem;
import kr.sparta.livechat.dto.chatroom.CreateChatRoomResponse;
import kr.sparta.livechat.dto.chatroom.GetChatRoomListResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.ChatRoomRepository;
import kr.sparta.livechat.repository.MessageRepository;
import kr.sparta.livechat.repository.ProductRepository;
import kr.sparta.livechat.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * 채팅방 생성/조회/ 상태 변경과 같은 채팅방 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * <p>
 * 채팅방 생성은 [채팅방 생성 + 참여자 등록 + 첫 메시지 전송] 을 하나의 트랜잭션으로 보장합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 19.
 */
@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final MessageRepository messageRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;

	/**
	 * 상품에 대한 상담 채팅방을 생성합니다.
	 * <p>
	 * 채팅방 생성은 클래스 상단 설명과 같이 하나의 트랜잭션으로 보장합니다.
	 *
	 * @param productId     상담 대상 상품 식별자
	 * @param currentUserId 채팅방 생성 요청 사용자 식별자(구매자)
	 * @param content       첫 메시지 내용
	 * @return 채팅방 생성 응답
	 * @throws CustomException 구매자 권한이 아니거나, 상품이 존재하지 않거나, 판매중이 아니거나, 이미 열린 채팅방이 존재하는 경우 발생
	 */
	@Transactional
	public CreateChatRoomResponse createChatRoom(Long productId, Long currentUserId, String content) {
		User currentUser = userRepository.findById(currentUserId)
			.orElseThrow(() -> new CustomException(ErrorCode.AUTH_USER_NOT_FOUND));

		validateBuyer(currentUser);

		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

		validateProductAvailableForChat(product);

		validateNoDuplicateOpenRoom(productId, currentUserId);

		ChatRoom room = ChatRoom.open(product);

		ChatRoomParticipant buyerParticipant = ChatRoomParticipant.of(room, currentUser, RoleInRoom.BUYER);
		User seller = extractSeller(product);
		ChatRoomParticipant sellerParticipant = ChatRoomParticipant.of(room, seller, RoleInRoom.SELLER);

		room.getParticipants().add(buyerParticipant);
		room.getParticipants().add(sellerParticipant);

		ChatRoom savedRoom = chatRoomRepository.save(room);

		Message firstMessage = Message.of(savedRoom, currentUser, content, MessageType.TEXT);

		Message savedMessage = messageRepository.save(firstMessage);

		savedRoom.touchLastMessageSentAt(savedMessage.getSentAt());

		CreateChatRoomResponse.FirstMessageResponse firstMessageResponse =
			CreateChatRoomResponse.FirstMessageResponse.of(savedMessage);

		CreateChatRoomResponse.ParticipantResponse buyerResponse =
			CreateChatRoomResponse.ParticipantResponse.of(buyerParticipant);
		CreateChatRoomResponse.ParticipantResponse sellerResponse =
			CreateChatRoomResponse.ParticipantResponse.of(sellerParticipant);

		return CreateChatRoomResponse.of(
			savedRoom,
			List.of(buyerResponse, sellerResponse),
			firstMessageResponse
		);
	}

	private void validateBuyer(User currentUser) {
		if (currentUser.getRole() != Role.BUYER) {
			throw new CustomException(ErrorCode.CHATROOM_CREATE_ACCESS_DENIED);
		}
	}

	private void validateProductAvailableForChat(Product product) {
		if (product.getStatus() != ProductStatus.ONSALE) {
			throw new CustomException(ErrorCode.PRODUCT_NOT_AVAILABLE_FOR_CHAT);
		}
	}

	private void validateNoDuplicateOpenRoom(Long productId, Long buyerId) {
		boolean exists = chatRoomRepository.existsRoomByProductAndBuyerAndStatus(
			productId,
			buyerId,
			ChatRoomStatus.OPEN,
			RoleInRoom.BUYER
		);
		if (exists) {
			throw new CustomException(ErrorCode.CHATROOM_ALREADY_EXISTS);
		}
	}

	private User extractSeller(Product product) {
		User seller = product.getSeller();
		if (seller == null) {
			throw new CustomException(ErrorCode.PRODUCT_SELLER_NOT_FOUND);
		}
		return seller;
	}

	/**
	 * 로그인한 사용자가 참여하는 채팅방 목록을 조회합니다.
	 * <p>
	 * 상담이 가능한 OPEN 상태의 채팅방만 조회하며, 마지막 메시지 전송 시각을 기준으로 내림차순 정렬합니다.
	 * 메시지가 없는 경우 채팅방 개설 시각({@code openedAt})을 기준으로 정렬합니다.
	 * </p>
	 *
	 * @param currentUserId 로그인한 사용자
	 * @param page          조회하고자 하는 페이지
	 * @param size          채팅방 조회 개수
	 * @return 로그인한 사용자가 들어가있는 채팅방 목록 응답
	 */
	public GetChatRoomListResponse getChatRoomList(Long currentUserId, int page, int size) {

		if (page < 0 || size <= 0) {
			throw new CustomException(ErrorCode.COMMON_BAD_PAGINATION);
		}

		Pageable pageable = PageRequest.of(
			page,
			size,
			Sort.by(Sort.Direction.DESC, "lastMessageSentAt", "openedAt"));

		Page<ChatRoom> roomPage = chatRoomRepository.findByParticipantsUserId(currentUserId, pageable);

		List<ChatRoomListItem> items = roomPage.getContent().stream()
			.map(room -> new ChatRoomListItem(room, currentUserId))
			.toList();

		return new GetChatRoomListResponse(
			roomPage.getNumber(),
			roomPage.getSize(),
			roomPage.getTotalElements(),
			roomPage.getTotalPages(),
			roomPage.hasNext(),
			items
		);
	}
}
