package kr.sparta.livechat.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import kr.sparta.livechat.domain.entity.ChatRoom;
import kr.sparta.livechat.domain.entity.ChatRoomParticipant;
import kr.sparta.livechat.domain.entity.Message;
import kr.sparta.livechat.domain.entity.Product;
import kr.sparta.livechat.domain.role.ChatRoomStatus;
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

/**
 * ChatRoomServiceTest 테스트 클래스입니다.
 * <p>
 * 대상 메서드: {@link ChatRoomService#createChatRoom(Long, Long, String)}
 * </p>
 *
 * @author 재원
 * @since 2025. 12. 19.
 */
@ExtendWith(MockitoExtension.class)
public class ChatRoomServiceTest {

	@Mock
	ChatRoomRepository chatRoomRepository;

	@Mock
	MessageRepository messageRepository;

	@Mock
	ProductRepository productRepository;

	@Mock
	UserRepository userRepository;

	@InjectMocks
	ChatRoomService chatRoomService;

	/**
	 * 채팅방 생성 케이스를 검증합니다. 채팅방 생성에 필요한 최소 조건을 만족했는지 검증합니다.
	 */
	@Test
	@DisplayName("채팅방 생성 성공 - 구매자가 판매중 상품에 대해 채팅방 생성")
	void SuccessCaseCreateChatRoom() {
		// given
		Long productId = 1L;
		Long buyerId = 10L;
		String content = "문의드립니다.";

		User buyer = mock(User.class);
		given(buyer.getId()).willReturn(buyerId);
		given(buyer.getRole()).willReturn(Role.BUYER);

		User seller = mock(User.class);

		Product product = mock(Product.class);
		given(product.getSeller()).willReturn(seller);
		given(product.getStatus()).willReturn(ProductStatus.ONSALE);

		given(userRepository.findById(buyerId)).willReturn(Optional.of(buyer));
		given(productRepository.findById(productId)).willReturn(Optional.of(product));

		given(chatRoomRepository.existsRoomByProductAndBuyerAndStatus(
			productId, buyerId, ChatRoomStatus.OPEN, RoleInRoom.BUYER)).willReturn(false);

		given(chatRoomRepository.save(any(ChatRoom.class)))
			.willAnswer(invocation -> invocation.getArgument(0));
		given(messageRepository.save(any(Message.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		// when
		CreateChatRoomResponse response = chatRoomService.createChatRoom(productId, buyerId, content);

		// then
		assertThat(response).isNotNull();

		verify(userRepository).findById(buyerId);
		verify(productRepository).findById(productId);
		verify(chatRoomRepository).existsRoomByProductAndBuyerAndStatus(
			productId, buyerId, ChatRoomStatus.OPEN, RoleInRoom.BUYER);
		verify(chatRoomRepository).save(any(ChatRoom.class));
		verify(messageRepository).save(any(Message.class));
		verify(product).getStatus();
		verify(product).getSeller();
	}

	/**
	 * 상담 채팅방 생성 실패 케이스이며, 생성 요청자의 권한이 올바르지 않은 경우의 실패 케이스를 검증합니다.
	 */
	@Test
	@DisplayName("채팅방 생성 실패 - 구매자가 아닌 경우")
	void FailCaseCreateChatRoom_NotBuyer() {
		// given
		Long productId = 1L;
		Long userId = 10L;

		User user = mock(User.class);
		given(user.getRole()).willReturn(Role.SELLER);
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// when & then
		assertThatThrownBy(() -> chatRoomService.createChatRoom(productId, userId, "content"))
			.isInstanceOf(CustomException.class);

		verify(productRepository, never()).findById(anyLong());
		verify(chatRoomRepository, never()).save(any());
		verify(messageRepository, never()).save(any());
	}

	/**
	 * 상담 채팅방 생성 실패 케이스이며, 상품의 판매상태가 판매중이 아닌 경우의 실패 케이스를 검증합니다.
	 */
	@Test
	@DisplayName("채팅방 생성 실패 - 상품이 판매중이 아닌 경우")
	void FailCaseCreateChatRoom_ProductNotOnSale() {
		// given
		Long productId = 1L;
		Long buyerId = 10L;

		User buyer = mock(User.class);
		given(buyer.getRole()).willReturn(Role.BUYER);

		Product product = mock(Product.class);
		given(product.getStatus()).willReturn(null);

		given(userRepository.findById(buyerId)).willReturn(Optional.of(buyer));
		given(productRepository.findById(productId)).willReturn(Optional.of(product));

		// when & then
		assertThatThrownBy(() -> chatRoomService.createChatRoom(productId, buyerId, "content"))
			.isInstanceOf(CustomException.class);

		verify(chatRoomRepository, never()).save(any());
		verify(messageRepository, never()).save(any());
	}

	/**
	 * 로그인한 사용자가 참여자로 있는 채팅방 목록 조회 성공 케이스를 검증합니다.
	 */
	@Test
	@DisplayName("채팅방 목록 조회 성공 - 로그인한 사용자 기반 목록 조회")
	void SuccessCaseGetChatRoomList() {
		//given
		Long currentUserId = 10L;
		int page = 0;
		int size = 20;

		User meUser = mock(User.class);
		given(meUser.getId()).willReturn(currentUserId);

		User opponentUser = mock(User.class);
		given(opponentUser.getId()).willReturn(20L);

		ChatRoomParticipant meParticipant = mock(ChatRoomParticipant.class);
		given(meParticipant.getUser()).willReturn(meUser);

		ChatRoomParticipant opponentParticipant = mock(ChatRoomParticipant.class);
		given(opponentParticipant.getUser()).willReturn(opponentUser);

		Product product = mock(Product.class);
		given(product.getName()).willReturn("상품명");

		ChatRoom room = mock(ChatRoom.class);
		given(room.getId()).willReturn(1L);
		given(room.getProduct()).willReturn(product);
		given(room.getParticipants()).willReturn(List.of(meParticipant, opponentParticipant));
		given(room.getLastMessageSentAt()).willReturn(LocalDateTime.now());

		Pageable pageable = PageRequest.of(page, size);
		Page<ChatRoom> roomPage = new PageImpl<>(List.of(room), pageable, 1);
		given(chatRoomRepository.findByParticipantsUserId(eq(currentUserId), any(Pageable.class)))
			.willReturn(roomPage);

		// when
		GetChatRoomListResponse response = chatRoomService.getChatRoomList(currentUserId, page, size);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getPage()).isEqualTo(0);
		assertThat(response.getSize()).isEqualTo(20);
		assertThat(response.getTotalElements()).isEqualTo(1L);
		assertThat(response.getChatRoomList()).hasSize(1);

		ChatRoomListItem item = response.getChatRoomList().get(0);
		assertThat(item.getProductName()).isEqualTo("상품명");
		assertThat(item.getLastMessageSentAt()).isNotNull();

		verify(chatRoomRepository).findByParticipantsUserId(eq(currentUserId), any(Pageable.class));
	}

	/**
	 * 채팅방 목록 조회 요청 시 페이지 파라미터 입력값 오류에 따른 실패 케이스를 검증합니다.
	 */
	@Test
	@DisplayName("채팅방 목록 조회 실패 - 페이지 파라미터 입력값 오류")
	void FailCaseGetChatRoomList_InvalidPage() {
		//given
		Long currentUserId = 10L;
		int page = 0;
		int size = 0;

		// when & then
		Throwable thrown = catchThrowable(() -> chatRoomService.getChatRoomList(currentUserId, page, size));

		assertThat(thrown).isInstanceOf(CustomException.class);
		CustomException ce = (CustomException)thrown;
		assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.COMMON_BAD_PAGINATION);

		verifyNoInteractions(chatRoomRepository);
	}
}

