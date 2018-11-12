package com.jisang.config.code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;



/**
 * 
 * 
 * 클라이언트와 서버 양단에서 사용될 사전에 정의된 카테고리성 코드를 담은 enum 클래스들을 포함하고 있는 클래스이다.
 * 
 * 
 * 클라이언트 프로그램이 실행되어 코드북(코드를 모아두었단 의미로 코드북이라고 표현하였음.)에 대한 웹 요청이 전달될 경우 이 클래스의 오브젝트가 
 * 반환되어 json 형태로 클라이언트에 전달된다.
 * 
 * 애초에 코드북을 json 파일로 만들어 코드가 변경되어도(새로운 종류의 코드가 추가될 경우 해당 종류의 코드에 대한 이 클래스의 {@link List} 타입 필드가 추가되어야 함.) 
 * 다시 배포되어야 하는 수고를 막을 수도 있지만 변경될 일이 적다는 점을 고려하여, enum 이용 방식이 보다 편하면서도 다른 컴포넌트로부터 종종 이용 또는 참조되면
 * 편리할 수 있겠다( 예 : {@link UserType} )는 이유로 내부의 enum을 두는 방식으로 이 클래스를 작성하였다.
 * 
 * 이 클래스의 내부를 보면 여러 개의 {@code enum} 클래스가 정의되어 있고 이 클래스의 생성자에서 해당 enum들의 오브젝트를 이 클래스의 {@link List} 타입 필드에 
 * {@code add}함을 알 수 있다. 후에 클라이언트로부터 코드북에대한 웹 요청이 있을 경우 컨트롤러는 이 클래스의 오브젝트를 담아 클라이언트에 전달할 것이며 클라이언트는
 * json 형태로 응답을 받게 될 것이다. 
 * 
 * 
 * 이 클래스의 필드를 보면 토픽 별로 {@link List} 타입 필드가 있음을 알 수 있다. 각 토픽 별로 enum 오브젝트를 분리하여 json으로 전달될 때 
 * 
 * 
 * <pre>"USER_TYPE": [
 *       {
 *           "name": "NORMAL_USER",
 *           "code": "1"
 *       },
 *       {
 *           "name": "MANAGER_USER",
 *           "code": "2"
 *       },
 *       {
 *           "name": "ADMIN_USER",
 *           "code": "3"
 *       }
 *   ],</pre>
 *   
 * 와 같은 형태로 전달되게 하려는 목적 때문인데, 이런 형태로 json이 전달될 경우 코드북을 요청한 클라이언트에서는 혹시나 후에 코드가 추가되더라도 동일하게 동작할 수 
 * 있다. 간단히 "USER_TYPE" 배열을 순회하기만 하면 되기 때문이다. 초기의 이 클래스는 현재 구현과 같이 내부 enum 클래스를 두고 내부 enum의 오브젝트들을 이 
 * 클래스 {@link CodeBook}의 필드로 담고 있는 방법으로 구현되지 않고 아래와 같이 단순 코드의 나열 형식으로 구현되어 있었다.
 * 
 * <pre>	
 *	
 *	NORMAL_USER("1"), MANAGER_USER("2"), ADMIN_USER("3"),
 *
 *
 *	BEST("best"), RECOMMENDED("recommended"), PAGE("page"),
 *	
 * </pre>
 * 
 * 현재 구현과 같이 연관된 코드끼리 그룹핑을 시키지 않을 경우(초기의 구현), 클라이언트는 코드 전체의 존재를 알아야만 한다. 코드 전체의 존재를 모른다면
 * 예를 들어, BLOCKED_USER("4") 와 같은 새로운 코드가 추가되어도 클라이언트 어플리케이션은 이를 빼먹게 되어 제대로 동작할 수 없기 때문이다. 그러므로
 * 코드 전체의 존재를 알기위해 코드("13"과 같은 코드)가 추가될 때마다 클라이언트 코드(프로그램의 코드를 의미)가 수정되어야 할 수도 있다. 반대로 현재 구현은
 * "USER_TYPE" 배열만 순회하면 코드("13"과 같은 코드)가 추가되어도 클라이언트 코드(프로그램 코드를 의미)의 수정 없이 잘못 동작될 일이 없다.
 * 물론 API 문서를 통해 코드에 대해 다룰 것이기 때문에 이 문제는 크지 않다고 볼 수 있겠다.
 * 
 * 이 클래스를 spring bean({@link @Component})로 둔 이유는 이 클래스의 오브젝트가 매번 생성된다면 이 클래스의 생성자에서의 {@code values()} 호출이 매번
 * 일어날 것이기 때문이며, 코드 정보는 서버 어플리케이션이 실행되고 나서 런타임에 변경될 일은 없을 것이기 때문이기도 하다. 컨트롤러로부터 클라이언트로 코드북에 대한 
 * 응답이 있을 때, 이 클래스가 DTO 오브젝트 같이 사용된다. 
 *
 *
 *
 * 10.05) 그러나 이와 같은 방법은 데이터베이스의 레코드를 살펴볼 때 "1", "2"와 같이 저장된 정보가 무엇을 의미하는지를 알 수 없다는 단점이 있다. 
 * 매번 데이터베이스 레코드를 볼 때마다 이 클래스 {@link CodeBook}을 보며 "1" 값이 무엇을 의미하는지를 확인해야 한다. 이런 이유로 앞으로는 이렇게는 하지 않기로
 * 하였다.
 * 
 * 
 * @author leeseunghyun
 * 
 *
 */
