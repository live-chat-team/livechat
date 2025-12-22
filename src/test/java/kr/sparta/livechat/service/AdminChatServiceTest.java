package kr.sparta.livechat.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import kr.sparta.livechat.domain.entity.ChatRoom;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.ChatRoomRepository;

/**
 * 관리자 전용 채팅 서비스에 대한 단위 테스트 클래스
 * Mockito를 사용하여 외부 의존성을 분리하고 비즈니스 로직 및 권한 검증을 수행합니다.
 * AdminChatServiceTest.java
 *
 * @author kimsehyun
 * @since 2025. 12. 21.
 */
@ExtendWith(MockitoExtension.class)
class AdminChatServiceTest {

	@InjectMocks
	private AdminChatService adminChatService;

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private Authentication authentication;

	@Mock
	private SecurityContext securityContext;

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
			return pageable.getSort().getOrderFor("status").isAscending() &&
				pageable.getSort().getOrderFor("createdAt").isDescending();
		}));
	}

	/**
	 * 관리자 권하이 없는 일반 사용자가 조회 했을경우 테스트
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
}
