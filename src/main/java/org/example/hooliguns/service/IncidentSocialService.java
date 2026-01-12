package org.example.hooliguns.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.example.hooliguns.domain.IncidentComment;
import org.example.hooliguns.domain.IncidentReaction;
import org.example.hooliguns.domain.IncidentSocial;
import org.example.hooliguns.domain.ReactionType;
import org.example.hooliguns.repository.IncidentSocialRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class IncidentSocialService {
    private final IncidentSocialRepository incidentSocialRepository;

    public IncidentSocialService(IncidentSocialRepository incidentSocialRepository) {
        this.incidentSocialRepository = incidentSocialRepository;
    }

    public Mono<IncidentSocialSummary> summary(UUID incidentId) {
        return incidentSocialRepository.findByIncidentId(incidentId)
                .map(this::toSummary)
                .defaultIfEmpty(new IncidentSocialSummary(0, 0, 0));
    }

    public Mono<IncidentSocial> addReaction(UUID incidentId, UUID userId, ReactionType type) {
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

    public Mono<IncidentSocial> addComment(UUID incidentId, UUID userId, String text) {
        return incidentSocialRepository.findByIncidentId(incidentId)
                .defaultIfEmpty(new IncidentSocial(null, incidentId, new ArrayList<>(), new ArrayList<>(), Instant.now()))
                .flatMap(social -> {
                    List<IncidentComment> comments = new ArrayList<>(
                            social.getComments() == null ? List.of() : social.getComments()
                    );
                    comments.add(new IncidentComment(userId, text, Instant.now()));
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