@Component
public class CodeBook {
	
	@JsonFormat(shape = JsonFormat.Shape.OBJECT)
	private final List<UserType> userType = new ArrayList<>();
	@JsonFormat(shape = JsonFormat.Shape.OBJECT)
	private final List<ProductListViewType> productListViewType = new ArrayList<>();
	@JsonFormat(shape = JsonFormat.Shape.OBJECT)
	private final List<MallLocation> mallLocation = new ArrayList<>();
	@JsonFormat(shape = JsonFormat.Shape.OBJECT)
	private final List<ProductCategory> productCategory = new ArrayList<>();
	
	@JsonIgnore
	private static final Map<String, MallLocation> codeToMallLocation = new HashMap<String, MallLocation>();

	
	public CodeBook(){
				
		for(UserType e : UserType.values()) {
			userType.add(e);
		}
		
		for(ProductListViewType e : ProductListViewType.values()) {
			productListViewType.add(e);
		}
		
		for(MallLocation e : MallLocation.values()) {
			mallLocation.add(e);
			codeToMallLocation.put(e.getCode(), e);
		}
		
		for(ProductCategory e : ProductCategory.values()) {
			productCategory.add(e);
		}
	}		


	/**
	 * 
	 * 유저 타입 정보에 대한 enum 클래스로 {@code code} 필드는 해당 정보에 대한 축약된 형태의 코드를 의미한다.
	 * 
	 * 이 클래스의 내부를 보면 단순 코드를 담은 것 외에도 수행하는 일이 하나 더 있는데 바로 전달받은 인자 {@code role}에 해당하는 
	 * {@link Collection<GrantedAuthority>}를 반환하는 것이다. 유저 롤 정보에 대한 반환은 유저의 타입을 나타내는 이 클래스에서 
	 * 처리하기에 알맞다고 판단하여 enum 전략패턴을 적용하여 이를 구현하였다.
	 * 
	 * 
	 * @author leeseunghyun
	 * 
	 */
	@JsonFormat(shape = JsonFormat.Shape.OBJECT)
	//@JsonPropertyOrder({"name", "code"})
	public enum UserType implements AppCode {

		/** {@code MANAGER_USER}는 쇼핑몰 관리자를 의미하며 {@code ADMIN_USER}는 어플리케이션 관리자 계정을 의미한다. */

		NORMAL_USER("1"), MANAGER_USER("2"), ADMIN_USER("3");

		private String code;

		private UserType(String code) {
			this.code = code;
		}

		// Override from AppCode

		@Override
		public String getName() {
			return name();
		}

		@Override
		public String getCode(){
			return code;
		}	
		
		@Override
		public boolean equalsByCode(String code) {
			return this.code.equals(code);
		}
	}
	
