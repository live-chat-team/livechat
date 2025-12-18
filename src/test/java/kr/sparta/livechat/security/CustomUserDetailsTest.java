package kr.sparta.livechat.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;

import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;

/**
 * CustomUserDetails 클래스에 대한 단위 테스트 클래스입니다.
 * Spring Security의 인터페이스 구현이 User 엔티티의 데이터를 캡슐화하고,
 * 역할 정보가 GrantedAuthority로 반환되는지 검증합니다.
 * CustomUserDetailsTest.java
 *
 * @author kimsehyun
 * @since 2025. 12. 16.
 */
public class CustomUserDetailsTest {

	@Mock
	private User mockBuyerUser;

	@Mock
	private User mockSellerUser;

	private CustomUserDetails buyerDetails;
	private CustomUserDetails sellerDetails;

	private static final Long BUYER_ID = 1L;
	private static final String BUYER_EMAIL = "buyer@test.com";
	private static final Long SELLER_ID = 2L;
	private static final String SELLER_EMAIL = "seller@test.com";
	private static final String ENCODED_PASSWORD = "{bcrypt}encodedPassword123";

	/**
	 * 각테스트 실행전에 Mock 객체를 초기화 하고,
	 * User Mock 객체를 설정하여ㅕ CustomUserDetails 인스턴스를 생성합니다.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		when(mockBuyerUser.getId()).thenReturn(BUYER_ID);
		when(mockBuyerUser.getEmail()).thenReturn(BUYER_EMAIL);
		when(mockBuyerUser.getPassword()).thenReturn(ENCODED_PASSWORD);
		when(mockBuyerUser.getRole()).thenReturn(Role.BUYER);

		buyerDetails = new CustomUserDetails(mockBuyerUser);

		when(mockSellerUser.getId()).thenReturn(SELLER_ID);
		when(mockSellerUser.getEmail()).thenReturn(SELLER_EMAIL);
		when(mockSellerUser.getPassword()).thenReturn(ENCODED_PASSWORD);
		when(mockSellerUser.getRole()).thenReturn(Role.SELLER);

		sellerDetails = new CustomUserDetails(mockSellerUser);
	}

	/**
	 * CustomUserDetails가 내부 User 엔티티의 ID를 올바르게 반환하는지 확인합니다.
	 */
	@Test
	@DisplayName("성공: CustomUserDetails가 User ID를 정확히 반환한다")
	void getUserId_Success() {
		assertEquals(BUYER_ID, buyerDetails.getUserId());
		assertEquals(SELLER_ID, sellerDetails.getUserId());
	}

	/**
	 * CustomUserDetails가 이메일을 Spring Security의 사용자 이름으로 올바르게 반환하는지 확인합니다.
	 */
	@Test
	@DisplayName("성공: CustomUserDetails가 이메일을 사용자 이름으로 반환한다 (getUsername)")
	void getUsername_Success() {
		assertEquals(BUYER_EMAIL, buyerDetails.getUsername());

	}

	/**
	 * CustomUserDetails가 암호화된 비밀번호를 올바르게 반환하는지 확인합니다.
	 */
	@Test
	@DisplayName("성공: CustomUserDetails가 암호화된 비밀번호를 반환한다 (getPassword)")
	void getPassword_Success() {
		assertEquals(ENCODED_PASSWORD, buyerDetails.getPassword());
	}

	/**
	 * Role(Buyer)이 Spring Security에서 ROLE_BUYER로 올바르게 반환되는지 확인합닏.
	 */
	@Test
	@DisplayName("성공: BUYER 역할이 GrantedAuthority로 정확히 매핑된다")
	void getAuthorities_BuyerRole_Success() {
		Collection<? extends GrantedAuthority> authorities = buyerDetails.getAuthorities();
		assertNotNull(authorities);
		assertEquals(1, authorities.size(), "권한은 하나여야 합니다.");
		assertTrue(authorities.stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.BUYER.name())),
			"권한은 'ROLE_BUYER'여야 합니다.");
		verify(mockBuyerUser, times(1)).getRole();
	}

	/**
	 * Role(SEller)이 Spring Security에서 ROLE_SELLERR로 올바르게 반환되는지 확인합닏.
	 */
	@Test
	@DisplayName("성공: SELLER 역할이 GrantedAuthority로 정확히 매핑된다")
	void getAuthorities_SellerRole_Success() {
		Collection<? extends GrantedAuthority> authorities = sellerDetails.getAuthorities();
		assertNotNull(authorities);
		assertEquals(1, authorities.size());

		assertTrue(authorities.stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.SELLER.name())),
			"권한은 'ROLE_SELLER'여야 합니다.");
		verify(mockSellerUser, times(1)).getRole();
	}
}
