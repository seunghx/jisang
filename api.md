# Jisang API Documentation

## Introduction
Jisang REST API META Documentation - version 1.0 

각각의 엔드포인트 url에 대한 API 문서는 http://54.180.3.90/swagger-ui.html 에서 확인 가능하다.

### HTTP status codes
지상 어플리케이션에서 사용하는 HTTP 상태 코드 및 설명


Status code | Usage
-------- | --------
200 | 일반적으로 `GET` 메서드 요청이 성공하였을 경우 이 상태 코드가 client로 전달된다. 보통 서버의 resource가 client에 함께 전달된다.
201 | 일반적으로 `POST` 메서드 요청이 성공하였을 경우 이 상태 코드가 client로 전달된다. 보통 새로운 resource가 생성 되었음을 나타내며 현재까지는 생성된 resource와 관련된 `Location` 헤더는 사용하지 않는다.  
204 | 일반적으로 `PUT`, `PATCH` 메서드 요청이 성공하였을 경우 이 상태 코드가 client로 전달된다. 보통 기존 존재하는 resource의 정보가 변경되었음을 나타낸다.
400 | 잘못된 요청이 전달될 때 이 상태 코드가 client로 전달된다. 
404 | 요청에 해당하는 자원이 없음을 나타내나 요청 URL에 해당하는 handler가 없는 경우에도 이 상태 코드가 사용된다.  
405 | resource에 대한 요청 URL이 서버에 존재하나 요청의 메서드를 지원하지 않음을 나타내기위해 사용 된다.
409 | 이 메서드의 개념상의 의미는 '요청이 현재 서버의 상태와 충돌될 때'이나 지상 어플리케이션에서는 보통 유일해야 할 자원 등이 중복되었을 때에도 이 상태 코드를 사용한다. 예를 들어, 회원 가입 중 요청 이메일 주소가 중복 되었을 경우 이 응답이 사용될 수 있다. 
500 | 요청 처리 중 서버 내부 또는 외부에서 오류가 발생할 경우 이 메서드가 사용된다.



### /auth/ 이하 경로에서의 HTTP status codes
`/auth/` 이하의 경로의 경우 `JWT token` 인증 작업 및 요청 자원에 대한 인가 절차가 이루어진다.

Status code | Usage
-------- | --------
401 | 401 상태 메세지 `Unauthorized`와는 다르게, `인증(Authentication)` 실패시에 이 상태 코드가 전달된다.
403 | 요청의 권한이 없음을 나타내며 `401 Unauthorized`와 혼동하면 안된다. `인가(Authorization)`실패를 의미 한다.

/auth/ 이하의 경로 외에도 `403 Forbidden` 상태 코드가 반환되는 경우가 있다. 마켓 관리자 계정에서
타 마켓의 상품에 대한 수정 또는 삭제 요청이 있을 때이다.

<br>
> 상태 코드만으로는 정보가 부족한 경우가 있다. 
> 예를 들어, 로그인 실패 시 `401 Unauthorized` 응답은 여러 이유로 발생할 수 있는데, 
client에서 어플리케이션 사용자에게 상황에 따른 보다 detail한 메세지를 전달하거나 다른 동작을 수행해야 할 경우 응답 본문 상에 추가로 포함한 상태 코드와 메세지를 참고하는 것이 좋겠다.
<br>

### /auth/ 이하 요청 경로에 접근시 JWT token 전달.

`/auth/` 이하의 경로의 경우 `JWT token` 인증 작업 및 요청 자원에 대한 인가 절차가 이루어진다.
그러므로 `/auth/` 이하의 url로의 요청은 항상 ***요청 헤더***에 다음과 같은 JWT token 전용 필드를 담아야 한다.
	
    Authorization : "Bearer : eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

