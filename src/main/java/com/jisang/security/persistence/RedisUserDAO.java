package com.jisang.security.persistence;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Repository;

import com.jisang.security.domain.TokenComponent;
import com.jisang.security.support.RedisKeyUtils;

import static com.jisang.security.support.RedisKeyUtils.HashKeyUtils;

/**
 * 
 * JWT token의 경우 토큰 재생 공격 방지를 위해 JTI 필드에 대한 비교가 필요하다.
 * 
 * 오버헤드와 지연 시간을 고려하면 매 로그인 유지를 위한 JWT token 인증시마다 RDB에 다녀오는 것은 무리가 크다고 생각하여 JWT
 * token에 refresh-ttl을 지정하여 토큰 갱신 기간(예를 들면 30분)이 지날 때마다 RDB로부터 JTI를 비교하도록 하였었다.
 * 그러나 이 마저도 만족스럽지는 않았다. refresh-ttl 마다 RDB를 조회해야 했고 JTI 비교에 통과한 경우 새로 갱신까지 해주어야
 * 했기 때문이다. state less한 어플리케이션을 포기해야하나 아니면 JTI 필드를 없애 보안(재생 공격 방어)을 포기해야의 갈림길에서
 * state less를 포기하기로 하여 redis 적용을 결심하게 되었다.
 * 
 * JTI는 메모리에 두되 유저 아이디와 role 정보 등의 정보는 JWT token에 담음으로써 state-less는 아니어도 어느 정도
 * 메모리 절약이 가능하다. 정말 꼭 필요한 정보만 메모리에 담았기 때문이다.(유저 아이디와 role 정보를 RDB 등에 담아도 되나 이럴
 * 경우 인증을 거치는 모든 엔드포인트 요청에 대한 처리마다 RDB에 접근하게 된다.) 물론 redis가 아닌 웹 컨테이너가 제공하는 세션에
 * JTI를 담아도 되겠으나 (실제 서비스를 하지는 않지만 서비스를 한다는 가정하에) 나중에 어플리케이션이 성장하여 서버 인스턴스가 여러 개가
 * 될 경우 redis를 세션 저장소로 이용하는 경우가 더 나을 것이라고 생각하였다. (이 때 당시에는 SpringSession 프로젝트의
 * 존재를 몰랐다.)
 * 
 * 
 * 웹 컨테이너 세션을 사용할 경우 서비스 이용자의 증/감에 따른 클러스터 내의 서버의 증/감이 일어날 때마다 서버 내부의 세션 정보 처리에
 * 대한 리밸런싱 작업이 서비스를 수행하는 어플리케이션의 성능에 영향을 미칠 수 있으며 또한 웹 컨테이너의 세션도 서버의 JVM 상의 힙 위에
 * 존재하기 때문에 데이터 양이 많아질 수록 가비지 컬렉션 등의 동작에 따라 서버 동작이 느려질 수 있다고 한다 또한 웹 컨테이너(의 세션
 * 매니저)가 (세션 정보를 가져오기 위해)클라우드 내의 다른 서버의 존재를 알기 위해 기존 서비스 중인 서버에 다른 서버를 가리키는
 * ip/port 등을 설정해줘야 하므로 불편해서라고도 설명한다.
 * 
 * 
 * 
 * 또한 작지만 RDB의 부하를 줄일 수 있다는 장점도 있다. 웹 서버의 세션을 사용할 경우(redis 사용 x) 세션이 종료되면 저장이
 * 필요한 데이터가 있다고 할 때 저장을 위해 RDB에 접근해야 할 것이다. 상점의 상품 목록을 보는 화면이야 당연하겠지만 각 지하상가
 * 지역(예: 강남, 고속 터미널) 내의 상품 목록을 보는 화면에도 상점 정보와의 join이 필요하다. 어느 마켓의 상품인지 사용자와
 * 클라이언트 어플리케이션(프론트엔드) 둘 모두 알아야 하기 때문이다. (이 시나리오에서 사용자의 화면에 마켓의 이름이 제공된다.) 또한
 * jisang 어플리케이션은 지하상가 쇼핑몰 어플리케이션으로 방금 말한 화면이 메인이 되는 어플리케이션인 만큼 join이 자주 일어나는
 * RDB의 부하를 줄여주는 것은 메모리 사용으로 인한 비용의 증가만큼 가치는 있을 수 있다. redis를 세션으로 사용하면 redis 그
 * 자체가 지속성을 갖는 데이터베이스이므로 (메모리 위에 올려지는 만큼 돌발 상황에 대한 대처는 필요하다. - 레디스에서 추천하는 대로 aof
 * 방식과 rdb 방식을 둘 다 사용하였다. (그러나 replica 등은 사용하지 않았다.)) 세션이 끝날 때 필요한 정보를(저장이 필요한
 * 정보가 있다고할 때 해당 정보를) RDB에 따로 저장할 필요가 없다. 유사 기술 memcached의 경우 지속성을 갖는 데이터베이스는
 * 아니며 단지 메모리 위에서 동작하는 일종의 캐쉬이기 때문에 위와 같은 장점을 만족하지 못한다. 물론 redis의 String과 같은 단순
 * key-value 타입 외에 제공되는 데이터 타입이 없다는 점으로부터 이미 memchached는 고려하지 않았다.
 * 
 * 
 * 이 클래스의 메서드에서 redis 연산 중 예외가 발생할 경우 해당 예외는 spring에 의해
 * {@link DataAccessException}으로 변환될 것이다. 그러나 security 단에서 발생하는 데이터 접근 관련
 * 예외({@link DataAccessException})는 {@link AuthenticationServiceException}으로 변환
 * 되어야만 한다. (꼭 위의 예외로 변환되어야만 한다기보다는 {@link AuthenticationException}의 하위 타입 중 위
 * 예외가 가장 {@link DataAccessException}을 변환시키기에 알맞다.) 위와 같은 동작은 이 클래스 뿐만 아니라
 * security 영역 내의 모든 DAO 클래스에 적용되어야 한다. 그래서 위와 같은 예외 변환 동작은 AOP를 이용해 공통적으로 처리되도록
 * 하였다.
 * 
 * 
 * @author leeseunghyun
 * 
 */
