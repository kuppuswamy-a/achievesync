package com.achievesync.goalservice.config;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.modelling.command.Repository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.achievesync.goalservice.aggregate.GoalAggregate;

@Configuration
@Profile("!test")
public class AxonConfig {

    @Bean
    public Repository<GoalAggregate> goalAggregateRepository(EventStore eventStore) {
        return EventSourcingRepository.builder(GoalAggregate.class)
                .eventStore(eventStore)
                .build();
    }
    
    @Bean
    public EventBus eventBus() {
        return SimpleEventBus.builder().build();
    }
}