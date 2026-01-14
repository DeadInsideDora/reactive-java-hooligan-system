package org.example.hooliguns.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.example.hooliguns.domain.IncidentComment;
import org.example.hooliguns.domain.IncidentReaction;
import org.example.hooliguns.domain.IncidentSocial;
import org.example.hooliguns.domain.ReactionType;
import org.example.hooliguns.dto.CommentResponse;
import org.example.hooliguns.repository.IncidentSocialRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class IncidentSocialService {
    private final IncidentSocialRepository incidentSocialRepository;
    private final UserService userService;

    public IncidentSocialService(IncidentSocialRepository incidentSocialRepository,
                                 UserService userService) {
        this.incidentSocialRepository = incidentSocialRepository;
        this.userService = userService;
    }

    public Mono<IncidentSocialSummary> summary(UUID incidentId) {
        return incidentSocialRepository.findByIncidentId(incidentId)
                .map(this::toSummary)
                .defaultIfEmpty(new IncidentSocialSummary(0, 0, 0));
    }

    public Mono<IncidentSocial> addReaction(UUID incidentId, String userId, ReactionType type) {
        return incidentSocialRepository.findByIncidentId(incidentId)
                .defaultIfEmpty(new IncidentSocial(null, incidentId, new ArrayList<>(), new ArrayList<>(), Instant.now()))
                .flatMap(social -> {
                    List<IncidentReaction> reactions = new ArrayList<>(
                            social.getReactions() == null ? List.of() : social.getReactions()
                    );
                    reactions.removeIf(reaction -> reaction.getUserId().equals(userId));
                    reactions.add(new IncidentReaction(userId, type, Instant.now()));
                    social.setReactions(reactions);
                    social.setUpdatedAt(Instant.now());
                    return incidentSocialRepository.save(social);
                });
    }

    public Mono<IncidentSocial> addComment(UUID incidentId, String userId, String text) {
        return incidentSocialRepository.findByIncidentId(incidentId)
                .defaultIfEmpty(new IncidentSocial(null, incidentId, new ArrayList<>(), new ArrayList<>(), Instant.now()))
                .flatMap(social -> {
                    List<IncidentComment> comments = new ArrayList<>(
                            social.getComments() == null ? List.of() : social.getComments()
                    );
                    comments.add(new IncidentComment(UUID.randomUUID(), userId, text, Instant.now()));
                    social.setComments(comments);
                    social.setUpdatedAt(Instant.now());
                    return incidentSocialRepository.save(social);
                });
    }

    public Flux<CommentResponse> getComments(UUID incidentId) {
        return incidentSocialRepository.findByIncidentId(incidentId)
                .flatMapMany(social -> Flux.fromIterable(
                        social.getComments() == null ? List.of() : social.getComments()
                ))
                .flatMap(comment -> userService.findById(comment.getUserId())
                        .map(user -> new CommentResponse(
                                comment.getId(),
                                comment.getUserId(),
                                user.getDisplayName(),
                                comment.getText(),
                                comment.getCreatedAt()
                        )));
    }

    public Mono<IncidentSocial> deleteComment(UUID incidentId, UUID commentId) {
        return incidentSocialRepository.findByIncidentId(incidentId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Incident comments not found")))
                .flatMap(social -> {
                    List<IncidentComment> comments = new ArrayList<>(
                            social.getComments() == null ? List.of() : social.getComments()
                    );
                    boolean removed = comments.removeIf(comment -> commentId.equals(comment.getId()));
                    if (!removed) {
                        return Mono.error(new IllegalArgumentException("Comment not found"));
                    }
                    social.setComments(comments);
                    social.setUpdatedAt(Instant.now());
                    return incidentSocialRepository.save(social);
                });
    }

    private IncidentSocialSummary toSummary(IncidentSocial social) {
        List<IncidentReaction> reactions = social.getReactions() == null ? List.of() : social.getReactions();
        List<IncidentComment> comments = social.getComments() == null ? List.of() : social.getComments();
        long likes = reactions.stream()
                .filter(reaction -> reaction.getType() == ReactionType.LIKE)
                .count();
        long dislikes = reactions.stream()
                .filter(reaction -> reaction.getType() == ReactionType.DISLIKE)
                .count();
        return new IncidentSocialSummary(likes, dislikes, comments.size());
    }
}
