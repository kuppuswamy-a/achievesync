package com.achievesync.goalservice.config;

import com.achievesync.goalservice.aggregate.GoalAggregate;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.modelling.command.Repository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestAxonConfig {

    @Bean
    public EventStore eventStore() {
        return EmbeddedEventStore.builder()
                .storageEngine(new InMemoryEventStorageEngine())
                .build();
    }

    @Bean
    public Repository<GoalAggregate> goalAggregateRepository(EventStore eventStore) {
        return EventSourcingRepository.builder(GoalAggregate.class)
                .eventStore(eventStore)
                .build();
    }
}