package kr.sparta.livechat.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.sparta.livechat.domain.entity.ChatRoom;
import kr.sparta.livechat.domain.entity.Message;
import kr.sparta.livechat.domain.entity.Product;
import kr.sparta.livechat.domain.role.ChatRoomStatus;
import kr.sparta.livechat.domain.role.ProductStatus;
import kr.sparta.livechat.domain.role.RoleInRoom;
import kr.sparta.livechat.dto.chatroom.CreateChatRoomResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
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
}

