package org.example.hooliguns.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import org.example.hooliguns.domain.CommentReport;
import org.example.hooliguns.dto.CommentReportResponse;
import org.example.hooliguns.repository.CommentReportRepository;
import org.example.hooliguns.repository.IncidentSocialRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CommentReportService {
    private final CommentReportRepository commentReportRepository;
    private final IncidentSocialRepository incidentSocialRepository;
    private final UserService userService;

    public CommentReportService(CommentReportRepository commentReportRepository,
                                IncidentSocialRepository incidentSocialRepository,
                                UserService userService) {
        this.commentReportRepository = commentReportRepository;
        this.incidentSocialRepository = incidentSocialRepository;
        this.userService = userService;
    }

    public Mono<CommentReportResponse> create(UUID incidentId,
                                              UUID commentId,
                                              String reporterId,
                                              String reason) {
        return incidentSocialRepository.findByIncidentId(incidentId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Incident comments not found")))
                .flatMap(social -> Mono.justOrEmpty(
                        social.getComments() == null
                                ? null
                                : social.getComments().stream()
                                .filter(comment -> commentId.equals(comment.getId()))
                                .findFirst()
                ))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Comment not found")))
                .flatMap(comment -> commentReportRepository.save(new CommentReport(
                        null,
                        incidentId,
                        commentId,
                        reporterId,
                        comment.getUserId(),
                        comment.getText(),
                        comment.getCreatedAt(),
                        reason,
                        Instant.now()
                )))
                .flatMap(this::toResponse);
    }

    public Flux<CommentReportResponse> list() {
        return commentReportRepository.findAll()
                .flatMap(this::toResponse)
                .sort(Comparator.comparing(CommentReportResponse::createdAt).reversed());
    }

    public Mono<Void> delete(String id) {
        return commentReportRepository.deleteById(id);
    }

    private Mono<CommentReportResponse> toResponse(CommentReport report) {
        return Mono.zip(
                        userService.findById(report.getReporterId()),
                        userService.findById(report.getCommentAuthorId())
                )
                .map(tuple -> new CommentReportResponse(
                        report.getId(),
                        report.getIncidentId(),
                        report.getCommentId(),
                        report.getReporterId(),
                        tuple.getT1().getDisplayName(),
                        report.getCommentAuthorId(),
                        tuple.getT2().getDisplayName(),
                        report.getCommentText(),
                        report.getCommentCreatedAt(),
                        report.getReason(),
                        report.getCreatedAt()
                ));
    }
}
