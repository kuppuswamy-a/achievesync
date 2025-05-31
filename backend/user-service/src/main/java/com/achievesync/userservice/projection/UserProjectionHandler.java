package com.achievesync.userservice.projection;

import com.achievesync.userservice.event.ConsistencyPointsAwardedEvent;
import com.achievesync.userservice.event.UserCreatedEvent;
import com.achievesync.userservice.event.UserProfileUpdatedEvent;
import com.achievesync.userservice.query.FindUserByEmailQuery;
import com.achievesync.userservice.query.FindUserQuery;
import com.achievesync.userservice.query.GetConsistencyPointsQuery;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserProjectionHandler {
    
    private final UserRepository userRepository;
    private final ConsistencyPointsRepository consistencyPointsRepository;

    public UserProjectionHandler(UserRepository userRepository, 
                               ConsistencyPointsRepository consistencyPointsRepository) {
        this.userRepository = userRepository;
        this.consistencyPointsRepository = consistencyPointsRepository;
    }

    @EventHandler
    public void on(UserCreatedEvent event) {
        UserProjection user = new UserProjection(
            event.getUserId(),
            event.getName(),
            event.getEmail(),
            event.getCreatedAt()
        );
        userRepository.save(user);

        ConsistencyPointsProjection points = new ConsistencyPointsProjection(
            event.getUserId(),
            0,
            event.getCreatedAt()
        );
        consistencyPointsRepository.save(points);
    }

    @EventHandler
    public void on(UserProfileUpdatedEvent event) {
        Optional<UserProjection> userOpt = userRepository.findById(event.getUserId());
        if (userOpt.isPresent()) {
            UserProjection user = userOpt.get();
            user.setName(event.getName());
            user.setEmail(event.getEmail());
            user.setUpdatedAt(event.getUpdatedAt());
            userRepository.save(user);
        }
    }

    @EventHandler
    public void on(ConsistencyPointsAwardedEvent event) {
        Optional<ConsistencyPointsProjection> pointsOpt = consistencyPointsRepository.findById(event.getUserId());
        if (pointsOpt.isPresent()) {
            ConsistencyPointsProjection points = pointsOpt.get();
            points.setTotalPoints(event.getTotalPoints());
            points.setLastUpdated(event.getAwardedAt());
            consistencyPointsRepository.save(points);
        }
    }

    @QueryHandler
    public UserProjection handle(FindUserQuery query) {
        return userRepository.findById(query.getUserId()).orElse(null);
    }

    @QueryHandler
    public UserProjection handle(FindUserByEmailQuery query) {
        return userRepository.findByEmail(query.getEmail()).orElse(null);
    }

    @QueryHandler
    public ConsistencyPointsProjection handle(GetConsistencyPointsQuery query) {
        return consistencyPointsRepository.findById(query.getUserId()).orElse(null);
    }
}

interface UserRepository extends JpaRepository<UserProjection, String> {
    Optional<UserProjection> findByEmail(String email);
}

interface ConsistencyPointsRepository extends JpaRepository<ConsistencyPointsProjection, String> {
}