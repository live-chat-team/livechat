package kr.sparta.livechat.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import kr.sparta.livechat.domain.entity.Message;
import kr.sparta.livechat.domain.role.MessageType;
import kr.sparta.livechat.dto.message.GetChatMessageListResponse;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.ChatRoomParticipantRepository;
import kr.sparta.livechat.repository.ChatRoomRepository;
import kr.sparta.livechat.repository.MessageRepository;

/**
 * MessageServiceTest 테스트 클래스입니다.
 * <p>
 * 대상 클래스(또는 메서드): {@link MessageService#getMessageList(Long, Long, Integer, Long)}
 * </p>
 *
 * @author 재원
 * @since 2025. 12. 23.
 */
@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

	@InjectMocks
	private MessageService messageService;

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private MessageRepository messageRepository;

	@Mock
	private ChatRoomParticipantRepository participantRepository;

	/**
	 * 메시지 목록 조회 케이스를 검증합니다. 최초 목록 조회 시를 기준으로 검증조건을 잘 통과하는지를 점검합니다.
	 */
	@Test
	@DisplayName("메시지 목록 조회 성공 - cursor, size null")
	void SuccessGetMessageList() {
		// given
		Long chatRoomId = 1L;
		Long currentUserId = 1L;

		given(chatRoomRepository.existsById(chatRoomId)).willReturn(true);
		given(participantRepository.existsByRoomIdAndUserId(chatRoomId, currentUserId))
			.willReturn(true);

		User writer = mock(User.class);
		given(writer.getId()).willReturn(999L);

		Message m1 = mock(Message.class);
		given(m1.getId()).willReturn(120L);
		given(m1.getWriter()).willReturn(writer);
		given(m1.getType()).willReturn(MessageType.TEXT);
		given(m1.getContent()).willReturn("최근 메시지");
		given(m1.getSentAt()).willReturn(LocalDateTime.now());

		Message m2 = mock(Message.class);
		given(m2.getId()).willReturn(110L);
		given(m2.getWriter()).willReturn(writer);
		given(m2.getType()).willReturn(MessageType.TEXT);
		given(m2.getContent()).willReturn("이전 메시지");
		given(m2.getSentAt()).willReturn(LocalDateTime.now().minusSeconds(1));

		Slice<Message> slice = new SliceImpl<>(List.of(m1, m2), PageRequest.of(0, 50), false);

		given(messageRepository.findByRoom_Id(eq(chatRoomId), any(Pageable.class)))
			.willReturn(slice);

		// when
		GetChatMessageListResponse response =
			messageService.getMessageList(chatRoomId, null, null, currentUserId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getMessageList()).hasSize(2);
		assertThat(response.getNextCursor()).isNull();

		verify(chatRoomRepository).existsById(chatRoomId);
		verify(participantRepository).existsByRoomIdAndUserId(chatRoomId, currentUserId);
		verify(messageRepository).findByRoom_Id(eq(chatRoomId), any(Pageable.class));
		verify(messageRepository, never()).findByRoom_IdAndIdLessThan(anyLong(), anyLong(), any(Pageable.class));
	}

	/**
	 * cursor를 기반으로 이전 메시지의 목록을 반환하는 성공 케이스를 검증합니다.
	 */
	@Test
	@DisplayName("메시지 목록 조회 성공 - cursor 존재 시 이전 메시지 조회")
	void SuccessGetMessageList_WithCursor() {
		//given
		Long chatRoomId = 1L;
		Long currentUserId = 1L;
		Long cursor = 1050L;
		Integer size = 2;

		given(chatRoomRepository.existsById(chatRoomId)).willReturn(true);
		given(participantRepository.existsByRoomIdAndUserId(chatRoomId, currentUserId)).willReturn(true);

		User writer = mock(User.class);
		given(writer.getId()).willReturn(999L);

		Message m1 = mock(Message.class);
		given(m1.getId()).willReturn(1049L);
		given(m1.getWriter()).willReturn(writer);
		given(m1.getType()).willReturn(MessageType.TEXT);
		given(m1.getContent()).willReturn("cursor 이전 메시지 1");
		given(m1.getSentAt()).willReturn(LocalDateTime.now());

		Message m2 = mock(Message.class);
		given(m2.getId()).willReturn(1048L);
		given(m2.getWriter()).willReturn(writer);
		given(m2.getType()).willReturn(MessageType.TEXT);
		given(m2.getContent()).willReturn("cursor 이전 메시지 2");
		given(m2.getSentAt()).willReturn(LocalDateTime.now().minusSeconds(1));

		Slice<Message> slice = new SliceImpl<>(List.of(m1, m2), PageRequest.of(0, size), true);

		given(messageRepository.findByRoom_IdAndIdLessThan(eq(chatRoomId), eq(cursor), any(Pageable.class)))
			.willReturn(slice);

		//when
		GetChatMessageListResponse response =
			messageService.getMessageList(chatRoomId, cursor, size, currentUserId);

		//then
		assertThat(response).isNotNull();
		assertThat(response.getMessageList()).hasSize(2);
		assertThat(response.getNextCursor()).isEqualTo(1048L);

		verify(messageRepository, never()).findByRoom_Id(anyLong(), any(Pageable.class));
		verify(messageRepository).findByRoom_IdAndIdLessThan(eq(chatRoomId), eq(cursor), any(Pageable.class));
	}

	/**
	 * 메시지 목록 조회 요청 간 채팅방이 존재하지 않는 경우의 실패 케이스를 검증합니다.
	 * 올바른 에러코드와 서비스 미호출 여부를 검증합니다.
	 */
	@Test
	@DisplayName("메시지 목록 조회 실패 - 채팅방이 없는 경우")
	void FailGetMassageList_ChatRoomNotFound() {
		//given
		Long chatRoomId = 1L;
		Long currentUserId = 1L;

		given(chatRoomRepository.existsById(chatRoomId)).willReturn(false);

		//when
		Throwable thrown = catchThrowable(() ->
			messageService.getMessageList(chatRoomId, null, null, currentUserId)
		);

		//then
		assertThat(thrown).isInstanceOf(CustomException.class);
		CustomException ce = (CustomException)thrown;
		assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.CHATROOM_NOT_FOUND);

		verify(chatRoomRepository).existsById(chatRoomId);
		verifyNoInteractions(participantRepository, messageRepository);
	}

	/**
	 * 메시지 목록 조회 실패 케이스를 검증합니다. 채팅방 참여자가 아닌 경우 목록 조회 요청 시 접근 예외처리 여부 및 서비스 미호출을 검증합니다.
	 */
	@Test
	@DisplayName("메시지 목록 조회 실패 - 해당 채팅방 참여자가 아닌 경우")
	void FailGetMessageList_AccessDenied() {
		//given
		Long chatRoomId = 1L;
		Long currentUserId = 99L;

		given(chatRoomRepository.existsById(chatRoomId)).willReturn(true);
		given(participantRepository.existsByRoomIdAndUserId(chatRoomId, currentUserId)).willReturn(false);

		//when
		Throwable thrown = catchThrowable(() ->
			messageService.getMessageList(chatRoomId, null, null, currentUserId)
		);

		//then
		assertThat(thrown).isInstanceOf(CustomException.class);
		CustomException ce = (CustomException)thrown;
		assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.CHATROOM_ACCESS_DENIED);

		verify(participantRepository).existsByRoomIdAndUserId(chatRoomId, currentUserId);
		verifyNoInteractions(messageRepository);
	}

}

