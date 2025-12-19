package kr.sparta.livechat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.sparta.livechat.dto.product.CreateProductRequest;
import kr.sparta.livechat.dto.product.CreateProductResponse;
import kr.sparta.livechat.dto.product.GetProductListResponse;
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
	 * 현재는 인증 연동 전 단계로, 판매자 식별자를 임시로 {@code 1L}로 설정합니다.
	 * JWT 인증 연동 완료 후에는 인증된 사용자 정보에서 판매자 식별자를 추출하도록 변경해야 합니다.
	 *
	 * @param request 상품 등록에 필요한 요청 데이터
	 * @return 등록된 상품 정보 응답
	 */
	@PostMapping
	public ResponseEntity<CreateProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
		// TODO(#33): JWT 인증 연동 완료 후, 인증된 사용자 정보에서 sellerId 추출로 변경
		Long sellerId = 1L;

		CreateProductResponse response = productService.createProduct(request, sellerId);
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
}
