import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

class StorageService {
  static final StorageService _instance = StorageService._internal();
  factory StorageService() => _instance;
  StorageService._internal();

  static const String _tokenKey = 'auth_token';
  static const String _userDataKey = 'user_data';
  static const String _goalsDataKey = 'goals_data';
  static const String _consistencyPointsKey = 'consistency_points';
  static const String _offlineActionsKey = 'offline_actions';

  SharedPreferences? _prefs;

  Future<void> initialize() async {
    _prefs ??= await SharedPreferences.getInstance();
  }

  Future<SharedPreferences> get prefs async {
    _prefs ??= await SharedPreferences.getInstance();
    return _prefs!;
  }

  // Auth Storage
  Future<void> saveToken(String token) async {
    final p = await prefs;
    await p.setString(_tokenKey, token);
  }

  Future<String?> getToken() async {
    final p = await prefs;
    return p.getString(_tokenKey);
  }

  Future<void> saveUserData(Map<String, dynamic> userData) async {
    final p = await prefs;
    await p.setString(_userDataKey, json.encode(userData));
  }

  Future<Map<String, dynamic>?> getUserData() async {
    final p = await prefs;
    final data = p.getString(_userDataKey);
    if (data != null) {
      return json.decode(data);
    }
    return null;
  }

  Future<void> clearAuth() async {
    final p = await prefs;
    await p.remove(_tokenKey);
    await p.remove(_userDataKey);
  }

  // Goals Cache
  Future<void> saveGoalsData(List<Map<String, dynamic>> goals) async {
    final p = await prefs;
    await p.setString(_goalsDataKey, json.encode(goals));
  }

  Future<List<Map<String, dynamic>>?> getGoalsData() async {
    final p = await prefs;
    final data = p.getString(_goalsDataKey);
    if (data != null) {
      final List<dynamic> decoded = json.decode(data);
      return decoded.cast<Map<String, dynamic>>();
    }
    return null;
  }

  // Consistency Points Cache
  Future<void> saveConsistencyPoints(Map<String, dynamic> points) async {
    final p = await prefs;
    await p.setString(_consistencyPointsKey, json.encode(points));
  }

  Future<Map<String, dynamic>?> getConsistencyPoints() async {
    final p = await prefs;
    final data = p.getString(_consistencyPointsKey);
    if (data != null) {
      return json.decode(data);
    }
    return null;
  }

  // Offline Actions Queue
  Future<void> addOfflineAction(Map<String, dynamic> action) async {
    final actions = await getOfflineActions();
    actions.add(action);
    await _saveOfflineActions(actions);
  }

  Future<List<Map<String, dynamic>>> getOfflineActions() async {
    final p = await prefs;
    final data = p.getString(_offlineActionsKey);
    if (data != null) {
      final List<dynamic> decoded = json.decode(data);
      return decoded.cast<Map<String, dynamic>>();
    }
    return [];
  }

  Future<void> _saveOfflineActions(List<Map<String, dynamic>> actions) async {
    final p = await prefs;
    await p.setString(_offlineActionsKey, json.encode(actions));
  }

  Future<void> clearOfflineActions() async {
    final p = await prefs;
    await p.remove(_offlineActionsKey);
  }

  // Clear all cached data
  Future<void> clearAll() async {
    final p = await prefs;
    await p.clear();
  }
}