위와 같은 JWT token은 매번의 `/auth/` 이하의 요청 경로에 접근할 때마다(횟수는 변동될 수 있음.)새로 갱신되어 ***응답 헤더***의 `Authorization`필드에 저장되어 전달된다. 클라이언트는 이를 파싱할 필요 없이 필요할 때 같은 헤더명의 필드에 그대로 전달만 하면 된다. 물론 가장 최신에 전달 받은 JWT 토큰 문자열을 갖고 있다가 필요할 때 서버로 전송할 수 있어야 한다.
<br>

> login에 성공할 경우 서버로부터 최초로 JWT token을 발급 받는다. 

<br>

### 요청 성공시 응답 
`GET` 요청과 같이 특정 데이터 요청에 대한 응답을 제외한 성공 응답은 `201 CREATED`, `204 NO CONTENT` 등의 상태 코드만 전달된다. 

( 지상 어플리케이션 와이어프레임 상에는 존재하지 않지만 유저 정보 수정 요청의 경우에는 수정된 데이터가 응답 본문에 포함된다. )

### 400 Bad Request 관련 공통 응답 Response body 
요청 파라미터가 제한 사항을 위반하였을 경우 아래와 같은 응답이 전달된다.

	{
    	"status": 400,
    	"message": "요청 정보가 잘못되었습니다.",
    	"details": [
       		{
            	"target": "name",
            	"message": "이름을 입력해주세요."
        	},
        	{
            	"target": "password",
            	"message": "비밀번호를 입력해주세요."
        	}
   	]
	}
위는 `required : true` 인 필드 `name`, `password`를 입력하지 않고 요청을 보낼 때의 400 응답 본문의 예. 

요청 종류에 따라 `details` 필드가 전달되지 않을 수 있으나 `message`	 필드가 항상 전달되며 이런 경우는 대부분 클라이언트 코드가 정상적으로 동작하지 않거나 외부에서 의도한 악의적 요청일 경우에 해당된다. 


>요청 파라미터 별 제한 사항은 각각의 API에서 다룬다.


## Pre Defined Codes <a id="codebook" />


#### 유저 타입
    NORMAL_USER : "10"      - 일반 유저
    MANAGER_USER : "11"     - 상점 관리자
    ADMIN_USER : "12"       - 어플리케이션 관리자

#### 상품 목록 화면 타입
	BEST : "best"				   - 베스트 상품 목록
    RECOMMENDED : "recommended" 	- 추천 상품 목록
    PAGE : "page"				   - 페이지네이션 
    
#### 상품 카테고리
    ALL : "30"              - 모든상품 
    OUTER : "31"            - 아우터
    TOP : "32"              - 상의
    BOTTOM : "33"           - 하의
    SHOES : "34"            - 신발
    ONEPIECE : "35"         - 원피스
    SKIRT : "36"            - 치마
    BAG : "37"              - 가방
    ACC : "38"              - 악세사리

#### 지역 코드
	ENTIRE_LOCATION : "100"		- 지역 정보 X,(카테고리별 상품 보기 화면에서만 사용)
    GANGNAM : "101"			- 강남 지하상가
    EXPRESS_TERMINAL : "102"   - 고속 터미널
    BUPYEONG : "103" 		  - 부평
    

#### 상품 목록 화면 관련 주의 사항
주의 사항은 아래와 같으며 아래의 내용은 http://54.180.3.90/swagger-ui.html의 `ProductController`상의 해당 엔드포인트 문서를 함께 참고하기 바람.

##### 카테고리별 상품 목록 요청 
카테고리별 상품 목록은 지역별 상품 목록 요청 경로 `WEBAPP/mall/{mallLocation}/products` 에서 경로 변수 `mallLocation`에 `CodeBook.ENTIRE_LOCATION` 값을 지정하면 된다.

##### 전체 카테고리 상품 목록 요청
쇼핑 화면 상품 목록 정보 중 전체 카테고리 상품에 대한 요청을 하려면 요청 변수 `category`에 `CodeBook.ALL` 값을 지정하면 된다.

