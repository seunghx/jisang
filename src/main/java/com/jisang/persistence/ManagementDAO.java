package com.jisang.persistence;

import org.apache.ibatis.annotations.Param;

/**/
/**
 * 
 * 현재 지상 어플리케이션에서 사용하는 데이터베이스 스키마에는 일반 유저와 매니저를 따로 구분한 테이블이 정의되어 있지 않고 일반 유저와 매니저의 정보 모두 
 * 유저 관리용 테이블 tbl_users 내에 저장된다.
 * 
 * 어느 유저(매니저)가 어느 마켓을 담당하는 지에 대한 정보를 저장하기 위한 칼럼을 tbl_users에 추가하자니 
 * 대부분의 칼럼이 null값 등의 무의미한 값으로 채워질 것이기 때문에 (보통 일반유저일 것이므로) 이렇게 하지는 않았다.
 * 
 * 다른 방법으로 tbl_markets에 해당 마켓의 관리자에 대한 외래키 칼럼을 넣을까도 생각해 보았다. 그러나 추후에 하나의 마켓에 대하여 여러 명의 매니저가 생길 것을 
 * 고려하면 이 방법도 좋지 않았다. 결국 tbl_managements 테이블을 만들게 되었다. 이 테이블은 단순히 manager의 id와 market의 id를 칼럼(market_id, manager_id)을 
 * 둔 매핑용 테이블이다.
 * 
 * 이런 테이블은 객체 지향적인 모델링에는 어울리지 않다고 배웠고 그래서 tbl_managements에 대한 도메인 클래스(예를 들면 class Management)는 작성하지 않았다.
 * 
 */
public interface ManagementDAO extends MybatisMapper {
	public Integer readMarketId(int managerId);
	public Integer readMarketIdWithProductId(@Param("managerId") int managerId, @Param("productId")int productId);	
	
	public void create(@Param("managerId") int managerId, @Param("marketId") int marketId);
}
