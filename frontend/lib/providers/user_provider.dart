import 'package:flutter/material.dart';
import '../models/user.dart';
import '../services/api_service.dart';

class UserProvider with ChangeNotifier {
  final ApiService _apiService = ApiService();
  
  User? _currentUser;
  ConsistencyPoints? _consistencyPoints;
  bool _isLoading = false;

  User? get currentUser => _currentUser;
  ConsistencyPoints? get consistencyPoints => _consistencyPoints;
  bool get isLoading => _isLoading;

  Future<void> loadUser(String userId) async {
    _isLoading = true;
    notifyListeners();

    try {
      _currentUser = await _apiService.getUser(userId);
      _consistencyPoints = await _apiService.getConsistencyPoints(userId);
    } catch (e) {
      print('Error loading user: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<String?> createUser(String name, String email, String password) async {
    _isLoading = true;
    notifyListeners();

    try {
      final userId = await _apiService.createUser(name, email, password);
      if (userId != null) {
        await loadUser(userId);
      }
      return userId;
    } catch (e) {
      print('Error creating user: $e');
      return null;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> refreshConsistencyPoints() async {
    if (_currentUser != null) {
      _consistencyPoints = await _apiService.getConsistencyPoints(_currentUser!.userId);
      notifyListeners();
    }
  }

  void logout() {
    _currentUser = null;
    _consistencyPoints = null;
    notifyListeners();
  }
}