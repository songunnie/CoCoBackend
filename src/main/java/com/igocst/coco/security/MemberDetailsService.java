package com.igocst.coco.security;

import com.igocst.coco.domain.Member;
import com.igocst.coco.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // 스프링 시큐리티가 보내준 email을 받아서 DB에 회원 정보가 있는지 확인
    @Override
    public MemberDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("일치하는 회원 정보가 없습니다." + email)
        );

        // DB에 일치하는 회원이 있으면 MemberDetails로 보냄
        return new MemberDetails(member);
    }
}
