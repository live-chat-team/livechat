package kr.sparta.livechat.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import kr.sparta.livechat.domain.entity.Product;
import kr.sparta.livechat.domain.role.ProductStatus;
import kr.sparta.livechat.dto.product.CreateProductRequest;
import kr.sparta.livechat.dto.product.CreateProductResponse;
import kr.sparta.livechat.dto.product.GetProductDetailResponse;
import kr.sparta.livechat.dto.product.GetProductListResponse;
import kr.sparta.livechat.dto.product.ProductListItem;
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

	/**
	 * 상품 중복 등록 요청 시 예외에 대한 검증 진행 -> PRODUCT_ALREADY_EXISTS 예외처리 필요
	 */
	@Test
	@DisplayName("상품 등록 실패 - 중복된 상품 등록 시에 대한 테스트 메서드")
	void FailCaseCreateProduct_DuplicateName() {
		//given
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
		given(productRepository.existsBySellerAndName(seller, req.getName())).willReturn(true);

		//when
		Throwable thrown = catchThrowable(() -> productService.createProduct(req, sellerId));

		//then
		assertThat(thrown).isInstanceOf(CustomException.class);
		CustomException ce = (CustomException)thrown;
		assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_ALREADY_EXISTS);

		verify(userRepository).findById(sellerId);
		verify(productRepository).existsBySellerAndName(seller, req.getName());
		verify(productRepository, never()).save(any(Product.class));
	}

	/**
	 * 상품 목록 조회 성공 케이스를 검증합니다.
	 * page/size 유효성 검증 -> 페이징 조회 호출 -> 응답 매핑 DTO 반환
	 */
	@Test
	@DisplayName("상품 목록 조회 성공 케이스")
	void SuccessCaseGetProductList() {
		//given
		int page = 0;
		int size = 20;

		User seller = User.builder()
			.email("seller@test.com")
			.name("판매자")
			.password("password123!")
			.role(Role.SELLER)
			.build();

		Product p1 = Product.builder()
			.name("상품1")
			.price(1000)
			.description("상품1 설명")
			.seller(seller)
			.build();

		Product p2 = Product.builder()
			.name("상품2")
			.price(5000)
			.description("상품2 설명")
			.seller(seller)
			.build();

		List<Product> products = List.of(p1, p2);
		Page<Product> pageResult = new PageImpl<>(
			products,
			PageRequest.of(page, size, Sort.by("createdAt").descending()), 2);

		given(productRepository.findAll(any(PageRequest.class))).willReturn(pageResult);

		//when
		GetProductListResponse res = productService.getProductList(page, size);

		//then
		assertThat(res).isNotNull();
		assertThat(res.getPage()).isEqualTo(page);
		assertThat(res.getSize()).isEqualTo(size);
		assertThat(res.getTotalElements()).isEqualTo(2L);
		assertThat(res.getTotalPages()).isEqualTo(1);
		assertThat(res.isHasNext()).isFalse();
		assertThat(res.getProductList()).isNotNull();
		assertThat(res.getProductList().size()).isEqualTo(2);

		ProductListItem first = res.getProductList().get(0);
		assertThat(first.getName()).isEqualTo("상품1");
		assertThat(first.getPrice()).isEqualTo(1000);

		verify(productRepository).findAll(any(PageRequest.class));
	}

	/**
	 * 페이징 파라미터 유효성 검증 실패 시 예외를 처리하는지에 대한 검증을 진행
	 */
	@Test
	@DisplayName("상품 목록 조회 실패 - page/size 유효성 검증 실패")
	void FailCaseGetProductList_BadPagination() {
		//given
		int page = -1;
		int size = 0;

		//when
		Throwable thrown = catchThrowable(() -> productService.getProductList(page, size));

		//then
		assertThat(thrown).isInstanceOf(CustomException.class);
		CustomException ce = (CustomException)thrown;
		assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.COMMON_BAD_PAGINATION);

		verifyNoInteractions(productRepository);
		verifyNoInteractions(userRepository);
	}

	/**
	 * 범위를 벗어난 페이지 요청 시에도 예외가 아니라 빈 목록이 반환되는지 검증
	 */
	@Test
	@DisplayName("상품 목록 조회 - 범위를 벗어난 페이지 요청 케이스 검증")
	void GetProductList_OutOfRangePage() {
		//given
		int page = 5;
		int size = 20;

		Page<Product> emptyPage = new PageImpl<>(
			List.of(),
			PageRequest.of(page, size, Sort.by("createdAt").descending()),
			0
		);

		given(productRepository.findAll(any(PageRequest.class))).willReturn(emptyPage);

		//when
		GetProductListResponse res = productService.getProductList(page, size);

		//then
		assertThat(res).isNotNull();
		assertThat(res.getPage()).isEqualTo(page);
		assertThat(res.getProductList()).isNotNull();
		assertThat(res.getProductList().size()).isEqualTo(0);
		assertThat(res.isHasNext()).isFalse();

		verify(productRepository).findAll(any(PageRequest.class));
	}

	/**
	 * 상품 상세 조회 성공 케이스를 검증합니다.
	 * productId 유효성 검증 -> 응답 매핑 DTO 반환
	 */
	@Test
	@DisplayName("상품 상세 조회 성공 케이스")
	void SuccessCaseGetProductDetail() {
		//given
		Long productId = 1L;
		LocalDateTime createdAt = LocalDateTime.parse("2025--12-09T14:06:47");

		User seller = mock(User.class);
		given(seller.getId()).willReturn(1L);

		Product product = mock(Product.class);
		given(product.getId()).willReturn(productId);
		given(product.getName()).willReturn("토르의 망치");
		given(product.getPrice()).willReturn(3000000);
		given(product.getDescription()).willReturn("선택받은 자만 들 수 있는 망치");
		given(product.getSeller()).willReturn(seller);
		given(product.getStatus()).willReturn(ProductStatus.ONSALE);
		given(product.getCreatedAt()).willReturn(createdAt);

		given(productRepository.findById(productId)).willReturn(Optional.of(product));

		//when
		GetProductDetailResponse res = productService.getProductDetail(productId);

		//then
		assertThat(res).isNotNull();
		assertThat(res.getProductId()).isEqualTo(productId);
		assertThat(res.getName()).isEqualTo("토르의 망치");
		assertThat(res.getPrice()).isEqualTo(3000000);
		assertThat(res.getDescription()).isEqualTo("선택받은 자만 들 수 있는 망치");
		assertThat(res.getSellerId()).isEqualTo(1L);
		assertThat(res.getStatus()).isEqualTo(ProductStatus.ONSALE);
		assertThat(res.getCreatedAt()).isEqualTo(createdAt);

		verify(productRepository).findById(productId);
	}
}
