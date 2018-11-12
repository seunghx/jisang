package com.jisang.security.core.userdetails;

import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.jisang.security.core.authority.GrantedAuthorityMapper;
import com.jisang.security.domain.Account;

/**/
/**
 * 
 * 계정 정보 {@link Account}를 담은 UserDetails 구현 클래스이다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class DefaultUserDetails implements UserDetails {

    private static final long serialVersionUID = 8029129073998440460L;

    @Valid
    @NotNull
    private Account account;

    public DefaultUserDetails(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return GrantedAuthorityMapper.resolve(account.getRole()).mapToGrantedAuthority();
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return String.valueOf(account.getId());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
