import 'package:json_annotation/json_annotation.dart';

part 'goal.g.dart';

enum GoalStatus { pending, inProgress, completed }

@JsonSerializable()
class Goal {
  final String goalId;
  final String userId;
  final String description;
  final DateTime targetDate;
  final GoalStatus status;
  final double progressPercentage;
  final DateTime createdAt;
  final DateTime updatedAt;
  final String? category;
  final List<String> tags;

  Goal({
    required this.goalId,
    required this.userId,
    required this.description,
    required this.targetDate,
    required this.status,
    required this.progressPercentage,
    required this.createdAt,
    required this.updatedAt,
    this.category,
    this.tags = const [],
  });

  factory Goal.fromJson(Map<String, dynamic> json) => _$GoalFromJson(json);
  Map<String, dynamic> toJson() => _$GoalToJson(this);
}

@JsonSerializable()
class GoalProgress {
  final String progressId;
  final String goalId;
  final double progressPercentage;
  final String? notes;
  final DateTime updateTimestamp;

  GoalProgress({
    required this.progressId,
    required this.goalId,
    required this.progressPercentage,
    this.notes,
    required this.updateTimestamp,
  });

  factory GoalProgress.fromJson(Map<String, dynamic> json) => _$GoalProgressFromJson(json);
  Map<String, dynamic> toJson() => _$GoalProgressToJson(this);
}