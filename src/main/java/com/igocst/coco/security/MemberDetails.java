package com.igocst.coco.security;

import com.igocst.coco.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class MemberDetails implements UserDetails {

    private final Member member;

    public MemberDetails(Member member) {
        this.member = member;
    }

    public Member getMember() {
        return member;
    }

    // 인가 부분
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    // 스프링 시큐리티 username = coco 프로젝트 email
    @Override
    public String getUsername() {
        return member.getEmail();
    }

    // 로그인 된 사용자를 페이지에서 표시할 땐, 이메일이 아닌 닉네임을 보여줄 것
    public String getNickname() {
        return member.getNickname();
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
