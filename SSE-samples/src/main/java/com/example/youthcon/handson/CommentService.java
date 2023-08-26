package com.example.youthcon.handson;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.example.youthcon.preparation.*;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class CommentService {
    HashMap<String, Set<SseEmitter>> container = new HashMap<>();

    public SseEmitter connect(String articleId) {
        // 1. 새로운 이미터를 생성
        SseEmitter sseEmitter = new SseEmitter(300_000L); // 타임아웃 설정 가능 - 5분
        // 타임아웃이 너무길면 서버에서 불필요한 커넥션을 관리해줘야하는 오버헤드
        // 타임아웃이 너무 짧으면 재연결시간의 트레이드오프

        // 만료된 SseEmiter가 container에 있기때문에 onCOmpletion코드를 이용해서 만료된것은 없애도록해야함
        // 실습에서는 진행하지 않음

        // 2. 전송할 이벤트를 작성
        SseEmitter.SseEventBuilder sseEventBuilder = SseEmitter.event()
                .name("connect")
                .data("connected!")
                .reconnectTime(3000L);//500L);//3000L

        // 3. 작성한 이벤트를 생성한 이미터에 전송
        sendEvent(sseEmitter, sseEventBuilder);

        // 4. 아티클과 연결된 이미터 컨테이너를 생성
        Set<SseEmitter> sseEmitters = container.getOrDefault(articleId, new HashSet<>());
        sseEmitters.add(sseEmitter);
        container.put(articleId, sseEmitters);
        return sseEmitter;
    }

    private void sendEvent(SseEmitter sseEmitter, SseEmitter.SseEventBuilder sseEventBuilder) {
        try {
            // send에서 커넥션이 끊기거나 하면 IOException이 발생하여 try-catch처리
            // 명시적으로 연결상태를 알수없는 단점으로 인한 Exception발생
            sseEmitter.send(sseEventBuilder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendComment(Comment comment, String articleId) {
        // 1. 아티클과 연결된 이미터들을 모두 가져오기
        Set<SseEmitter> sseEmitters = container.getOrDefault(articleId, new HashSet<>());

        // 2. 가져온 이미터들에게 댓글을 전송하기
        SseEmitter.SseEventBuilder sseEventBuilder = SseEmitter.event()
                .name("newComment")
                .data(comment)
                .reconnectTime(3000L);
        sseEmitters.stream().forEach(sseEmitter -> sendEvent(sseEmitter, sseEventBuilder));
    }

    // 어떤 점을 더 배워서 심화시킬수있을까
    // 자료 - 영문자료권장(한국어자료부족)
    // 동시성제어와 관련된부분을 주의깊게 보도록!

    // 웹소켓 vs SseEmitter -> 클라이언트와 서버간의 통신은 대부분 단방향이면 충분하고(웹소켓은 양방향)
    // Sse를 사용하면 모바일유저의 사용경험을 향상시킬수있음 (Sse가 에너지 절약 기능 제공 - 버퍼에저장하여 리소스절약)
    // ex) slack을 껏다 켰는데 알림이 어느순간 파바박 뜨는.....

    // 메모리에 저장되어 일관성이 깨지는 문제는 어떻게? -> 서버가 여러개인 경우 SSeEmiter는 전역으로 가지고있어야함
    // -> Reddis 를 이용하여 전역으로 가지도록 한다고함

    // 뉴스댓글기사같은것들은 인기많은 뉴스는 좋아요/싫어요 이런게 실시간으로 쌓이는데
    // 보고있는사람은 실시간으로 보지않아도됨
    // 댓글만 실시간으로 보고...나머지는 Rest로 받아서

    // Sse연결이 끊겼을때에는 어떻게?
    // 실시간통신에서 이벤트가 소실되는 문제는 다른기술을 이용해서 보완
    // Sse자체에서는 원복기능 제공하지않음
}
