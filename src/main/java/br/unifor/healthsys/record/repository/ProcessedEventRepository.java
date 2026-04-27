package br.unifor.healthsys.record.repository;

import br.unifor.healthsys.record.model.ProcessedEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessedEventRepository extends MongoRepository<ProcessedEvent, String> {

    boolean existsByCorrelationId(String correlationId);
}
