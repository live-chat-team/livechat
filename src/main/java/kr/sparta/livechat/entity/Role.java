package kr.sparta.livechat.entity;

/**
 * 사용자 역할을 정의하는 Enum입니다.
 * 구매자, 판매자, 관리자 등 서비스에서 사용되는 주요 사용자 권한을 구분하기 위해 사용됩니다.
 * 각 역할은 인증 및 권한 부여 과정에서 활용됩니다.
 * Role.java
 *
 * @author kimsehyun
 * @since 2025. 12. 11.
 *
 */
public enum Role {

	BUYER,

	SELLER,

	ADMIN
}
