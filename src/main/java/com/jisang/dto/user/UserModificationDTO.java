package com.jisang.dto.user;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jisang.support.validation.Korea;

import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * 유저 정보 수정 과정에 클라이언트로부터 전달 받고 응답에 사용되는 DTO 오브젝트이다. 같은 패키지에 {@link AuthUserResponseDTO}도 있으나
 * 이 DTO 클래스가 정의되어 응답에 이용된 이유는 유저 정보 중 유저에게 보여지는 화면 중 수정 가능한 정보와 수정 불가능한 정보를 분리하기 위함이다.
 * 이렇게 분리함으로써 수정될 그리고 수정 된 정보만 클라이언트로부터 전달 받고 전달 할 수 있다.
 * 
 * 이 DTO 클래스에 유저 타입을 나타내는 user role 관련 프로퍼티가 없음을 알 수 있는데, 일반 유저 계정과 상점 관리자 유저 계정 사이에 변경은 없다고
 * 가정하였다. 상점 관리자 계정이 일반 유저 계정으로 변경되면 기존 마켓 정보는 어떻게 처리해야 할지도 애매하며 이런 경우까지 고려할 필요는 없다고 생각한다.
 * 
 * 또한 email의 경우 사용자에게는 id인 셈인만큼 변경이 불가능하다고 정하였으나 임시 비밀번호 발급 과정에 필요한 정보이기 때문에 일단 두었다. 
 * 
 * 
 * @author leeseunghyun
 *
 */
public class UserModificationDTO {
	
	/** 컨트롤러 메서드에서 set 해줄 프로퍼티로 클라이언트에서는 이 프로퍼티를 알 필요 없다.*/
	@ApiModelProperty(hidden = true)
	private int id;
	@ApiModelProperty(notes = "유저 이메일.", name = "email", required = true, example = "test@sopt.org")
	private String email;

	/**
	 * {@code password} 프로퍼티는 아래와 같이 {@link JsonIgnore} 애노테이션을 붙이고 또한 {@code password} 프로퍼에 대해서는 bean validation을
	 * 수행하지 않는다. 
	 * 
	 * 유저 정보 수정 화면에 처음 들어갈 때, 클라이언트는 서버로부터 유저의 기존 정보를 전달받게 되어 있다. 그러나 다른 프로퍼티와 달리 패스워드는 전달해서는 안되는
	 * 중요한 정보이다. 그렇기 때문에 다른 프로퍼티의 경우, 변경이 없어도 클라이언트에서 기존 정보를 서버에 전달할 수 있는 반면 비밀번호의 경우 사용자의 변경을 위한
	 * 입력이 없으면 null 값이 전달되어 버리게 된다. 그러므로 {@code password} 프로퍼티에는 bean validation을 수행하지 않고 대신 서비스 오브젝트나 DAO에서
	 * 이 필드가 null 값인지 아닌지 판별하여 유효성 검사(서비스)를 하거나 업데이트 SQL을 동적으로 변경(DAO)하거나 해야 한다.
	 * 
	 */
	@JsonIgnore
	@ApiModelProperty(notes = "유저의 비밀번호.", name = "password", required = false)
	private String password;
	

	@ApiModelProperty(notes = "유저 핸드폰 번호", name = "phone", required = true, example = "01012341234"
					, value = "하이픈(-)을 제외한 10-11자리의 숫자로만 구성 되어야함.")	@NotBlank(message = "핸드폰 번호를 입력해주세요.")
	@Pattern(regexp = "^\\+821([0|1|6|7|8|9])\\-([0-9]{3,4})\\-([0-9]{4})$", message = "하이픈('-')을 제외한 10~11자리의 올바른 핸드폰 번호를 입력하세요."
		   , groups= {Korea.class})
	private String phone;
	
	@ApiModelProperty(notes = "유저 이름", name = "name", required = true)
	@NotBlank(message = "이름을 입력해주세요.")
	private String name;
	
	public UserModificationDTO() {}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPhone() {
		return phone;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return super.toString() + "[id=" + id + ", email= " + email + ", phone=" + phone + ", name=" + name + "]";
	}
}
