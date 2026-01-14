package org.example.hooliguns.repository;

import org.example.hooliguns.domain.CommentReport;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CommentReportRepository extends ReactiveMongoRepository<CommentReport, String> {
}
