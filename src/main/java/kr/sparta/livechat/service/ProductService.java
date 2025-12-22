package kr.sparta.livechat.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.sparta.livechat.domain.entity.Product;
import kr.sparta.livechat.domain.role.ProductStatus;
import kr.sparta.livechat.dto.product.CreateProductRequest;
import kr.sparta.livechat.dto.product.CreateProductResponse;
import kr.sparta.livechat.dto.product.GetProductDetailResponse;
import kr.sparta.livechat.dto.product.GetProductListResponse;
import kr.sparta.livechat.dto.product.PatchProductRequest;
import kr.sparta.livechat.dto.product.PatchProductResponse;
import kr.sparta.livechat.dto.product.ProductListItem;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
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
	 * JWT 인증을 통해 식별된 사용자를 기준으로 판매자를 조회한 뒤, 판매자 권한 및 중복 상품 여부를 검증하고 상품을 저장합니다.
	 * </p>
	 *
	 * @param request       상품 등록 요청 데이터
	 * @param currentUserId JWT 인증을 통해 식별된 사용자 식별자
	 * @return 등록된 상품 정보 응답
	 * @throws CustomException 사용자를 찾을 수 없거나, 판매자 권한이 없거나, 중복된 상품명이거나, 유효하지 않은 입력인 경우
	 */
	@Transactional
	public CreateProductResponse createProduct(CreateProductRequest request, Long currentUserId) {
		validateCreateRequest(request);

		User seller = getSellerOrThrow(currentUserId);

		if (productRepository.existsBySellerAndName(seller, request.getName())) {
			throw new CustomException(ErrorCode.PRODUCT_ALREADY_EXISTS);
		}

		Product product = Product.create(seller, request);
		Product saved = productRepository.save(product);

		return CreateProductResponse.from(saved);
	}

	private void validateCreateRequest(CreateProductRequest request) {
		if (request == null) {
			throw new CustomException(ErrorCode.PRODUCT_INVALID_INPUT);
		}

		if (request.getName() == null || request.getName().isBlank()) {
			throw new CustomException(ErrorCode.PRODUCT_INVALID_INPUT);
		}

		if (request.getPrice() == null || request.getPrice() < 0) {
			throw new CustomException(ErrorCode.PRODUCT_INVALID_INPUT);
		}

		if (request.getDescription() != null && request.getDescription().isBlank()) {
			throw new CustomException(ErrorCode.PRODUCT_INVALID_INPUT);
		}
	}

	private User getSellerOrThrow(Long currentUserId) {
		if (currentUserId == null || currentUserId <= 0) {
			throw new CustomException(ErrorCode.AUTH_USER_NOT_FOUND);
		}

		User user = userRepository.findById(currentUserId)
			.orElseThrow(() -> new CustomException(ErrorCode.AUTH_USER_NOT_FOUND));

		if (user.getRole() != Role.SELLER) {
			throw new CustomException(ErrorCode.PRODUCT_ACCESS_DENIED);
		}

		return user;
	}

	/**
	 * 상품 목록을 페이징하여 조회합니다.
	 *
	 * @param page 조회하는 상품 목록 페이지
	 * @param size 조회하는 상품 개수
	 * @return 상품 목록과 페이징 정보를 포함한 응답 DTO
	 */
	public GetProductListResponse getProductList(int page, int size) {

		if (page < 0 || size <= 0) {
			throw new CustomException(ErrorCode.COMMON_BAD_PAGINATION);
		}

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		Page<Product> pageResult = productRepository.findAllByStatusNot(ProductStatus.DELETED, pageable);

		List<ProductListItem> productList = pageResult.getContent().stream()
			.map(ProductListItem::from)
			.toList();

		return new GetProductListResponse(pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements(),
			pageResult.getTotalPages(), pageResult.hasNext(), productList);
	}

	/**
	 * 특정 상품의 상세 정보를 조회합니다.
	 *
	 * @param productId 조회할 상품 식별자
	 * @return 상품 상세 조회 응답 DTO
	 * @throws CustomException 상품이 존재하지 않는 경우
	 */
	public GetProductDetailResponse getProductDetail(Long productId) {

		if (productId == null || productId <= 0) {
			throw new CustomException((ErrorCode.PRODUCT_INVALID_INPUT));
		}

		Product product = productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)
			.orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

		return GetProductDetailResponse.from(product);
	}

	/**
	 * 상품 정보를 부분 수정(PATCH)합니다.
	 * <p>
	 * 판매자 권한 및 상품 소유자 검증 후, 요청에 포함된 필드만 수정합니다.
	 * </p>
	 *
	 * @param productId     수정할 상품 식별자
	 * @param request       부분 수정 요청 DTO (모든 필드 nullable)
	 * @param currentUserId 요청 사용자(판매자) 식별자
	 * @return 수정된 상품 정보 응답 DTO
	 * @throws CustomException 400(입력값/빈 바디), 403(권한/소유자 불일치), 404(상품 없음)
	 */
	@Transactional
	public PatchProductResponse patchProduct(Long productId, PatchProductRequest request, Long currentUserId) {

		if (productId == null || productId <= 0) {
			throw new CustomException(ErrorCode.PRODUCT_INVALID_INPUT);
		}

		User currentUser = userRepository.findById(currentUserId)
			.orElseThrow(() -> new CustomException(ErrorCode.AUTH_USER_NOT_FOUND));

		if (currentUser.getRole() != Role.SELLER) {
			throw new CustomException(ErrorCode.PRODUCT_ACCESS_DENIED);
		}

		if (request == null || request.isEmpty()) {
			throw new CustomException(ErrorCode.PRODUCT_INVALID_INPUT);
		}

		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

		if (product.getStatus() == ProductStatus.DELETED) {
			throw new CustomException(ErrorCode.PRODUCT_ALREADY_DELETED);
		}

		if (!product.getSeller().getId().equals(currentUser.getId())) {
			throw new CustomException(ErrorCode.PRODUCT_ACCESS_DENIED);
		}

		product.patch(
			request.getName(),
			request.getPrice(),
			request.getDescription(),
			request.getStatus()
		);

		return PatchProductResponse.from(product);
	}

	/**
	 * 상품 삭제 (Soft Delete)를 진행합니다.
	 *
	 * @param productId     상품 고유 식별자
	 * @param currentUserId 로그인한 사용자의 식별자
	 */
	@Transactional
	public void deleteProduct(Long productId, Long currentUserId) {

		if (productId == null || productId <= 0) {
			throw new CustomException(ErrorCode.PRODUCT_INVALID_INPUT);
		}

		User currentUser = userRepository.findById(currentUserId)
			.orElseThrow(() -> new CustomException(ErrorCode.AUTH_USER_NOT_FOUND));

		if (currentUser.getRole() != Role.SELLER) {
			throw new CustomException(ErrorCode.PRODUCT_ACCESS_DENIED);
		}

		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

		if (product.getStatus() == ProductStatus.DELETED) {
			throw new CustomException(ErrorCode.PRODUCT_ALREADY_DELETED);
		}

		if (!product.getSeller().getId().equals(currentUser.getId())) {
			throw new CustomException(ErrorCode.PRODUCT_ACCESS_DENIED);
		}

		product.delete();
	}
}
