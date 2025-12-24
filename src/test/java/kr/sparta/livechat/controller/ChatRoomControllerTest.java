package kr.sparta.livechat.controller;

import static kr.sparta.livechat.domain.role.ChatRoomStatus.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.hamcrest.Matchers;
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
import kr.sparta.livechat.domain.role.RoleInRoom;
import kr.sparta.livechat.dto.chatroom.ChatRoomListItem;
import kr.sparta.livechat.dto.chatroom.CreateChatRoomRequest;
import kr.sparta.livechat.dto.chatroom.CreateChatRoomResponse;
import kr.sparta.livechat.dto.chatroom.GetChatRoomDetailResponse;
import kr.sparta.livechat.dto.chatroom.GetChatRoomListResponse;
import kr.sparta.livechat.dto.chatroom.ParticipantsListItem;
import kr.sparta.livechat.dto.chatroom.ProductInfo;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
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

	private void loginAsSeller(Long userId) {
		loginAs(userId, Role.SELLER);
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

	/**
	 * 채팅방 생성 실패(요청 바디 검증 실패) 케이스를 검증
	 * 메시지 내용(Content)가 빈 경우 400 응답을 반환하고 서비스는 미호출 된 부분에 대한 검증을 진행합니다.
	 */
	@Test
	@DisplayName("채팅방 생성 실패 - content 빈 값인 경우")
	void createChatRoom_Fail_EmptyContent() throws Exception {
		//given
		Long productId = 1L;
		Long buyerId = 10L;
		loginAsBuyer(buyerId);

		CreateChatRoomRequest request = new CreateChatRoomRequest(null);

		String requestJson = objectMapper.writeValueAsString(request);

		// when & then
		mockMvc.perform(post("/api/products/{productId}/chat-rooms", productId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(chatRoomService);
	}

	/**
	 * 채팅방 생성 실패(구매자 권한 아님)의 경우를 검증
	 * 구매자가 아닌 사용자가 채팅방 생성 요청을 수행하면 서비스에서 예외가 발생하고
	 * 전역 예외 처리기가 403 에러와 ErrorResponse를 반환하는지 확인합니다.
	 */
	@Test
	@DisplayName("채팅방 생성 실패 - BUYER가 아닌 사용자가 생성 요청하는 경우")
	void createChatRoom_Fail_NotBuyer() throws Exception {
		//given
		Long productId = 1L;
		Long sellerId = 20L;
		loginAs(sellerId, Role.SELLER);

		CreateChatRoomRequest request = new CreateChatRoomRequest("문의드립니다.");
		String requestJson = objectMapper.writeValueAsString(request);

		willThrow(new CustomException(ErrorCode.CHATROOM_CREATE_ACCESS_DENIED))
			.given(chatRoomService)
			.createChatRoom(eq(productId), eq(sellerId), eq("문의드립니다."));

		// when & then
		mockMvc.perform(post("/api/products/{productId}/chat-rooms", productId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isForbidden())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.status").value(403))
			.andExpect(jsonPath("$.code").value(ErrorCode.CHATROOM_CREATE_ACCESS_DENIED.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorCode.CHATROOM_CREATE_ACCESS_DENIED.getMessage()))
			.andExpect(jsonPath("$.timestamp").exists());

		then(chatRoomService).should(times(1))
			.createChatRoom(eq(productId), eq(sellerId), eq("문의드립니다."));
	}

	/**
	 * 기 생성된 채팅방에 대한 상담 채팅방 생성 요청 시 중복 에러코드 응답 확인
	 */
	@Test
	@DisplayName("채팅방 생성 실패 - 이미 열린 채팅방이 존재하는 경우")
	void createChatRoom_Fail_AlreadyExists() throws Exception {
		//given
		Long productId = 1L;
		Long buyerId = 10L;
		loginAsBuyer(buyerId);

		CreateChatRoomRequest request = new CreateChatRoomRequest("문의드립니다.");
		String requestJson = objectMapper.writeValueAsString(request);

		willThrow(new CustomException(ErrorCode.CHATROOM_ALREADY_EXISTS))
			.given(chatRoomService)
			.createChatRoom(eq(productId), eq(buyerId), eq("문의드립니다."));

		// when & then
		mockMvc.perform(post("/api/products/{productId}/chat-rooms", productId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isConflict())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.status").value(409))
			.andExpect(jsonPath("$.code").value(ErrorCode.CHATROOM_ALREADY_EXISTS.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorCode.CHATROOM_ALREADY_EXISTS.getMessage()))
			.andExpect(jsonPath("$.timestamp").exists());

		then(chatRoomService).should(times(1))
			.createChatRoom(eq(productId), eq(buyerId), eq("문의드립니다."));

	}

	/**
	 * 채팅방 목록 조회 성공 케이스를 검증합니다.
	 * <p>
	 * 인증된 사용자가 채팅방 목록 조회 요청을 수행하면 200(OK) 상태와 함께 서비스가 반환한 응답 DTO가 JSON으로 직렬화되어 내려오는지 확인합니다.
	 * </p>
	 */
	@Test
	@DisplayName("채팅방 목록 조회 성공 - 200 응답과 목록 반환")
	void getChatRoomList_Success() throws Exception {
		// given
		Long buyerId = 10L;
		loginAsBuyer(buyerId);

		int page = 0;
		int size = 20;

		// 응답 DTO mock(필요한 getter만 stub)
		GetChatRoomListResponse response = mock(GetChatRoomListResponse.class);
		given(response.getPage()).willReturn(page);
		given(response.getSize()).willReturn(size);
		given(response.getTotalElements()).willReturn(1L);
		given(response.getTotalPages()).willReturn(1);
		given(response.isHasNext()).willReturn(false);

		ChatRoomListItem item = mock(ChatRoomListItem.class);
		given(item.getChatRoomId()).willReturn(1L);
		given(item.getProductName()).willReturn("상품명");
		given(item.getOpponentName()).willReturn("상대방");
		given(item.getLastMessageSentAt()).willReturn(LocalDateTime.parse("2025-12-20T12:00:00"));
		given(response.getChatRoomList()).willReturn(List.of(item));

		given(chatRoomService.getChatRoomList(eq(buyerId), eq(page), eq(size)))
			.willReturn(response);

		// when & then
		mockMvc.perform(get("/api/chat-rooms")
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size)))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.page").value(0))
			.andExpect(jsonPath("$.size").value(20))
			.andExpect(jsonPath("$.totalElements").value(1))
			.andExpect(jsonPath("$.totalPages").value(1))
			.andExpect(jsonPath("$.hasNext").value(false))

			.andExpect(jsonPath("$.chatRoomList[0].chatRoomId").value(1))
			.andExpect(jsonPath("$.chatRoomList[0].productName").value("상품명"))
			.andExpect(jsonPath("$.chatRoomList[0].opponentName").value("상대방"))
			.andExpect(jsonPath("$.chatRoomList[0].lastMessageSentAt").exists());

		then(chatRoomService).should(times(1))
			.getChatRoomList(eq(buyerId), eq(page), eq(size));
	}

	/**
	 * 채팅방 생성 실패(요청 바디 검증 실패) 케이스를 검증합니다.
	 * <p>
	 * content가 비어있는 요청을 전송하면 400(Bad Request)을 반환하고 서비스는 호출되지 않는지 확인합니다.
	 * </p>
	 */
	@Test
	@DisplayName("채팅방 목록 조회 실패 - page/size 파라미터가 유효하지 않은 경우")
	void getChatRoomList_Fail_InvalidPaging() throws Exception {
		// given
		Long buyerId = 10L;
		loginAsBuyer(buyerId);

		int page = -1;
		int size = 0;

		willThrow(new CustomException(ErrorCode.COMMON_BAD_PAGINATION))
			.given(chatRoomService)
			.getChatRoomList(eq(buyerId), eq(page), eq(size));

		// when & then
		mockMvc.perform(get("/api/chat-rooms")
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size)))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.code").value(ErrorCode.COMMON_BAD_PAGINATION.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorCode.COMMON_BAD_PAGINATION.getMessage()))
			.andExpect(jsonPath("$.timestamp").exists());

		then(chatRoomService).should(times(1))
			.getChatRoomList(eq(buyerId), eq(page), eq(size));
	}

	/**
	 * 채팅방 상세 조회 성공 케이스를 검증합니다.
	 * <p>
	 * 인증된 사용자가 특정 채팅방 상세 조회 요청을 수행하면 200(OK) 상태와 함께
	 * 서비스가 반환한 응답 DTO가 JSON으로 직렬화되어 내려오는지 확인합니다.
	 * </p>
	 */
	@Test
	@DisplayName("채팅방 상세 조회 성공 - 200 응답과 목록 반환")
	void getChatRoomDetail_Success() throws Exception {
		// given
		Long chatRoomId = 1L;
		Long buyerId = 10L;
		loginAsBuyer(buyerId);

		GetChatRoomDetailResponse response = mock(GetChatRoomDetailResponse.class);
		given(response.getChatRoomId()).willReturn(chatRoomId);
		given(response.getStatus()).willReturn(OPEN);
		given(response.getOpenedAt()).willReturn(LocalDateTime.parse("2025-12-10T09:00:00"));
		given(response.getClosedAt()).willReturn(null);

		ProductInfo productInfo = mock(ProductInfo.class);
		given(productInfo.getProductId()).willReturn(1L);
		given(productInfo.getProductName()).willReturn("토르의 망치");
		given(response.getProductInfo()).willReturn(productInfo);

		ParticipantsListItem buyerItem = mock(ParticipantsListItem.class);
		given(buyerItem.getUserId()).willReturn(buyerId);
		given(buyerItem.getUserName()).willReturn("홍길동");
		given(buyerItem.getRoleInRoom()).willReturn(RoleInRoom.BUYER);

		ParticipantsListItem sellerItem = mock(ParticipantsListItem.class);
		given(sellerItem.getUserId()).willReturn(20L);
		given(sellerItem.getUserName()).willReturn("김토르");
		given(sellerItem.getRoleInRoom()).willReturn(RoleInRoom.SELLER);

		given(response.getParticipantsList()).willReturn(List.of(buyerItem, sellerItem));

		given(chatRoomService.getChatRoomDetail(eq(chatRoomId), eq(buyerId))).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/chat-rooms/{chatRoomId}", chatRoomId))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.chatRoomId").value(1))
			.andExpect(jsonPath("$.status").value("OPEN"))
			.andExpect(jsonPath("$.openedAt").exists())
			.andExpect(jsonPath("$.closedAt").value(Matchers.nullValue()))
			.andExpect(jsonPath("$.productInfo.productId").value(1))
			.andExpect(jsonPath("$.productInfo.productName").value("토르의 망치"))
			.andExpect(jsonPath("$.participantsList").isArray())
			.andExpect(jsonPath("$.participantsList.length()").value(2))
			.andExpect(jsonPath("$.participantsList[0].userId").exists())
			.andExpect(jsonPath("$.participantsList[0].userName").exists())
			.andExpect(jsonPath("$.participantsList[0].roleInRoom").exists());

		then(chatRoomService).should(times(1)).getChatRoomDetail(eq(chatRoomId), eq(buyerId));
	}

	/**
	 * 채팅방 상세 조회 실패(요청 식별자 유효성 오류) 케이스를 검증합니다.
	 */
	@Test
	@DisplayName("채팅방 상세 조회 실패 - 요청 식별자가 올바르지 않은 경우")
	void getChatRoomDetail_Fail_InvalidInput() throws Exception {
		// given
		Long buyerId = 10L;
		loginAsBuyer(buyerId);

		Long invalidChatRoomId = 0L;

		willThrow(new CustomException(ErrorCode.CHATROOM_INVALID_INPUT))
			.given(chatRoomService)
			.getChatRoomDetail(eq(invalidChatRoomId), eq(buyerId));

		// when & then
		mockMvc.perform(get("/api/chat-rooms/{chatRoomId}", invalidChatRoomId))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.code").value(ErrorCode.CHATROOM_INVALID_INPUT.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorCode.CHATROOM_INVALID_INPUT.getMessage()))
			.andExpect(jsonPath("$.timestamp").exists());

		then(chatRoomService).should(times(1)).getChatRoomDetail(eq(invalidChatRoomId), eq(buyerId));
	}

	/**
	 * 채팅방 상세 조회 실패(권한 없음) 케이스를 검증합니다.
	 */
	@Test
	@DisplayName("채팅방 상세 조회 실패 - 채팅방 상세조회 권한이 없는 경우")
	void getChatRoomDetail_Fail_AccessDenied() throws Exception {
		// given
		Long buyerId = 10L;
		loginAsBuyer(buyerId);

		Long chatRoomId = 1L;

		willThrow(new CustomException(ErrorCode.CHATROOM_ACCESS_DENIED))
			.given(chatRoomService)
			.getChatRoomDetail(eq(chatRoomId), eq(buyerId));

		// when & then
		mockMvc.perform(get("/api/chat-rooms/{chatRoomId}", chatRoomId))
			.andExpect(status().isForbidden())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.status").value(403))
			.andExpect(jsonPath("$.code").value(ErrorCode.CHATROOM_ACCESS_DENIED.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorCode.CHATROOM_ACCESS_DENIED.getMessage()))
			.andExpect(jsonPath("$.timestamp").exists());

		then(chatRoomService).should(times(1)).getChatRoomDetail(eq(chatRoomId), eq(buyerId));

	}

	/**
	 * 채팅방 상세 조회 실패(채팅방 없음) 케이스를 검증합니다.
	 */
	@Test
	@DisplayName("채팅방 상세 조회 실패 - 채팅방 조회를 할 수 없는 경우")
	void getChatRoomDetail_Fail_NotFoundChatRoom() throws Exception {
		// given
		Long buyerId = 10L;
		loginAsBuyer(buyerId);

		Long chatRoomId = 999L;

		willThrow(new CustomException(ErrorCode.CHATROOM_NOT_FOUND))
			.given(chatRoomService)
			.getChatRoomDetail(eq(chatRoomId), eq(buyerId));

		// when & then
		mockMvc.perform(get("/api/chat-rooms/{chatRoomId}", chatRoomId))
			.andExpect(status().isNotFound())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.code").value(ErrorCode.CHATROOM_NOT_FOUND.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorCode.CHATROOM_NOT_FOUND.getMessage()))
			.andExpect(jsonPath("$.timestamp").exists());

		then(chatRoomService).should(times(1)).getChatRoomDetail(eq(chatRoomId), eq(buyerId));

	}
}
