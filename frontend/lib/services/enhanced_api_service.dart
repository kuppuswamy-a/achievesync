import 'dart:convert';
import 'dart:async';
import 'package:http/http.dart' as http;
import '../models/user.dart';
import '../models/goal.dart';
import 'auth_service.dart';
import 'storage_service.dart';
import 'connectivity_service.dart';
import 'config_service.dart';

class EnhancedApiService {
  static final EnhancedApiService _instance = EnhancedApiService._internal();
  factory EnhancedApiService() => _instance;
  EnhancedApiService._internal();

  final AuthService _auth = AuthService();
  final StorageService _storage = StorageService();
  final ConnectivityService _connectivity = ConnectivityService();
  final ConfigService _config = ConfigService();

  // Enhanced HTTP client with retry logic
  Future<http.Response> _makeRequest(
    String method,
    String url,
    {Map<String, dynamic>? body, 
     Map<String, String>? headers,
     bool useAuth = true}) async {
    
    int attempts = 0;
    Exception? lastException;

    while (attempts < _config.maxRetries) {
      try {
        final Map<String, String> finalHeaders = {
          'Content-Type': 'application/json',
          ...?headers,
        };

        if (useAuth) {
          finalHeaders.addAll(_auth.getAuthHeaders());
        }

        http.Response response;
        final uri = Uri.parse(url);

        switch (method.toUpperCase()) {
          case 'GET':
            response = await http.get(uri, headers: finalHeaders)
                .timeout(Duration(milliseconds: _config.apiTimeout));
            break;
          case 'POST':
            response = await http.post(
              uri,
              headers: finalHeaders,
              body: body != null ? json.encode(body) : null,
            ).timeout(Duration(milliseconds: _config.apiTimeout));
            break;
          case 'PUT':
            response = await http.put(
              uri,
              headers: finalHeaders,
              body: body != null ? json.encode(body) : null,
            ).timeout(Duration(milliseconds: _config.apiTimeout));
            break;
          case 'DELETE':
            response = await http.delete(uri, headers: finalHeaders)
                .timeout(Duration(milliseconds: _config.apiTimeout));
            break;
          default:
            throw ArgumentError('Unsupported HTTP method: $method');
        }

        return response;
      } catch (e) {
        lastException = e is Exception ? e : Exception(e.toString());
        attempts++;
        
        if (attempts < _config.maxRetries) {
          await Future.delayed(_config.retryDelay);
        }
      }
    }

    throw lastException ?? Exception('Unknown error');
  }

  // Enhanced User Service API calls with offline support
  Future<User?> getUser(String userId) async {
    try {
      if (!_connectivity.isConnected) {
        // Return cached data if offline
        final userData = await _storage.getUserData();
        return userData != null ? User.fromJson(userData) : null;
      }

      final response = await _makeRequest(
        'GET',
        '${_config.userServiceUrl}/users/$userId',
      );

      if (response.statusCode == 200) {
        final user = User.fromJson(json.decode(response.body));
        // Cache the user data
        await _storage.saveUserData(user.toJson());
        return user;
      }
      return null;
    } catch (e) {
      if (_config.enableDebugLogging) {
        print('Error getting user: $e');
      }
      
      // Fallback to cached data
      final userData = await _storage.getUserData();
      return userData != null ? User.fromJson(userData) : null;
    }
  }

  Future<String?> createUser(String name, String email, String password) async {
    try {
      if (!_connectivity.isConnected) {
        // Queue for offline processing
        await _storage.addOfflineAction({
          'type': 'create_user',
          'data': {'name': name, 'email': email, 'password': password},
          'timestamp': DateTime.now().toIso8601String(),
        });
        return null;
      }

      final response = await _makeRequest(
        'POST',
        '${_config.userServiceUrl}/users',
        body: {'name': name, 'email': email, 'password': password},
        useAuth: false,
      );

      if (response.statusCode == 201) {
        return response.body;
      }
      return null;
    } catch (e) {
      if (_config.enableDebugLogging) {
        print('Error creating user: $e');
      }
      return null;
    }
  }

