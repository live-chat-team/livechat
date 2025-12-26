package kr.sparta.livechat.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

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

import kr.sparta.livechat.config.SecurityConfig;
import kr.sparta.livechat.dto.message.ChatMessageListItem;
import kr.sparta.livechat.dto.message.GetChatMessageListResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.UserRepository;
import kr.sparta.livechat.security.CustomUserDetails;
import kr.sparta.livechat.service.AuthService;
import kr.sparta.livechat.service.JwtService;
import kr.sparta.livechat.service.MessageService;

/**
 * MessageControllerTest 테스트 클래스입니다.
 * <p>
 * 메시지 관련 응답 상태/ 바디 형식을 검증하며, MessageService의 비즈니스 로직을 Mock 처리하여 컨트롤러 계층만 테스트합니다.
 * </p>
 *
 * @author 재원
 * @since 2025. 12. 23.
 */
@WebMvcTest(MessageController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "server.port=0")
public class MessageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private MessageService messageService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private UserRepository userRepository;

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

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

	/**
	 * 메시지 목록 조회 성공 케이스를 검증합니다.
	 * cursor/size 미전달(최초 조회) 요청 시 200 OK와 JSON 응답 형식을 확인합니다.
	 */
	@Test
	@DisplayName("메시지 목록 조회 성공 - cursor/size null")
	void getMessageList_Success_FirstRequest() throws Exception {
		// given
		Long chatRoomId = 1L;
		Long buyerId = 10L;
		loginAsBuyer(buyerId);

		ChatMessageListItem item = mock(ChatMessageListItem.class);
		given(item.getMessageId()).willReturn(110L);
		given(item.getContent()).willReturn("안녕하세요");
		given(item.getWriterId()).willReturn(10L);
		given(item.getSentAt()).willReturn(LocalDateTime.parse("2025-12-23T10:00:00"));

		GetChatMessageListResponse response = mock(GetChatMessageListResponse.class);
		given(response.getChatRoomId()).willReturn(chatRoomId);
		given(response.getMessageList()).willReturn(List.of(item));
		given(response.isHasNext()).willReturn(false);
		given(response.getNextCursor()).willReturn(null);

		given(messageService.getMessageList(eq(chatRoomId), isNull(), isNull(), eq(buyerId)))
			.willReturn(response);

		// when & then
		mockMvc.perform(get("/api/chat-rooms/{chatRoomId}/messages", chatRoomId))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.chatRoomId").value(1))
			.andExpect(jsonPath("$.hasNext").value(false))
			.andExpect(jsonPath("$.nextCursor").isEmpty())
			.andExpect(jsonPath("$.messageList").isArray());

		then(messageService).should(times(1))
			.getMessageList(eq(chatRoomId), isNull(), isNull(), eq(buyerId));
	}

	/**
	 * 메시지 목록 조회 성공 케이스를 검증합니다.
	 * cursor/size 전달하여 요청 시 200 OK와 JSON 응답 형식을 확인합니다.
	 */
	@Test
	@DisplayName("메시지 목록 조회 성공 - cursor/size 존재")
	void getMessageList_Success_WithCursor() throws Exception {
		// given
		Long chatRoomId = 1L;
		Long buyerId = 10L;
		Long cursor = 1050L;
		Integer size = 2;

		loginAsBuyer(buyerId);

		ChatMessageListItem item1 = mock(ChatMessageListItem.class);
		given(item1.getMessageId()).willReturn(1049L);
		given(item1.getContent()).willReturn("이전 메시지1");
		given(item1.getWriterId()).willReturn(10L);
		given(item1.getSentAt()).willReturn(LocalDateTime.parse("2025-12-23T09:59:59"));

		ChatMessageListItem item2 = mock(ChatMessageListItem.class);
		given(item2.getMessageId()).willReturn(1048L);
		given(item2.getContent()).willReturn("이전 메시지2");
		given(item2.getWriterId()).willReturn(20L);
		given(item2.getSentAt()).willReturn(LocalDateTime.parse("2025-12-23T09:59:58"));

		GetChatMessageListResponse response = mock(GetChatMessageListResponse.class);
		given(response.getChatRoomId()).willReturn(chatRoomId);
		given(response.getMessageList()).willReturn(List.of(item1, item2));
		given(response.isHasNext()).willReturn(true);
		given(response.getNextCursor()).willReturn(1048L);

		given(messageService.getMessageList(eq(chatRoomId), eq(cursor), eq(size), eq(buyerId)))
			.willReturn(response);

		// when & then
		mockMvc.perform(get("/api/chat-rooms/{chatRoomId}/messages", chatRoomId)
				.param("cursor", String.valueOf(cursor))
				.param("size", String.valueOf(size)))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.chatRoomId").value(1))
			.andExpect(jsonPath("$.hasNext").value(true))
			.andExpect(jsonPath("$.nextCursor").value(1048))
			.andExpect(jsonPath("$.messageList").isArray())
			.andExpect(jsonPath("$.messageList.length()").value(2));

		then(messageService).should(times(1))
			.getMessageList(eq(chatRoomId), eq(cursor), eq(size), eq(buyerId));
	}

	/**
	 * 메시지 목록 조회 실패 케이스를 검증합니다.
	 * 채팅방 참여자가 아닌 사용자가 조회 요청 시 403 Forbidden 응답을 확인합니다.
	 */
	@Test
	@DisplayName("메시지 목록 조회 실패 - 채팅방 참여자가 아님(403)")
	void getMessageList_Fail_AccessDenied() throws Exception {
		// given
		Long chatRoomId = 1L;
		Long buyerId = 10L;
		loginAsBuyer(buyerId);

		willThrow(new CustomException(ErrorCode.CHATROOM_ACCESS_DENIED))
			.given(messageService)
			.getMessageList(eq(chatRoomId), any(), any(), eq(buyerId));

		// when & then
		mockMvc.perform(get("/api/chat-rooms/{chatRoomId}/messages", chatRoomId)
				.param("cursor", "1050")
				.param("size", "50"))
			.andExpect(status().isForbidden())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.status").value(403))
			.andExpect(jsonPath("$.code").value(ErrorCode.CHATROOM_ACCESS_DENIED.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorCode.CHATROOM_ACCESS_DENIED.getMessage()))
			.andExpect(jsonPath("$.timestamp").exists());

		then(messageService).should(times(1))
			.getMessageList(eq(chatRoomId), eq(1050L), eq(50), eq(buyerId));
	}

	/**
	 * 메시지 목록 조회 실패 케이스를 검증합니다.
	 * 존재하지 않는 채팅방을 조회 요청할 경우 404 Not Found 응답을 확인합니다.
	 */
	@Test
	@DisplayName("메시지 목록 조회 실패 - 채팅방이 존재하지 않음(404)")
	void getMessageList_Fail_ChatRoomNotFound() throws Exception {
		// given
		Long chatRoomId = 999L;
		Long buyerId = 10L;
		loginAsBuyer(buyerId);

		willThrow(new CustomException(ErrorCode.CHATROOM_NOT_FOUND))
			.given(messageService)
			.getMessageList(eq(chatRoomId), any(), any(), eq(buyerId));

		// when & then
		mockMvc.perform(get("/api/chat-rooms/{chatRoomId}/messages", chatRoomId))
			.andExpect(status().isNotFound())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.code").value(ErrorCode.CHATROOM_NOT_FOUND.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorCode.CHATROOM_NOT_FOUND.getMessage()))
			.andExpect(jsonPath("$.timestamp").exists());

		then(messageService).should(times(1))
			.getMessageList(eq(chatRoomId), isNull(), isNull(), eq(buyerId));
	}

}
