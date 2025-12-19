package kr.sparta.livechat.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.sparta.livechat.dto.product.CreateProductResponse;
import kr.sparta.livechat.dto.product.GetProductDetailResponse;
import kr.sparta.livechat.dto.product.GetProductListResponse;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.global.exception.GlobalExceptionHandler;
import kr.sparta.livechat.service.ProductService;

/**
 * ProductController 테스트 클래스입니다.
 * <p>
 * {@link ProductController}의 요청 매핑, 요청 바디({@code @RequestBody}), 응답 상태/바디 형식을 검증합니다.
 * 비즈니스 로직은 {@link ProductService}를 Mock 처리하여 컨트롤러 계층만 테스트합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 16.
 */
@WebMvcTest(controllers = ProductController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "server.port=0")
public class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ProductService productService;
	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	/**
	 * 상품 등록 성공 케이스를 검증합니다.
	 * <p>
	 * 요청 JSON이 정상적으로 바인딩되고, 서비스가 반환한 {@link CreateProductResponse}가
	 * 201(Created) 상태로 JSON 응답에 포함되는지 확인합니다.
	 * </p>
	 */
	@Test
	@DisplayName("상품 등록 성공 - 201 응답, CreateProductResponse JSON 반환 검증")
	void createProduct_Success() throws Exception {
		//given
		Map<String, Object> body = new HashMap<>();
		body.put("name", "토르의 망치");
		body.put("price", 3000000);
		body.put("description", "선택받은 자만 들 수 있는 망치");

		String requestJson = objectMapper.writeValueAsString(body);

		CreateProductResponse response = CreateProductResponse.builder()
			.productId(1L)
			.sellerId(1L)
			.name("토르의 망치")
			.price(3000000)
			.description("선택받은 자만 들 수 있는 망치")
			.build();

		given(productService.createProduct(any(), anyLong())).willReturn(response);

		//when&then
		mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("토르의 망치"))
			.andExpect(jsonPath("$.price").value(3000000));

	}

	/**
	 * 상품 등록 실패(중복 상품) 케이스를 검증합니다.
	 * <p>
	 * 서비스에서 중복 예외를 던지면 전역 예외 처리기가 409(Conflict)와 ErrorResponse(JSON)를 반환하는지 확인합니다.
	 * </p>
	 */
	@Test
	@DisplayName("상품 등록 실패 - 상품명 중복이면 409와 ErrorResponse를 반환한다")
	void createProduct_Fail_DuplicateName() throws Exception {
		// given
		Map<String, Object> body = new HashMap<>();
		body.put("name", "토르의 망치");
		body.put("price", 3000000);
		body.put("description", "선택받은 자만 들 수 있는 망치");

		String requestJson = objectMapper.writeValueAsString(body);

		given(productService.createProduct(any(), anyLong()))
			.willThrow(new CustomException(ErrorCode.PRODUCT_ALREADY_EXISTS));

		// when & then
		mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value(409))
			.andExpect(jsonPath("$.code").value(ErrorCode.PRODUCT_ALREADY_EXISTS.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_ALREADY_EXISTS.getMessage()))
			.andExpect(jsonPath("$.timestamp").exists());
	}

	/**
	 * 상품 목록 조회 성공 케이스를 검증합니다.
	 * 쿼리 파라미터를 전달하지 않으면 기본 값이 적용되며
	 * 서비스가 반환한 {@link GetProductListResponse} 가 200(OK) 상태로 응답에 포함되는지 검증합니다.
	 *
	 * @throws Exception MockMvc 수행 중 예외가 발생할 수 있음
	 */
	@Test
	@DisplayName("상품 목록 조회 성공 - 기본 파라미터로 200 OK 응답")
	void getProductList_Success_DefaultParam() throws Exception {
		// given
		GetProductListResponse response = new GetProductListResponse(
			0,
			20,
			0L,
			0,
			false,
			List.of()
		);

		given(productService.getProductList(0, 20)).willReturn(response);

		//when & then
		mockMvc.perform(get("/api/products")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.page").value(0))
			.andExpect(jsonPath("$.size").value(20))
			.andExpect(jsonPath("$.totalElements").value(0))
			.andExpect(jsonPath("$.totalPages").value(0))
			.andExpect(jsonPath("$.hasNext").value(false))
			.andExpect(jsonPath("$.productList").isArray());
	}

	/**
	 * 상품 목록 조회 실패(파라미터 타입 오류) 케이스를 검증
	 * <p>
	 * 파라미터가 양의 정수가 아닌 입력값이 전달되면, 400 에러와 ErrorResponse를 반환하는지 확인합니다.
	 * 해당 예외가 발생 시에는 서비스는 호출되지 않는 상태인지 점검합니다.
	 *
	 * @throws Exception MockMvc 수행 중 예외가 발생할 수 있음
	 */
	@Test
	@DisplayName("상품 목록 조회 실패 - page 타입 오류인 경우 검증")
	void getProductList_Fail_TypeMismatch() throws Exception {
		// when & then
		mockMvc.perform(get("/api/products")
				.param("page", "abc")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.code").value(ErrorCode.COMMON_BAD_PAGINATION.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorCode.COMMON_BAD_PAGINATION.getMessage()))
			.andExpect(jsonPath("$.timestamp").exists());

		verifyNoInteractions(productService);
	}

	/**
	 * 상품 상세 조회 성공 케이스를 검증합니다.
	 * PathVariable로 전달된 productId가 서비스로 전달되고,
	 * 서비스 반환 DTO가 200(OK)와 함께 JSON으로 응답되는지 확인합니다.
	 */
	@Test
	@DisplayName("상품 상세 조회 성공 - 200 OK 및 상품 상세정보 JSON 반환")
	void getProductDetail_Success() throws Exception {
		// given
		Long productId = 1L;
		GetProductDetailResponse response = mockDetailResponse(productId);
		given(productService.getProductDetail(productId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/products/{productId}", productId)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.productId").value(1))
			.andExpect(jsonPath("$.name").exists())
			.andExpect(jsonPath("$.price").exists());

		then(productService).should(times(1)).getProductDetail(productId);
	}

	private GetProductDetailResponse mockDetailResponse(Long productId) {
		GetProductDetailResponse res = mock(GetProductDetailResponse.class);
		given(res.getProductId()).willReturn(productId);
		given(res.getName()).willReturn("토르의 망치");
		given(res.getPrice()).willReturn(3000000);
		return res;
	}

	/**
	 * 상품 상세 조회 실패 케이스를 검증합니다.
	 * 입력값이 잘못 입력되었을 경우 400과 ErrorResponse를 반환한다.
	 */
	@Test
	@DisplayName("상품 상세 조회 실패 - 입력값 오류 시 검증")
	void getProductDetail_Fail_InvalidInput() throws Exception {
		//given
		Long invalidId = 0L;
		given(productService.getProductDetail(invalidId))
			.willThrow(new CustomException(ErrorCode.PRODUCT_INVALID_INPUT));

		//when & then
		mockMvc.perform(get("/api/products/{productId}", invalidId).accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.code").value(ErrorCode.PRODUCT_INVALID_INPUT.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_INVALID_INPUT.getMessage()))
			.andExpect(jsonPath("$.timestamp").exists());

		then(productService).should(times(1)).getProductDetail(invalidId);
	}

	/**
	 * 상품 상세 조회 실패 케이스를 검증합니다.
	 * 등록된 상품을 조회할 수 없는 경우 404와 ErrorResponse를 반환합니다.
	 */
	@Test
	@DisplayName("상품 상세 조회 실패 - 상품을 조회할 수 없을 경우 검증")
	void getProductDetail_Fail_ProductNotFound() throws Exception {
		//given
		Long productId = 999L;
		given(productService.getProductDetail(productId))
			.willThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

		//when & then
		mockMvc.perform(get("/api/products/{productId}", productId).accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.code").value(ErrorCode.PRODUCT_NOT_FOUND.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.getMessage()))
			.andExpect(jsonPath("$.timestamp").exists());

		then(productService).should(times(1)).getProductDetail(productId);
	}
}
