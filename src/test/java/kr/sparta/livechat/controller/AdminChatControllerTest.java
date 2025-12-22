package kr.sparta.livechat.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import kr.sparta.livechat.config.SecurityConfig;
import kr.sparta.livechat.dto.admin.AdminChatRoomListResponse;
import kr.sparta.livechat.repository.UserRepository;
import kr.sparta.livechat.service.AdminChatService;
import kr.sparta.livechat.service.AuthService;
import kr.sparta.livechat.service.JwtService;

/**
 * 관리자 전용 채팅방 관리 컨트롤러 단위 테스트 클래스입니다.
 * 스프링 시큐리티 설정을 임포트 하여 권한 기반의 접근 제어를 검증합니다.
 * AdminChatControllerTest.java
 *
 * @author kimsehyun
 * @since 2025. 12. 21.
 */
@WebMvcTest(AdminChatController.class)
@Import(SecurityConfig.class)
class AdminChatControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AdminChatService adminChatService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private UserRepository userRepository;

	/**
	 * 관리자 권한을 가진 사용자가 전체 채팅방을 조회할때 성공하는 케이스 테스트
	 * HTTP 200 반환 여부
	 * 페이지 번호와 사이즈 파라미터가 전달되는지 확인
	 * @throws Exception 요청수행중 발생할수 있는 예외
	 */
	@Test
	@DisplayName("GET /api/admin/chat-rooms - 성공 (관리자 권한)")
	@WithMockUser(roles = "ADMIN")
	void getAdminChatRooms_Success() throws Exception {
		// given
		AdminChatRoomListResponse response = AdminChatRoomListResponse.builder()
			.page(0)
			.size(20)
			.totalElements(1)
			.chatRoomList(List.of())
			.build();

		given(adminChatService.getAllChatRooms(0, 20)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/chat-rooms")
				.param("page", "0")
				.param("size", "20")
				.with(csrf()) // CSRF 보안 통과용 추가
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
	}

	/**
	 * 일반 유저가 관리자 전용 API 에 접근할때 테스트
	 * HTTP 403 반환여부
	 * @throws Exception 요청중 발생할수 있는예외
	 */
	@Test
	@DisplayName("GET /api/admin/chat-rooms - 실패 (일반 유저 권한)")
	@WithMockUser(roles = "USER")
	void getAdminChatRooms_Forbidden() throws Exception {
		mockMvc.perform(get("/api/admin/chat-rooms")
				.with(csrf()))
			.andExpect(status().isForbidden());
	}

	/**
	 * 인증정보가 없는 사용자가 관리자 전용 API 에 겁근할때 차단되는지 테스트
	 * 시큐리티 설정에 따른 접근 거부 403 확인
	 * @throws Exception 요청 수행중 발생할수 있는예외
	 */
	@Test
	@DisplayName("GET /api/admin/chat-rooms - 실패 (비로그인)")
	void getAdminChatRooms_Unauthorized() throws Exception {
		mockMvc.perform(get("/api/admin/chat-rooms")
				.with(csrf()))
			.andExpect(status().isForbidden());
	}
}
