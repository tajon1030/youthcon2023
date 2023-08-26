package hello.core.application;

import hello.core.domain.Member;
import hello.core.domain.MemberRepository;
import org.springframework.stereotype.Service;

@Service // 컴포넌트스캔범위를 벗어나게되어버림 -> application을 hello 하위로 위치 변경
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow();
    }

    public void deleteMemberById(Long memberId) {
        memberRepository.deleteById(memberId);
    }
}
