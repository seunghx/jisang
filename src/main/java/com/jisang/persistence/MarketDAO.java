package com.jisang.persistence;

import com.jisang.domain.Market;

/**/
/**
 * 
 * Market(지하 상가 상점) 도메인에 대한 DAO 인터페이스.
 * 
 * @author leeseunghyun
 *
 */
public interface MarketDAO extends MybatisMapper {

    public void create(Market market);

    public Market read(int marketId);

    public void update(Market market);

}
