package org.example.hooliguns.controller;

import jakarta.validation.Valid;
import org.example.hooliguns.dto.CommentReportResponse;
import org.example.hooliguns.dto.ReportCommentRequest;
import org.example.hooliguns.security.AuthenticatedUser;
import org.example.hooliguns.service.CommentReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/reports")
public class CommentReportController {
    private final CommentReportService commentReportService;

    public CommentReportController(CommentReportService commentReportService) {
        this.commentReportService = commentReportService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT','ADMIN','IMMORTAL')")
    public Mono<CommentReportResponse> create(@Valid @RequestBody ReportCommentRequest request,
                                              @AuthenticationPrincipal AuthenticatedUser user) {
        return commentReportService.create(
                request.incidentId(),
                request.commentId(),
                user.getId(),
                request.reason()
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','IMMORTAL')")
    public Flux<CommentReportResponse> list() {
        return commentReportService.list();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IMMORTAL')")
    public Mono<Void> delete(@PathVariable String id) {
        return commentReportService.delete(id);
    }
}
