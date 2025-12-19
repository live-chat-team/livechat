package kr.sparta.livechat.controller;

import static kr.sparta.livechat.domain.role.ChatRoomStatus.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.sparta.livechat.config.SecurityConfig;
import kr.sparta.livechat.dto.chatroom.CreateChatRoomRequest;
import kr.sparta.livechat.dto.chatroom.CreateChatRoomResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.repository.UserRepository;
import kr.sparta.livechat.security.CustomUserDetails;
import kr.sparta.livechat.service.AuthService;
import kr.sparta.livechat.service.ChatRoomService;
import kr.sparta.livechat.service.JwtService;

/**
 * ChatRoomController 테스트 클래스입니다.
 * <p>
 * {@link ChatRoomController}의 요청 매핑, 요청 바디({@code @RequestBody}), 응답 상태/바디 형식을 검증합니다.
 * 비즈니스 로직은 {@link ChatRoomService}를 Mock 처리하여 컨트롤러 계층만 테스트합니다.
 * </p>
 *
 * @author 재원
 * @since 2025. 12. 19.
 */
@WebMvcTest(ChatRoomController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "server.port=0")
public class ChatRoomControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ChatRoomService chatRoomService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private AuthService authService;

	private void loginAs(Long userId, Role role) {
		User user = mock(User.class);
		given(user.getId()).willReturn(userId);
		given(user.getEmail()).willReturn("test@test.com");
		given(user.getPassword()).willReturn("pw");
		given(user.getRole()).willReturn(role);

		CustomUserDetails userDetails = new CustomUserDetails(user);

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
			userDetails, null, userDetails.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	private void loginAsBuyer(Long userId) {
		loginAs(userId, Role.BUYER);
	}

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	/**
	 * 채팅방 생성 성공 케이스를 검증합니다.
	 * <p>
	 * 인증된 구매자 권한을 가진 사용자가 정상 요청 바디로 채팅방 생성 요청을 진행하면
	 * 201(Created) 상태와 함께 서비스 반환 DTO가 JSON 응답에 포함되는지 확인합니다.
	 */
	@Test
	@DisplayName("채팅방 생성 성공 - 201 응답")
	void createChatRoom_Success() throws Exception {
		// given
		Long productId = 1L;
		Long buyerId = 10L;
		loginAsBuyer(buyerId);

		CreateChatRoomResponse response = mock(CreateChatRoomResponse.class);
		given(response.getChatRoomId()).willReturn(1L);
		given(response.getStatus()).willReturn(OPEN);

		given(chatRoomService.createChatRoom(eq(productId), eq(buyerId), eq("문의드립니다.")))
			.willReturn(response);

		CreateChatRoomRequest request =
			new CreateChatRoomRequest("문의드립니다.");

		String requestJson = objectMapper.writeValueAsString(request);

		// when & then
		mockMvc.perform(post("/api/products/{productId}/chat-rooms", productId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isCreated())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.chatRoomId").value(1))
			.andExpect(jsonPath("$.status").value("OPEN"));

		then(chatRoomService).should(times(1))
			.createChatRoom(eq(productId), eq(buyerId), eq("문의드립니다."));
	}
}