  Future<ConsistencyPoints?> getConsistencyPoints(String userId) async {
    try {
      if (!_connectivity.isConnected) {
        final cached = await _storage.getConsistencyPoints();
        return cached != null ? ConsistencyPoints.fromJson(cached) : null;
      }

      final response = await _makeRequest(
        'GET',
        '${_config.userServiceUrl}/users/$userId/points',
      );

      if (response.statusCode == 200) {
        final points = ConsistencyPoints.fromJson(json.decode(response.body));
        await _storage.saveConsistencyPoints(points.toJson());
        return points;
      }
      return null;
    } catch (e) {
      if (_config.enableDebugLogging) {
        print('Error getting consistency points: $e');
      }
      
      final cached = await _storage.getConsistencyPoints();
      return cached != null ? ConsistencyPoints.fromJson(cached) : null;
    }
  }

  // Enhanced Goal Service API calls
  Future<List<Goal>> getUserGoals(String userId, {String? category, String? status}) async {
    try {
      if (!_connectivity.isConnected) {
        final cached = await _storage.getGoalsData();
        if (cached != null) {
          return cached.map((json) => Goal.fromJson(json)).toList();
        }
        return [];
      }

      String url = '${_config.goalServiceUrl}/goals/user/$userId';
      final queryParams = <String, String>{};
      
      if (category != null) queryParams['category'] = category;
      if (status != null) queryParams['status'] = status;
      
      if (queryParams.isNotEmpty) {
        url += '?' + queryParams.entries.map((e) => '${e.key}=${e.value}').join('&');
      }

      final response = await _makeRequest('GET', url);

      if (response.statusCode == 200) {
        final List<dynamic> jsonList = json.decode(response.body);
        final goals = jsonList.map((json) => Goal.fromJson(json)).toList();
        
        // Cache the goals
        await _storage.saveGoalsData(goals.map((g) => g.toJson()).toList());
        
        return goals;
      }
      return [];
    } catch (e) {
      if (_config.enableDebugLogging) {
        print('Error getting user goals: $e');
      }
      
      // Return cached data
      final cached = await _storage.getGoalsData();
      if (cached != null) {
        return cached.map((json) => Goal.fromJson(json)).toList();
      }
      return [];
    }
  }

  Future<Goal?> getGoal(String goalId) async {
    try {
      final response = await _makeRequest(
        'GET',
        '${_config.goalServiceUrl}/goals/$goalId',
      );

      if (response.statusCode == 200) {
        return Goal.fromJson(json.decode(response.body));
      }
      return null;
    } catch (e) {
      if (_config.enableDebugLogging) {
        print('Error getting goal: $e');
      }
      return null;
    }
  }

  Future<String?> createGoal(String userId, String description, DateTime targetDate, 
                           {String? category, List<String>? tags}) async {
    try {
      final goalData = {
        'userId': userId,
        'description': description,
        'targetDate': targetDate.toIso8601String().split('T')[0],
        if (category != null) 'category': category,
        if (tags != null) 'tags': tags,
      };

      if (!_connectivity.isConnected) {
        await _storage.addOfflineAction({
          'type': 'create_goal',
          'data': goalData,
          'timestamp': DateTime.now().toIso8601String(),
        });
        return 'offline-${DateTime.now().millisecondsSinceEpoch}';
      }

      final response = await _makeRequest(
        'POST',
        '${_config.goalServiceUrl}/goals',
        body: goalData,
      );

      if (response.statusCode == 201) {
        return response.body;
      }
      return null;
    } catch (e) {
      if (_config.enableDebugLogging) {
        print('Error creating goal: $e');
      }
      return null;
    }
  }

