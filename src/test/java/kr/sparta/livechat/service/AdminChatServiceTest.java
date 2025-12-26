package kr.sparta.livechat.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import kr.sparta.livechat.domain.entity.ChatRoom;
import kr.sparta.livechat.domain.entity.Product;
import kr.sparta.livechat.domain.role.ChatRoomStatus;
import kr.sparta.livechat.dto.admin.AdminChatStatusRequest;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.ChatRoomRepository;
import kr.sparta.livechat.repository.MessageRepository;

/**
 * 관리자 전용 채팅 서비스에 대한 단위 테스트 클래스
 * Mockito를 사용하여 외부 의존성을 분리하고 비즈니스 로직 및 권한 검증을 수행합니다.
 * AdminChatServiceTest.java
 *
 * @author kimsehyun
 * @since 2025. 12. 21.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminChatServiceTest {

	@InjectMocks
	private AdminChatService adminChatService;

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private Authentication authentication;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private MessageRepository messageRepository;

	@AfterEach
	void clear() {
		SecurityContextHolder.clearContext();
	}

	/**
	 * 테스트를 위해 가짜 SecurityContext를 설정합니다.
	 * @param role 사용자의 권환
	 */
	private void mockSecurityContext(String role) {
		given(securityContext.getAuthentication()).willReturn(authentication);
		given(authentication.isAuthenticated()).willReturn(true);
		given(authentication.getPrincipal()).willReturn("adminUser");
		List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
		doReturn(authorities).when(authentication).getAuthorities();
		SecurityContextHolder.setContext(securityContext);
	}

	/**
	 * 관리자 권한으로 전체 채팅방 조회하는 성공 테스트
	 * Repository의 findAll 호출 여부 검증
	 * 정렬조건으로 전달되는지 확인
	 */
	@Test
	@DisplayName("관리자 권한으로 전체 채팅방 조회 및 정렬 조건 확인")
	void getAllChatRooms_Success() {
		// given
		mockSecurityContext("ROLE_ADMIN");
		Page<ChatRoom> emptyPage = new PageImpl<>(List.of());
		given(chatRoomRepository.findAll(any(Pageable.class))).willReturn(emptyPage);

		// when
		adminChatService.getAllChatRooms(0, 20);

		// then
		verify(chatRoomRepository).findAll((Pageable)argThat(p -> {
			Pageable pageable = (Pageable)p;
			return pageable.getSort().getOrderFor("status").isDescending() &&
				pageable.getSort().getOrderFor("createdAt").isDescending();
		}));
	}

	/**
	 * 관리자 권한하이 없는 일반 사용자가 조회 했을경우 테스트
	 */
	@Test
	@DisplayName("관리자 권한이 없는 경우 에러코드 발생")
	void getAllChatRooms_AccessDenied() {
		// given
		mockSecurityContext("ROLE_USER");

		// when & then
		assertThatThrownBy(() -> adminChatService.getAllChatRooms(0, 20))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHATROOM_ACCESS_DENIED);
	}

	/**
	 * 페이징 파라미터가 유요하지 않을경우 테스트
	 */
	@Test
	@DisplayName("잘못된 페이징 파라미터 전달 시 에러코드 발생")
	void getAllChatRooms_InvalidPagination() {
		// given
		mockSecurityContext("ROLE_ADMIN");

		// when & then
		assertThatThrownBy(() -> adminChatService.getAllChatRooms(-1, 20))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMON_BAD_PAGINATION);
	}

	/**
	 * 관리자 권한으로 특정 채팅방 상세 조회 성공 테스트
	 */
	@Test
	@DisplayName("상세 조회 성공 - 관리자 권한 및 채팅방 존재 시 Slice 반환")
	void getChatRoomDetail_Success() {
		// given
		mockSecurityContext("ROLE_ADMIN");
		Long chatRoomId = 1L;

		ChatRoom mockRoom = mock(ChatRoom.class);
		given(mockRoom.getId()).willReturn(chatRoomId);
		given(mockRoom.getStatus()).willReturn(kr.sparta.livechat.domain.role.ChatRoomStatus.OPEN);

		given(chatRoomRepository.findById(chatRoomId)).willReturn(java.util.Optional.of(mockRoom));
		given(messageRepository.findByRoomId(eq(chatRoomId), any(org.springframework.data.domain.Pageable.class)))
			.willReturn(new org.springframework.data.domain.SliceImpl<>(List.of()));

		// when
		var response = adminChatService.getChatRoomDetail(chatRoomId, 0, 50);

		// then
		assertThat(response.getChatRoomId()).isEqualTo(chatRoomId);
		verify(messageRepository).findByRoomId(eq(chatRoomId), argThat(p ->
			p.getSort().getOrderFor("sentAt").isDescending()
		));
	}

	/**
	 * 존재하지 않는 채팅방 ID로 상세 조회 시 예외 발생 테스트
	 */
	@Test
	@DisplayName("상세 조회 실패 - 존재하지 않는 채팅방 ID")
	void getChatRoomDetail_NotFound() {
		// given
		mockSecurityContext("ROLE_ADMIN");
		Long invalidId = 999L;
		given(chatRoomRepository.findById(invalidId)).willReturn(java.util.Optional.empty());

		// when & then
		assertThatThrownBy(() -> adminChatService.getChatRoomDetail(invalidId, 0, 50))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHATROOM_NOT_FOUND);
	}

	/**
	 * 관리자 권한이 없을 때 상세 조회가 차단되는지 테스트
	 */
	@Test
	@DisplayName("상세 조회 실패 - 관리자 권한 없음")
	void getChatRoomDetail_AccessDenied() {
		// given
		mockSecurityContext("ROLE_USER");

		// when & then
		assertThatThrownBy(() -> adminChatService.getChatRoomDetail(1L, 0, 50))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHATROOM_ACCESS_DENIED);
	}

	/**
	 * 관리자가 OPEN 채팅방을 CLOSED 에 성공했을때 테스트
	 * 채팅방 조회후 close 가 실제 호출되는지 확인
	 */
	@Test
	@DisplayName("상태 변경 성공 - 관리자가 OPEN 상태인 방을 CLOSED로 변경")
	void updateChatRoomStatus_Success_Logic() {
		// given
		mockSecurityContext("ROLE_ADMIN");
		Long chatRoomId = 1L;
		AdminChatStatusRequest request = new AdminChatStatusRequest("CLOSED");

		ChatRoom mockRoom = mock(ChatRoom.class);
		Product mockProduct = mock(Product.class);

		given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(mockRoom));
		given(mockRoom.getStatus()).willReturn(ChatRoomStatus.OPEN);
		given(mockRoom.getProduct()).willReturn(mockProduct);
		given(mockProduct.getId()).willReturn(100L);
		given(mockProduct.getName()).willReturn("상품명");

		// when
		adminChatService.updateChatRoomStatus(chatRoomId, request);

		// then
		verify(mockRoom).close();
	}

	/**
	 * 이미 closed 상태인 채팅방을 다시 종료하려고 할때 예외가 발생하는지 테스트
	 */
	@Test
	@DisplayName("상태 변경 실패 - 이미 CLOSED 상태인 채팅방 종료 시 409 예외")
	void updateChatRoomStatus_Conflict() {
		// given
		mockSecurityContext("ROLE_ADMIN");
		Long chatRoomId = 1L;
		AdminChatStatusRequest request = new AdminChatStatusRequest("CLOSED");

		ChatRoom mockRoom = mock(ChatRoom.class);
		given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(mockRoom));
		given(mockRoom.getStatus()).willReturn(ChatRoomStatus.CLOSED);

		// when & then
		assertThatThrownBy(() -> adminChatService.updateChatRoomStatus(chatRoomId, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHATROOM_ALREADY_CLOSED);
	}

	/**
	 * 유요하지 않은 상태값을 전달헀을때 예외가 발생하는지 테스트
	 * 유요하지 않은 상태(INVALID_STATUS) 를 반환했을때 에러코드와 함께 예러 메세지 반환
	 */
	@Test
	@DisplayName("상태 변경 실패 - 유효하지 않은 상태값 전달 시 400 예외")
	void updateChatRoomStatus_InvalidStatus() {
		// given
		mockSecurityContext("ROLE_ADMIN");
		AdminChatStatusRequest request = new AdminChatStatusRequest("INVALID_STATUS");

		ChatRoom mockRoom = mock(ChatRoom.class);
		given(chatRoomRepository.findById(1L)).willReturn(Optional.of(mockRoom));

		// when & then
		assertThatThrownBy(() -> adminChatService.updateChatRoomStatus(1L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHATROOM_INVALID_STATUS);
	}
}
