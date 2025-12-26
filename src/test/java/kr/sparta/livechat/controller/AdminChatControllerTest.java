package kr.sparta.livechat.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.sparta.livechat.config.SecurityConfig;
import kr.sparta.livechat.dto.admin.AdminChatDetailResponse;
import kr.sparta.livechat.dto.admin.AdminChatRoomListResponse;
import kr.sparta.livechat.dto.admin.AdminChatStatusRequest;
import kr.sparta.livechat.dto.admin.AdminChatStatusResponse;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
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

	@Autowired
	private ObjectMapper objectMapper;

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
		given(adminChatService.getAllChatRooms(anyInt(), anyInt()))
			.willThrow(new CustomException(ErrorCode.CHATROOM_ACCESS_DENIED));

		mockMvc.perform(get("/api/admin/chat-rooms")
				.with(csrf()))
			.andExpect(status().isForbidden()) // 이제 403이 발생합니다.
			.andExpect(jsonPath("$.code").value("CHATROOM_ACCESS_DENIED"));
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

	/**
	 * 관리자 권한을 가진 사용자가 특정 채팅방 조회시 성공 케이스 테스트
	 * HTTP 200 반환여부
	 * 응답 바디의 채팅방 ID 및 상태값이 Mock 데이터와 일치하느지 검증
	 *
	 * @throws Exception 발생할수 있는 예외
	 */
	@Test
	@DisplayName("GET /api/admin/chat-rooms/{chatRoomId} - 성공 (관리자 권한)")
	@WithMockUser(roles = "ADMIN")
	void getAdminChatDetail_Success() throws Exception {
		// given
		Long chatRoomId = 1L;
		AdminChatDetailResponse response = AdminChatDetailResponse.builder()
			.chatRoomId(chatRoomId)
			.chatRoomStatus("OPEN")
			.page(0)
			.size(50)
			.hasNext(false)
			.messagesList(List.of())
			.build();

		given(adminChatService.getChatRoomDetail(eq(chatRoomId), anyInt(), anyInt())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/chat-rooms/{chatRoomId}", chatRoomId)
				.param("page", "0")
				.param("size", "50")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.chatRoomId").value(chatRoomId))
			.andExpect(jsonPath("$.chatRoomStatus").value("OPEN"));
	}

	/**
	 * 관리자 권한이 없는 일반 사용자가 조회했을떄 테스트
	 * HTTP 403 반화 여부
	 *
	 * @throws Exception 발생할수 있는 예외
	 */
	@Test
	@DisplayName("GET /api/admin/chat-rooms/{chatRoomId} - 실패 (일반 유저 권한)")
	@WithMockUser(roles = "USER")
	void getAdminChatDetail_Forbidden() throws Exception {
		given(adminChatService.getChatRoomDetail(anyLong(), anyInt(), anyInt()))
			.willThrow(new CustomException(ErrorCode.CHATROOM_ACCESS_DENIED));

		mockMvc.perform(get("/api/admin/chat-rooms/1")
				.with(csrf()))
			.andExpect(status().isForbidden()) // 이제 403이 발생합니다.
			.andExpect(jsonPath("$.code").value("CHATROOM_ACCESS_DENIED"));
	}

	/**
	 * 인증 정보가없는 사용자가 조히 했을때 테스트
	 * HTTP 403 반환 여부
	 *
	 * @throws Exception 발생할 수 있는 예외
	 */
	@Test
	@DisplayName("GET /api/admin/chat-rooms/{chatRoomId} - 실패 (비로그인)")
	void getAdminChatDetail_Unauthorized() throws Exception {
		mockMvc.perform(get("/api/admin/chat-rooms/1")
				.with(csrf()))
			.andExpect(status().isForbidden());
	}

	/**
	 * 관리자가 채팅방 상태를 변경할때 성공 케이스
	 * HTTP 200 응답 반환
	 * @throws Exception 요청수행중 발생 하는 예외
	 */
	@Test
	@DisplayName("PATCH /api/admin/chat-rooms/{chatRoomId} - 성공 (상태 변경)")
	@WithMockUser(roles = "ADMIN")
	void updateChatRoomStatus_Success() throws Exception {
		// given
		Long chatRoomId = 1L;
		AdminChatStatusRequest request = new AdminChatStatusRequest("CLOSED");
		AdminChatStatusResponse response = AdminChatStatusResponse.builder()
			.chatRoomId(chatRoomId)
			.status("CLOSED")
			.openedAt(LocalDateTime.now().minusHours(1))
			.closedAt(LocalDateTime.now())
			.productId(100L)
			.productName("테스트 상품")
			.build();

		given(adminChatService.updateChatRoomStatus(eq(chatRoomId), any(AdminChatStatusRequest.class)))
			.willReturn(response);

		// when & then
		mockMvc.perform(patch("/api/admin/chat-rooms/{chatRoomId}", chatRoomId)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CLOSED"))
			.andExpect(jsonPath("$.chatRoomId").value(chatRoomId));
	}

	/**
	 * 일반 사용자가 관리자 전용 상태 변경 API를 실행했을때 실패 테스트
	 * HTTP 403 반환 여부와 에러코드 반환 여부
	 * @throws Exception 요청 수행 중 발생할 수 있는 예외
	 */
	@Test
	@DisplayName("PATCH /api/admin/chat-rooms/{chatRoomId} - 실패 (일반 유저 권한)")
	@WithMockUser(roles = "USER")
	void updateChatRoomStatus_Forbidden() throws Exception {
		// given:
		given(adminChatService.updateChatRoomStatus(anyLong(), any()))
			.willThrow(new CustomException(ErrorCode.CHATROOM_ACCESS_DENIED));

		// when & then
		mockMvc.perform(patch("/api/admin/chat-rooms/1")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AdminChatStatusRequest("CLOSED"))))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("CHATROOM_ACCESS_DENIED"));
	}
}
