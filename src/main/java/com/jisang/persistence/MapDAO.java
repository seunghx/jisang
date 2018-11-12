package com.jisang.persistence;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jisang.domain.Address;
import com.jisang.domain.Location;

/**
 * 
 * 지상 어플리케이션의 지도 관련 도메인({@link Address}, {@link Location})에 대한 DAO 인터페이스.
 * 
 * 
 * @author leeseunghyun
 *
 */
public interface MapDAO extends MybatisMapper {

    /**
     * 
     * 스스로 정하기를 데이터 엑세스 메서드의 입(파라미터)/출(반환)력 타입은 도메인 오브젝트만 이용하려고 하였다. 입/출력에 도메인 오브젝트를
     * 이용할 경우 그렇지 않은 경우에 비해 DAO메서드의 재사용성이 증가하기 때문이다. 그러나 아래 메서드의 경우에는 도메인 클래스
     * {@link Address}를 전달하는 것은 과하다고 생각한다. 지하 상가내 해당 점포 내 마켓 정보만 변경하는 아래 메서드에 위도/경도
     * 정보 등 (지하상가에 공사가 있어 철거되지 않는 한) 절대 바뀔리 없는 정보까지 전달 및 갱신할 필요가 없기 때문이다.
     * ({@link Address} 클래스에서 바뀌는 정보는 마켓의 id밖에 없다.)
     * 
     */
    public void updateAddressMarketId(@Param("address") String address, @Param("location") String location,
            @Param("marketId") int marketId);

    /**
     * 
     * (이사 등의 이유로)마켓의 점포 위치가 변경되었다고 할 때, 변경된 점포에 해당하는 tbl_addresses의 row에
     * address_market_id를 설정해 주어야한다. 그러나 그 전에 이전 점포에 해당하는 row의 address_market_id의 값을
     * null 등의 값으로 변경해주어야 한다. 그렇지 않으면 빈 점포가 타 점포에서 장사중인 마켓을 가리키는 일이 발생할 수 있기
     * 때문이다.(물론 address_market_id 칼럼은 unique key로 설정되어 있어 중복된 마켓 id가 들어갈 수는 없다.)
     * address_market_id 칼럼을 변경하려면 위의 메서드 {@link #updateMarketId}를 사용해도 되나 이 주석 상단에
     * 말한대로 위의 메서드는 마켓의 점포 위치가 변경됨으로 인한 수정에 목적이 있어 {@link #updateMarketId} 메서드를
     * 호출하려면 먼저 {@link readByMarketId}와 같은 메서드를 호출하여 {@link Address} 정보를 db로부터 받아와야만
     * 한다. {@link updateMarketId} 메서드 호출의 번거로움을 피하기 위하여 아래 메서드
     * {@link updateMarketIdByOlderMarketId}를 정의하였다.
     * 
     * 사전에 정의한 것과 다르게 메서드의 파라미터는 도메인 타입 오브젝트가 아니라 마켓의 id를 나타내는 정수 값(및 정수 타입 박싱
     * 클래스)인데 {@link #updateMarketId} 메서드의 주석으로 설명하였듯 마켓 id외에 다른 정보는 모두 변경될 일 없이 고정
     * 된 {@link Address} 도메인의 특성을 고려하여 이렇게 하기로 하였다.
     * 
     * 
     */
    public void updateAddressMarketIdByOlderMarketId(@Param("olderMarketId") int olderMarketId,
            @Param("newMarketId") Integer newMarketId);

    /**
     * 지하 상가 내 위치 정보, 예를 들어 'A-1'과 같은 주소 체계는 다른 지하상가의 주소 체계와 중복될 가능성이 있다. 그러므로
     * {@link Address}를 유일하게 식별하기 위해 {@code address} 뿐만 아니라 {@code location} 정보도 함께
     * 전달되어야 한다.
     */
    public Address readAddressByAddressIdAndLocation(@Param("address") String address,
            @Param("location") String location);

    public Address readAddressByMarketId(int marketId);

    public Location readLocation(String location);

    public List<Address> readAll(String location);
}
