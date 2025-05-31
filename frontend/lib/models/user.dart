import 'package:json_annotation/json_annotation.dart';

part 'user.g.dart';

@JsonSerializable()
class User {
  final String userId;
  final String name;
  final String email;
  final DateTime createdAt;
  final DateTime updatedAt;

  User({
    required this.userId,
    required this.name,
    required this.email,
    required this.createdAt,
    required this.updatedAt,
  });

  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);
  Map<String, dynamic> toJson() => _$UserToJson(this);
}

@JsonSerializable()
class ConsistencyPoints {
  final String userId;
  final int totalPoints;
  final DateTime lastUpdated;

  ConsistencyPoints({
    required this.userId,
    required this.totalPoints,
    required this.lastUpdated,
  });

  factory ConsistencyPoints.fromJson(Map<String, dynamic> json) => _$ConsistencyPointsFromJson(json);
  Map<String, dynamic> toJson() => _$ConsistencyPointsToJson(this);
}