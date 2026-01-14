package org.example.hooliguns.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.example.hooliguns.domain.Incident;
import org.example.hooliguns.domain.ModerationStatus;
import org.example.hooliguns.domain.UserAccount;
import org.example.hooliguns.dto.CreateIncidentRequest;
import org.example.hooliguns.dto.IncidentResponse;
import org.example.hooliguns.repository.IncidentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
public class IncidentService {
    private final IncidentRepository incidentRepository;
    private final IncidentSocialService incidentSocialService;
    private final UserService userService;
    private final Sinks.Many<IncidentResponse> sink;

    public IncidentService(IncidentRepository incidentRepository,
                           IncidentSocialService incidentSocialService,
                           UserService userService) {
        this.incidentRepository = incidentRepository;
        this.incidentSocialService = incidentSocialService;
        this.userService = userService;
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    public Mono<IncidentResponse> create(CreateIncidentRequest request, UUID createdById) {
        Mono<UserAccount> offender = userService.findByIsu(request.offenderId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Offender not found")));
        Mono<Void> creatorCheck = userService.findById(createdById)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Creator not found")))
                .then();

        Mono<Void> expulsionCheck = offender.flatMapMany(user -> incidentRepository.findAllByOffenderId(user.getId()))
                .filter(incident -> incident.getPunishment() != null
                        && incident.getPunishment().name().equals("EXPULSION"))
                .filter(incident -> incident.getOccurredAt().isBefore(request.occurredAt())
                        || incident.getOccurredAt().equals(request.occurredAt()))
                .hasElements()
                .flatMap(hasExpulsion -> {
                    if (hasExpulsion) {
                        return Mono.error(new IllegalArgumentException("Offender already expelled; cannot add later incident"));
                    }
                    return Mono.empty();
                });

        return offender.flatMap(offenderUser ->
                        Mono.when(creatorCheck, expulsionCheck)
                                .then(Mono.just(offenderUser))
                )
                .flatMap(offenderUser -> {
                    Incident incident = new Incident(
                            UUID.randomUUID(),
                            request.title(),
                            request.description(),
                            request.place(),
                            request.department(),
                            request.type(),
                            request.occurredAt(),
                            Instant.now(),
                            offenderUser.getId(),
                            createdById,
                            ModerationStatus.PUBLISHED,
                            request.punishment(),
                            true
                    );
                    return incidentRepository.save(incident);
                })
                .flatMap(this::toResponse)
                .doOnNext(response -> sink.tryEmitNext(response));
    }

    public Flux<IncidentResponse> list(IncidentQuery query) {
        return filter(query)
                .flatMap(this::toResponse)
                .collectList()
                .flatMapMany(list -> Flux.fromIterable(sortResponses(list, query.sort())));
    }

    public Mono<IncidentResponse> get(UUID id) {
        return incidentRepository.findById(id)
                .flatMap(this::toResponse);
    }

    public Mono<IncidentResponse> updateModeration(UUID id, ModerationStatus status) {
        return incidentRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Incident not found")))
                .flatMap(incident -> {
                    incident.setModerationStatus(status);
                    return incidentRepository.save(incident);
                })
                .flatMap(this::toResponse)
                .doOnNext(response -> sink.tryEmitNext(response));
    }

    public Flux<IncidentResponse> stream() {
        return incidentRepository.findAll()
                .flatMap(this::toResponse)
                .concatWith(sink.asFlux());
    }

    public Mono<Double> incidentPoints(Incident incident) {
        return incidentSocialService.summary(incident.getId())
                .map(summary -> incident.getType().getBasePoints()
                        + (summary.likes() - summary.dislikes()) * 0.5);
    }

    public Flux<Incident> findAllIncidents() {
        return incidentRepository.findAll();
    }

    private Flux<Incident> filter(IncidentQuery query) {
        return incidentRepository.findAll()
                .filter(incident -> matchesQuery(incident, query))
                .filterWhen(incident -> matchesUserFilter(incident, query));
    }

    private boolean matchesQuery(Incident incident, IncidentQuery query) {
        if (query.type() != null && incident.getType() != query.type()) {
            return false;
        }
        if (query.from() != null && incident.getOccurredAt().isBefore(query.from())) {
            return false;
        }
        if (query.to() != null && incident.getOccurredAt().isAfter(query.to())) {
            return false;
        }
        if (query.query() != null && !query.query().isBlank()) {
            String needle = query.query().toLowerCase();
            String title = incident.getTitle() == null ? "" : incident.getTitle().toLowerCase();
            String description = incident.getDescription() == null ? "" : incident.getDescription().toLowerCase();
            String place = incident.getPlace() == null ? "" : incident.getPlace().toLowerCase();
            return title.contains(needle)
                    || description.contains(needle)
                    || place.contains(needle);
        }
        return true;
    }

    public Mono<IncidentResponse> toResponse(Incident incident) {
        Mono<IncidentSocialSummary> summary = incidentSocialService.summary(incident.getId());
        Mono<String> offender = userService.findById(incident.getOffenderId())
                .map(this::userIdentifier)
                .defaultIfEmpty(incident.getOffenderId().toString());
        Mono<String> creator = userService.findById(incident.getCreatedById())
                .map(this::userIdentifier)
                .defaultIfEmpty(incident.getCreatedById().toString());

        return Mono.zip(summary, offender, creator)
                .map(tuple -> new IncidentResponse(
                        incident.getId(),
                        incident.getTitle(),
                        incident.getDescription(),
                        incident.getPlace(),
                        incident.getDepartment(),
                        incident.getType(),
                        incident.getOccurredAt(),
                        incident.getCreatedAt(),
                        tuple.getT2(),
                        tuple.getT3(),
                        incident.getModerationStatus(),
                        incident.getPunishment(),
                        tuple.getT1().likes(),
                        tuple.getT1().dislikes(),
                        tuple.getT1().comments()
                ));
    }

    private Mono<Boolean> matchesUserFilter(Incident incident, IncidentQuery query) {
        if ((query.faculty() == null || query.faculty().isBlank())
                && (query.groupName() == null || query.groupName().isBlank())) {
            return Mono.just(true);
        }
        return userService.findById(incident.getOffenderId())
                .map(user -> {
                    if (query.faculty() != null && !query.faculty().isBlank()
                            && (user.getFaculty() == null
                            || !user.getFaculty().equalsIgnoreCase(query.faculty()))) {
                        return false;
                    }
                    if (query.groupName() != null && !query.groupName().isBlank()
                            && (user.getGroupName() == null
                            || !user.getGroupName().equalsIgnoreCase(query.groupName()))) {
                        return false;
                    }
                    return true;
                })
                .defaultIfEmpty(false);
    }

    private List<IncidentResponse> sortResponses(List<IncidentResponse> responses, IncidentSort sort) {
        if (sort == null || sort == IncidentSort.DATE_DESC) {
            responses.sort(Comparator.comparing(IncidentResponse::occurredAt).reversed());
            return responses;
        }
        if (sort == IncidentSort.DATE_ASC) {
            responses.sort(Comparator.comparing(IncidentResponse::occurredAt));
            return responses;
        }
        if (sort == IncidentSort.REACTIONS_DESC) {
            responses.sort(Comparator.comparingLong(IncidentResponse::likes)
                    .thenComparingLong(IncidentResponse::dislikes)
                    .reversed());
            return responses;
        }
        if (sort == IncidentSort.RATING_DESC) {
            responses.sort(Comparator.comparingDouble((IncidentResponse response) -> response.type().getBasePoints()
                    + (response.likes() - response.dislikes()) * 0.5).reversed());
            return responses;
        }
        return responses;
    }

    private String userIdentifier(UserAccount user) {
        if (user.getIsu() != null && !user.getIsu().isBlank()) {
            return user.getIsu();
        }
        return user.getUsername();
    }
}
