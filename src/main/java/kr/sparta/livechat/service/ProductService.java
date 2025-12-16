package kr.sparta.livechat.service;

import org.springframework.stereotype.Service;

import kr.sparta.livechat.domain.entity.Product;
import kr.sparta.livechat.dto.product.CreateProductRequest;
import kr.sparta.livechat.dto.product.CreateProductResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.global.exception.product.ProductException;
import kr.sparta.livechat.repository.ProductRepository;
import kr.sparta.livechat.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * 상품(Product) 등록 관련 비즈니스 로직을 처리하는 서비스입니다.
 * <p>
 * * 판매자(User) 조회, 권한 검증, 중복 상품 검증 후 상품을 저장합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 13.
 */
@Service
@RequiredArgsConstructor
public class ProductService {
	private final ProductRepository productRepository;
	private final UserRepository userRepository;

	/**
	 * 상품을 등록합니다.
	 * <p>
	 * 판매자(User)를 조회한 뒤, 판매자 권한 및 중복 상품 여부를 검증하고 상품을 저장합니다.
	 * </p>
	 *
	 * @param request       상품 등록 요청 데이터
	 * @param currentUserId 판매자 식별자(임시 단계에서는 고정값을 전달받을 수 있습니다)
	 * @return 등록된 상품 정보 응답
	 */
	public CreateProductResponse createProduct(CreateProductRequest request, Long currentUserId) {

		User currentUser = userRepository.findById(currentUserId)
			.orElseThrow(() -> new ProductException(ErrorCode.AUTH_USER_NOT_FOUND));

		if (currentUser.getRole() != Role.SELLER) {
			throw new ProductException(ErrorCode.PRODUCT_ACCESS_DENIED);
		}

		if (productRepository.existsBySellerAndName(currentUser, request.getName())) {
			throw new ProductException(ErrorCode.PRODUCT_ALREADY_EXISTS);
		}

		Product product = Product.create(currentUser, request);
		Product saved = productRepository.save(product);

		return CreateProductResponse.from(saved);
	}

}
