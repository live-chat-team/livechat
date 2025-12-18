package kr.sparta.livechat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.sparta.livechat.dto.product.CreateProductRequest;
import kr.sparta.livechat.dto.product.CreateProductResponse;
import kr.sparta.livechat.dto.product.GetProductDetailResponse;
import kr.sparta.livechat.dto.product.GetProductListResponse;
import kr.sparta.livechat.dto.product.PatchProductRequest;
import kr.sparta.livechat.dto.product.PatchProductResponse;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.security.CustomUserDetails;
import kr.sparta.livechat.service.ProductService;
import lombok.RequiredArgsConstructor;

/**
 * ProductController 클래스입니다.
 * <p>
 * 상품관리에 대한 CRUD API 요청을 처리하는 컨트롤러입니다.
 * 현재 기준으로는 상품 등록에 대한 API 만 구현되어 있습니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 13.
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
	private final ProductService productService;

	/**
	 * 상품을 등록합니다.
	 * JWT 인증된 사용자 기준으로 sellerId를 추출하여 등록합니다.
	 *
	 * @param request 상품 등록에 필요한 요청 데이터
	 * @return 등록된 상품 정보 응답
	 */
	@PostMapping
	public ResponseEntity<CreateProductResponse> createProduct(
		@Valid @RequestBody CreateProductRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		CreateProductResponse response =
			productService.createProduct(request, userDetails.getUserId());

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 등록된 상품 리스트를 조회합니다.
	 * 조회 시에는 모든 사용자들이 조회할 수 있습니다.
	 *
	 * @param page 상품 목록 조회 페이지 (기본 0페이지)
	 * @param size 상품 목록 조회 개수 (기본 20개 단위)
	 * @return 등록된 상품 목록 반환
	 */
	@GetMapping
	public ResponseEntity<GetProductListResponse> getProductList(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		GetProductListResponse response = productService.getProductList(page, size);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * 특정 상품의 상세 정보를 조회합니다.
	 * 로그인 여부와 상관없이 모든 사용자가 조회할 수 있습니다.
	 *
	 * @param productId 조회할 상품 식별자
	 * @return 상품 상세 조회 응답
	 */
	@GetMapping("/{productId}")
	public ResponseEntity<GetProductDetailResponse> getProductDetail(
		@PathVariable Long productId
	) {
		GetProductDetailResponse response = productService.getProductDetail(productId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * 상품 정보를 부분 수정합니다.
	 * <p>
	 * JWT 인증을 통해 인증된 사용자 정보를 기반으로 상품 수정을 처리합니다.
	 * </p>
	 *
	 * @param productId   수정할 상품 식별자
	 * @param request     상품 수정 요청 데이터(부분 수정)
	 * @param userDetails 인증된 사용자 정보
	 * @return 수정된 상품 정보 응답
	 */
	@PatchMapping("{productId}")
	public ResponseEntity<PatchProductResponse> patchProduct(
		@PathVariable Long productId,
		@RequestBody PatchProductRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		if (request == null || request.isEmpty()) {
			throw new CustomException(ErrorCode.PRODUCT_INVALID_INPUT);
		}

		PatchProductResponse response =
			productService.patchProduct(productId, request, userDetails.getUserId());

		return ResponseEntity.ok(response);
	}
}
