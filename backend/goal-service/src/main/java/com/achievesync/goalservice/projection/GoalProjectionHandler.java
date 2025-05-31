package com.achievesync.goalservice.projection;

import com.achievesync.goalservice.event.GoalCompletedEvent;
import com.achievesync.goalservice.event.GoalCreatedEvent;
import com.achievesync.goalservice.event.GoalProgressUpdatedEvent;
import com.achievesync.goalservice.query.FindGoalQuery;
import com.achievesync.goalservice.query.FindGoalsByUserQuery;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GoalProjectionHandler {
    
    private final GoalRepository goalRepository;
    private final GoalProgressRepository progressRepository;

    public GoalProjectionHandler(GoalRepository goalRepository, GoalProgressRepository progressRepository) {
        this.goalRepository = goalRepository;
        this.progressRepository = progressRepository;
    }

    @EventHandler
    public void on(GoalCreatedEvent event) {
        GoalProjection goal = new GoalProjection();
        goal.setGoalId(event.getGoalId());
        goal.setUserId(event.getUserId());
        goal.setDescription(event.getDescription());
        goal.setTargetDate(event.getTargetDate());
        goal.setStatus(GoalStatus.PENDING);
        goal.setProgressPercentage(0.0);
        goal.setCreatedAt(event.getCreatedAt());
        goal.setUpdatedAt(event.getCreatedAt());
        goal.setCategory(event.getCategory());
        goal.setTags(event.getTags() != null ? event.getTags() : new java.util.ArrayList<>());
        
        goalRepository.save(goal);
    }

    @EventHandler
    public void on(GoalProgressUpdatedEvent event) {
        Optional<GoalProjection> goalOpt = goalRepository.findById(event.getGoalId());
        if (goalOpt.isPresent()) {
            GoalProjection goal = goalOpt.get();
            goal.setProgressPercentage(event.getProgressPercentage());
            goal.setStatus(event.getProgressPercentage() >= 100.0 ? GoalStatus.COMPLETED : GoalStatus.IN_PROGRESS);
            goal.setUpdatedAt(event.getUpdatedAt());
            goalRepository.save(goal);

            // Record progress history
            GoalProgressProjection progress = new GoalProgressProjection(
                event.getGoalId(),
                event.getProgressPercentage(),
                event.getNotes(),
                event.getUpdatedAt()
            );
            progressRepository.save(progress);
        }
    }

    @EventHandler
    public void on(GoalCompletedEvent event) {
        Optional<GoalProjection> goalOpt = goalRepository.findById(event.getGoalId());
        if (goalOpt.isPresent()) {
            GoalProjection goal = goalOpt.get();
            goal.setStatus(GoalStatus.COMPLETED);
            goal.setProgressPercentage(100.0);
            goal.setUpdatedAt(event.getCompletedAt());
            goalRepository.save(goal);
        }
    }

    @QueryHandler
    public GoalProjection handle(FindGoalQuery query) {
        return goalRepository.findById(query.getGoalId()).orElse(null);
    }

    @QueryHandler
    public List<GoalProjection> handle(FindGoalsByUserQuery query) {
        return goalRepository.findByUserId(query.getUserId());
    }
}

interface GoalRepository extends JpaRepository<GoalProjection, String> {
    List<GoalProjection> findByUserId(String userId);
}

interface GoalProgressRepository extends JpaRepository<GoalProgressProjection, String> {
    List<GoalProgressProjection> findByGoalIdOrderByUpdateTimestampDesc(String goalId);
}