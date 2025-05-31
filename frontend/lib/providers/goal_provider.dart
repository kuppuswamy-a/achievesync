import 'package:flutter/material.dart';
import '../models/goal.dart';
import '../services/api_service.dart';

class GoalProvider with ChangeNotifier {
  final ApiService _apiService = ApiService();
  
  List<Goal> _goals = [];
  Goal? _selectedGoal;
  bool _isLoading = false;

  List<Goal> get goals => _goals;
  Goal? get selectedGoal => _selectedGoal;
  bool get isLoading => _isLoading;

  List<Goal> get activeGoals => _goals.where((goal) => 
    goal.status == GoalStatus.pending || goal.status == GoalStatus.inProgress).toList();
  
  List<Goal> get completedGoals => _goals.where((goal) => 
    goal.status == GoalStatus.completed).toList();

  Future<void> loadUserGoals(String userId) async {
    _isLoading = true;
    notifyListeners();

    try {
      _goals = await _apiService.getUserGoals(userId);
    } catch (e) {
      print('Error loading goals: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> loadGoal(String goalId) async {
    _isLoading = true;
    notifyListeners();

    try {
      _selectedGoal = await _apiService.getGoal(goalId);
    } catch (e) {
      print('Error loading goal: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<String?> createGoal(String userId, String description, DateTime targetDate) async {
    _isLoading = true;
    notifyListeners();

    try {
      final goalId = await _apiService.createGoal(userId, description, targetDate);
      if (goalId != null) {
        await loadUserGoals(userId);
      }
      return goalId;
    } catch (e) {
      print('Error creating goal: $e');
      return null;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> updateGoalProgress(String goalId, double progressPercentage, String? notes) async {
    try {
      final success = await _apiService.updateGoalProgress(goalId, progressPercentage, notes);
      if (success) {
        // Refresh the specific goal
        await loadGoal(goalId);
        // Update the goal in the list
        final goalIndex = _goals.indexWhere((g) => g.goalId == goalId);
        if (goalIndex != -1 && _selectedGoal != null) {
          _goals[goalIndex] = _selectedGoal!;
          notifyListeners();
        }
      }
      return success;
    } catch (e) {
      print('Error updating goal progress: $e');
      return false;
    }
  }

  Future<bool> completeGoal(String goalId) async {
    try {
      final success = await _apiService.completeGoal(goalId);
      if (success) {
        await loadGoal(goalId);
        final goalIndex = _goals.indexWhere((g) => g.goalId == goalId);
        if (goalIndex != -1 && _selectedGoal != null) {
          _goals[goalIndex] = _selectedGoal!;
          notifyListeners();
        }
      }
      return success;
    } catch (e) {
      print('Error completing goal: $e');
      return false;
    }
  }
}