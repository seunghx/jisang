package com.jisang.dto.user;

import com.jisang.domain.User;

/**
 * 
 * 유저 정보에 대한 응답용 DTO 클래스이다. 현재까지는 도메인 오브젝트 {@link User} 클래스와 {@code password}
 * 프로퍼티가 있냐 없냐의 차이만 있으나 위와 같은 차이는 시간이 지나 새로운 정보가 추가될 경우 더 커질 수 있다. 이런 차이 때문에 도메인
 * 클래스와 분리하여 DTO 클래스를 정의하였다.
 * 
 * 
 * 클래스명을 {@link UserResponseDTO}로 정의하지 않고 {@link AuthUserResponseDTO}라고 정한 이유는
 * 다음과 같다. 어플리케이션에서 회원 정보를 보는 화면은 두 가지 종류가 있을 수 있다(현재 지상 어플리케이션의 워크플로우 상에는 존재하지는
 * 않는다.) 하나는 자신의 회원 정보를 보는 것이며 다른 하나는 다른 회원의 정보를 보는 것이다. 전자의 경우가 후자의 경우보다 보다 더
 * credential한 정보를 담고 있을 가능성이 크다. (물론 비밀번호 등은 전자, 후자 어느 쪽에도 보여주지 않는 것이 좋겠다.) 위와
 * 같은 이유로 이 두 오브젝트를 분리하게 되었다. 클라이언트 앱에서 데이터를 선별적으로 화면에 보여줄 수야 있겠으나 브라우저나 다른 방법으로
 * 요청을 할 경우 감추어져야 할 정보가 외부에 전달될 수 있기 때문이다. (물론 현재 구현에는 그런 정보는 딱히 없는 것 같다.)
 * 
 * @author leeseunghyun
 *
 */
public class AuthUserResponseDTO {

    private String email;
    private String role;
    private String name;
    private String phone;

    public AuthUserResponseDTO() {
    }

    public AuthUserResponseDTO(User user) {
        this.email = user.getEmail();
        this.role = user.getRole();
        this.name = user.getName();
        this.phone = user.getPhone();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[email=" + email + ", role=" + role + ", name=" + name + ", phone=" + phone
                + "]";
    }
}
