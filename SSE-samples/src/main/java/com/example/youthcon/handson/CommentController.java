package com.example.youthcon.handson;

import com.example.youthcon.preparation.Comment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(final CommentService commentService) {
        this.commentService = commentService;
    }

    // 시나리오 1 알림요청 전송기능
    // 연결을 위한 스펙 GetMapping produces = text이벤트스트림을 지정
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect(@RequestParam("articleId") String articleId) {
        SseEmitter emitter = commentService.connect(articleId);
        return ResponseEntity.ok(emitter);
    }

    // 시나리오2 작성한기능이 원래 유저에게 새로고침없이 알람이 가도록
    @PostMapping("/comment")
    public ResponseEntity<Void> sendComment(@RequestBody Comment comment, @RequestParam("articleId") String articleId) {
        commentService.sendComment(comment, articleId);
        return ResponseEntity.ok().build();
    }

}