	/**
	 * 
	 * 상품 목록 화면 타입 정보에 대한 enum 클래스로 {@code code} 필드는 해당 정보에 대한 축약된 형태의 코드를 의미한다.
	 * {@link CodeBook} 의 다른 내부 이늄들과 이 이늄 클래스가 다른 점은 다른 이 이늄 클래스의 오브젝트들은 {@link code} 필드로 축약된 형태의
	 * 코드를 두지 않는다는 것이다. 이 enum 클래스의 {@code code} 값은 데이터 저장소에 저장될 필요가 없는 정보이며 또한 "20", "21", "22"보다는 
	 * "best", "recommended", "page"가 사용자에게 더 가독성이 좋기 때문이다. 클라이언트에게 API 문서가 제공되는 만큼 이 enum 클래스가 꼭 필요한 것은
	 * 아니라고도 할 수 있으나 프로그램의 다른 코드 등에서 상품 목록 화면 타입을 구분하기 위한 문자열 비교 등이 수행될 때 "best".equals() 와 같은 
	 * 문자열 리터럴 비교를 피하고자 정의하였으며 클라이언트에게도 동일한 값이 전달되기 때문에 API 변화에 보다 덜 민감하다.
	 * 
	 * ('"/url?viewType" + "best"' 보다는 '"/url?viewType" + codeBook.BEST' 와 같은 방식이 더 변경으로부터 안전하다. )
	 * 
	 * 
	 * @author leeseunghyun
	 *
	 */
	@JsonFormat(shape = JsonFormat.Shape.OBJECT)
	@JsonPropertyOrder({"name", "code"})
	public enum ProductListViewType implements AppCode {
		
		BEST("best"), RECOMMENDED("recommended"), PAGE("page");

		private String code;
		
		private ProductListViewType(String code) {
			this.code = code;
		}
		
		@Override
		public String getName() {
			return name();
		}

		@Override
		public String getCode() {
			return code;
		}

		@Override
		public boolean equalsByCode(String code) {
			return this.code.equals(code);
		}
		
	}
	
	

	/**
	 * 
	 * 지하상가 쇼핑몰의 상품 카테고리를 나타내는 enum 클래스로 이 클래스의 {@code code} 필드는 해당 정보에 대한 축약된 형태의 코드를 의미한다.
	 * 데이터 저장소에 "ONEPIECE"를 저장하는 것 보다는 "30"을 저장하는 것이 더 경제적이라 생각되어 축약된 형태로 코드를 정의하게 되었다.
	 *
	 *  
	 * @author leeseunghyun
	 * 
	 */
	@JsonFormat(shape = JsonFormat.Shape.OBJECT)
	@JsonPropertyOrder({"name", "code"})
	public enum ProductCategory implements AppCode{
		
		ALL("30"), OUTER("31"), TOP("32"), BOTTOM("33"), SHOES("34"), ONEPIECE("35"), SKIRT("36"), BAG("37"), ACC("38");
			
		private String code;

		private ProductCategory(String code) {
			this.code = code;
		}
			
		@Override
		public String getName() {
			return name();
		}
			
		@Override
		public String getCode(){
			return code;
		}
		
		@Override
		public boolean equalsByCode(String code) {
			return this.code.equals(code);
		}
	}
	
	/**
	 * 
	 *
	 * 지하 상가 쇼핑몰의 위치 정보를 나타내는 enum 클래스로 {@code code} 필드는 데이터베이스 상의 해당 지하상가의 ID 값을 의미한다.
	 * (실제 서비스를 진행하는 것은 아니여서 지역은 강남, 고속터미널, 부평의 세 가지만 정하였다.)
	 * 
	 * 
     * @author leeseunghyun
     *
     */
	@JsonFormat(shape = JsonFormat.Shape.OBJECT)
	@JsonPropertyOrder({"name", "code"})
	public enum MallLocation implements AppCode {

	
		ENTIRE_LOCATION("10"), GANGNAM("11"), EXPRESS_TERMINAL("12"), BUPYEONG("13");
	
		private String code;
	
		private MallLocation(String code) {
			this.code = code;
		}
	
		@Override
		public String getName() {
			return name();
		}

		@Override
		public String getCode(){
			return code;
		}
		
		@Override
		public boolean equalsByCode(String code) {
			return this.code.equals(code);
		}
		
		public static MallLocation fromString(String code) {
			return codeToMallLocation.get(code);
		}

	}
}