@Repository
public class RedisUserDAO {

    private final Logger logger = LoggerFactory.getLogger(RedisUserDAO.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private Jackson2HashMapper hashMapper;

    private static final String jtiField = HashKeyUtils.jtiField();

    /**
     * 
     * 인증에 사용 될 user 정보에 대한 갱신을 수행한다. 위에서 설명하였듯 redis 연산 중 발생한 예외는
     * {@link DataAccessException}으로 변환되며 이 예외는 다시
     * {@link com.jisang.security.aop.AspectPerAction} 클래스의 메서드로에 의해
     * {@link AuthenticationServiceException}으로 변환될 것이다.
     * 
     * 
     */
    public void update(TokenComponent tokenComponent) {

        logger.info("Starting to update user token information.");

        Objects.requireNonNull(tokenComponent,
                "Null value argument tokenComponent detected while trying to update user token info in " + this);

        stringRedisTemplate.opsForHash().putAll(RedisKeyUtils.userKey(tokenComponent.getId()),
                hashMapper.toHash(tokenComponent));

        logger.info("Updating user token information succeeded.");

    }

    /**
     * 
     * 인증에 사용 될 user의 토큰 정보(JTI)를 반환한다. 현재 구현은 JTI 반환 밖에 없으나 후에 인증에 사용될 또 다른 정보가 추가될
     * 수도 있는 것이기 때문에 문자열이 아닌 오브젝트를 반환하도록 정의하였다. 위에서 설명하였듯 redis 연산 중 발생한 예외는
     * {@link DataAccessException}으로 변환되며 이 예외는 다시
     * {@link com.jisang.security.aop.AspectPerAction} 클래스의 메서드로에 의해
     * {@link AuthenticationServiceException}으로 변환될 것이다.
     * 
     * 
     * @param uid
     * @return 전달 된 key에 해당하는 데이터가 없을 경우 null을 반환한다.
     * 
     */
    public TokenComponent find(int uid) {

        logger.info("Starting to find user token information.");

        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(RedisKeyUtils.userKey(uid));

        if (userMap.isEmpty()) {
            logger.warn("Token info associated with {} doesn't exist in Redis.", uid);

            throw new InsufficientAuthenticationException("token info doesn't exist in Redis.");
        }

        TokenComponent dto = new TokenComponent();
        dto.setJti((String) userMap.get(jtiField));

        logger.info("Finding user token information succeeded.");

        return dto;
    }

    /**
     * 파라미터 {@code uid}에 해당하는 유저의 토큰 정보를 삭제한다. 위에서 설명하였듯 redis 연산 중 발생한 예외는
     * {@link DataAccessException}으로 변환되며 이 예외는 다시
     * {@link com.jisang.security.aop.AspectPerAction} 클래스의 메서드로에 의해
     * {@link AuthenticationServiceException}으로 변환될 것이다.
     */
    public void delete(int uid) {
        logger.info("Starting to delete user token information.");

        stringRedisTemplate.delete(RedisKeyUtils.userKey(uid));
    }
}
