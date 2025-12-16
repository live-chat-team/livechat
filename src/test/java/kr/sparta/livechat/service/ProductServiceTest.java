package kr.sparta.livechat.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.sparta.livechat.domain.entity.Product;
import kr.sparta.livechat.dto.product.CreateProductRequest;
import kr.sparta.livechat.dto.product.CreateProductResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.ProductRepository;
import kr.sparta.livechat.repository.UserRepository;

/**
 * ProductServiceTest 테스트 클래스입니다.
 * <p>
 * 대상 클래스(또는 메서드): {@link ProductService#createProduct(CreateProductRequest, Long)}
 * 판매자의 고유 식별자를 임시 값으로 전달하는 흐름을 검증합니다.
 * </p>
 *
 * @author 재원
 * @since 2025. 12. 16.
 */
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

	@Mock
	ProductRepository productRepository;

	@Mock
	UserRepository userRepository;

	@InjectMocks
	ProductService productService;

	/**
	 * 상품 등록 성공 시나리오를 검증합니다.
	 * 판매자 조회 성공 -> SELLER 권한 검증 -> 중복 상품 없음 -> 저장 호출 -> 응답 DTO 반환
	 */
	@Test
	@DisplayName("상품 등록 성공 케이스에 대한 테스트 메서드")
	void SuccessCaseCreateProduct() {
		// given
		Long sellerId = 1L;

		User seller = User.builder()
			.email("thor@realhammer.com")
			.name("토르")
			.password("realthunder123!")
			.role(Role.SELLER)
			.build();

		CreateProductRequest req = new CreateProductRequest(
			"토르의 망치", 3000000, "선택받은 자만 들 수 있는 망치");

		given(userRepository.findById(sellerId)).willReturn(Optional.of(seller));
		given(productRepository.existsBySellerAndName(seller, req.getName())).willReturn(false);
		given(productRepository.save(any(Product.class))).willAnswer(inv -> inv.getArgument(0));

		// when
		CreateProductResponse res = productService.createProduct(req, sellerId);

		// then
		assertThat(res).isNotNull();
		assertThat(res.getName()).isEqualTo(req.getName());
		assertThat(res.getPrice()).isEqualTo(req.getPrice());

		verify(userRepository).findById(sellerId);
		verify(productRepository).existsBySellerAndName(seller, req.getName());
		verify(productRepository).save(any(Product.class));
	}

	/**
	 * 판매자 조회 실패 시 예외에 대한 검증 진행 -> USER_NOT_FOUND 예외처리 필요
	 */
	@Test
	@DisplayName("상품 등록 실패 - 판매자 조회 실패 시에 대한 테스트 메서드")
	void FailCaseCreateProduct_UserNotFound() {
		//given
		Long sellerId = 1L;
		CreateProductRequest req = new CreateProductRequest(
			"토르의 망치", 3000000, "선택받은 자만 들 수 있는 망치");

		given(userRepository.findById(sellerId)).willReturn(Optional.empty());

		// when
		Throwable thrown = catchThrowable(() -> productService.createProduct(req, sellerId));

		// then
		assertThat(thrown).isInstanceOf(CustomException.class);
		CustomException ce = (CustomException)thrown;
		assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.AUTH_USER_NOT_FOUND);

		verify(userRepository).findById(sellerId);
		verifyNoInteractions(productRepository);
	}

	/**
	 * 판매자 권한 조회 실패 시 예외에 대한 검증 진행 -> PRODUCT_ACCESS_DENIED 예외처리 필요
	 */
	@Test
	@DisplayName("상품 등록 실패 - 판매자 권한 아닐 시에 대한 테스트 메서드")
	void FailCaseCreateProduct_Product_Access_Denied() {
		//given
		Long sellerId = 1L;

		User buyer = User.builder()
			.email("buyer@test.com")
			.name("구매자")
			.password("wantbuy123!")
			.role(Role.BUYER)
			.build();

		CreateProductRequest req = new CreateProductRequest(
			"토르의 망치", 3000000, "선택받은 자만 들 수 있는 망치");

		given(userRepository.findById(sellerId)).willReturn(Optional.of(buyer));

		//when
		Throwable thrown = catchThrowable(() -> productService.createProduct(req, sellerId));

		//then
		assertThat(thrown).isInstanceOf(CustomException.class);
		CustomException ce = (CustomException)thrown;
		assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_ACCESS_DENIED);

		verify(userRepository).findById(sellerId);
		verifyNoInteractions(productRepository);
	}
}