  Future<bool> updateGoalProgress(String goalId, double progressPercentage, String? notes) async {
    try {
      final progressData = {
        'progressPercentage': progressPercentage,
        'notes': notes ?? '',
      };

      if (!_connectivity.isConnected) {
        await _storage.addOfflineAction({
          'type': 'update_goal_progress',
          'goalId': goalId,
          'data': progressData,
          'timestamp': DateTime.now().toIso8601String(),
        });
        return true;
      }

      final response = await _makeRequest(
        'PUT',
        '${_config.goalServiceUrl}/goals/$goalId/progress',
        body: progressData,
      );

      return response.statusCode == 200;
    } catch (e) {
      if (_config.enableDebugLogging) {
        print('Error updating goal progress: $e');
      }
      return false;
    }
  }

  Future<bool> completeGoal(String goalId) async {
    try {
      if (!_connectivity.isConnected) {
        await _storage.addOfflineAction({
          'type': 'complete_goal',
          'goalId': goalId,
          'timestamp': DateTime.now().toIso8601String(),
        });
        return true;
      }

      final response = await _makeRequest(
        'PUT',
        '${_config.goalServiceUrl}/goals/$goalId/complete',
      );

      return response.statusCode == 200;
    } catch (e) {
      if (_config.enableDebugLogging) {
        print('Error completing goal: $e');
      }
      return false;
    }
  }

  // New Streak API
  Future<GoalStreak?> getGoalStreak(String goalId) async {
    try {
      final response = await _makeRequest(
        'GET',
        '${_config.goalServiceUrl}/goals/$goalId/streak',
      );

      if (response.statusCode == 200) {
        return GoalStreak.fromJson(json.decode(response.body));
      }
      return null;
    } catch (e) {
      if (_config.enableDebugLogging) {
        print('Error getting goal streak: $e');
      }
      return null;
    }
  }

  // Process offline actions when connection is restored
  Future<void> processOfflineActions() async {
    if (!_connectivity.isConnected) return;

    final actions = await _storage.getOfflineActions();
    if (actions.isEmpty) return;

    for (final action in actions) {
      try {
        switch (action['type']) {
          case 'create_user':
            final data = action['data'];
            await createUser(data['name'], data['email'], data['password']);
            break;
          case 'create_goal':
            final data = action['data'];
            await createGoal(
              data['userId'],
              data['description'],
              DateTime.parse(data['targetDate']),
              category: data['category'],
              tags: data['tags']?.cast<String>(),
            );
            break;
          case 'update_goal_progress':
            final data = action['data'];
            await updateGoalProgress(
              action['goalId'],
              data['progressPercentage'],
              data['notes'],
            );
            break;
          case 'complete_goal':
            await completeGoal(action['goalId']);
            break;
        }
      } catch (e) {
        if (_config.enableDebugLogging) {
          print('Error processing offline action: $e');
        }
      }
    }

    await _storage.clearOfflineActions();
  }
}

// New Streak Model
class GoalStreak {
  final String goalId;
  final int currentStreakDays;
  final int longestStreakDays;
  final DateTime? lastStreakUpdate;
  final bool isStreakActive;

  GoalStreak({
    required this.goalId,
    required this.currentStreakDays,
    required this.longestStreakDays,
    this.lastStreakUpdate,
    required this.isStreakActive,
  });

  factory GoalStreak.fromJson(Map<String, dynamic> json) {
    return GoalStreak(
      goalId: json['goal_id'],
      currentStreakDays: json['current_streak_days'],
      longestStreakDays: json['longest_streak_days'],
      lastStreakUpdate: json['last_streak_update'] != null 
          ? DateTime.parse(json['last_streak_update'])
          : null,
      isStreakActive: json['is_streak_active'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'goal_id': goalId,
      'current_streak_days': currentStreakDays,
      'longest_streak_days': longestStreakDays,
      'last_streak_update': lastStreakUpdate?.toIso8601String(),
      'is_streak_active': isStreakActive,
    };
  }
}