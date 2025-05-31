import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/user.dart';
import '../models/goal.dart';
import 'auth_service.dart';
import 'storage_service.dart';
import 'connectivity_service.dart';
import 'config_service.dart';

class ApiService {
  static final ApiService _instance = ApiService._internal();
  factory ApiService() => _instance;
  ApiService._internal();

  final AuthService _auth = AuthService();
  final StorageService _storage = StorageService();
  final ConnectivityService _connectivity = ConnectivityService();
  final ConfigService _config = ConfigService();

  // User Service API calls
  Future<User?> getUser(String userId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUserUrl/users/$userId'),
        headers: {'Content-Type': 'application/json'},
      );

      if (response.statusCode == 200) {
        return User.fromJson(json.decode(response.body));
      }
      return null;
    } catch (e) {
      print('Error getting user: $e');
      return null;
    }
  }

  Future<String?> createUser(String name, String email, String password) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUserUrl/users'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({
          'name': name,
          'email': email,
          'password': password,
        }),
      );

      if (response.statusCode == 201) {
        return response.body;
      }
      return null;
    } catch (e) {
      print('Error creating user: $e');
      return null;
    }
  }

  Future<ConsistencyPoints?> getConsistencyPoints(String userId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUserUrl/users/$userId/points'),
        headers: {'Content-Type': 'application/json'},
      );

      if (response.statusCode == 200) {
        return ConsistencyPoints.fromJson(json.decode(response.body));
      }
      return null;
    } catch (e) {
      print('Error getting consistency points: $e');
      return null;
    }
  }

  // Goal Service API calls
  Future<List<Goal>> getUserGoals(String userId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseGoalUrl/goals/user/$userId'),
        headers: {'Content-Type': 'application/json'},
      );

      if (response.statusCode == 200) {
        final List<dynamic> jsonList = json.decode(response.body);
        return jsonList.map((json) => Goal.fromJson(json)).toList();
      }
      return [];
    } catch (e) {
      print('Error getting user goals: $e');
      return [];
    }
  }

  Future<Goal?> getGoal(String goalId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseGoalUrl/goals/$goalId'),
        headers: {'Content-Type': 'application/json'},
      );

      if (response.statusCode == 200) {
        return Goal.fromJson(json.decode(response.body));
      }
      return null;
    } catch (e) {
      print('Error getting goal: $e');
      return null;
    }
  }

  Future<String?> createGoal(String userId, String description, DateTime targetDate) async {
    try {
      final response = await http.post(
        Uri.parse('$baseGoalUrl/goals'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({
          'userId': userId,
          'description': description,
          'targetDate': targetDate.toIso8601String().split('T')[0],
        }),
      );

      if (response.statusCode == 201) {
        return response.body;
      }
      return null;
    } catch (e) {
      print('Error creating goal: $e');
      return null;
    }
  }

  Future<bool> updateGoalProgress(String goalId, double progressPercentage, String? notes) async {
    try {
      final response = await http.put(
        Uri.parse('$baseGoalUrl/goals/$goalId/progress'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({
          'progressPercentage': progressPercentage,
          'notes': notes ?? '',
        }),
      );

      return response.statusCode == 200;
    } catch (e) {
      print('Error updating goal progress: $e');
      return false;
    }
  }

  Future<bool> completeGoal(String goalId) async {
    try {
      final response = await http.put(
        Uri.parse('$baseGoalUrl/goals/$goalId/complete'),
        headers: {'Content-Type': 'application/json'},
      );

      return response.statusCode == 200;
    } catch (e) {
      print('Error completing goal: $e');
      return false;
    }
  }
}