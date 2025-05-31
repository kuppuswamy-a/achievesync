import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/user.dart';
import 'storage_service.dart';
import 'config_service.dart';

class AuthService {
  static final AuthService _instance = AuthService._internal();
  factory AuthService() => _instance;
  AuthService._internal();

  final StorageService _storage = StorageService();
  final ConfigService _config = ConfigService();
  
  String? _token;
  User? _currentUser;

  String? get token => _token;
  User? get currentUser => _currentUser;
  bool get isAuthenticated => _token != null && _currentUser != null;

  Future<void> initialize() async {
    await _loadStoredAuth();
  }

  Future<void> _loadStoredAuth() async {
    _token = await _storage.getToken();
    final userData = await _storage.getUserData();
    if (userData != null) {
      _currentUser = User.fromJson(userData);
    }
  }

  Future<LoginResult> login(String email, String password) async {
    try {
      final response = await http.post(
        Uri.parse('${_config.userServiceUrl}/users/login'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({
          'email': email,
          'password': password,
        }),
      );

      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        
        if (data['success'] == true) {
          _token = data['token'];
          _currentUser = User.fromJson(data['user']);
          
          // Store credentials
          await _storage.saveToken(_token!);
          await _storage.saveUserData(_currentUser!.toJson());
          
          return LoginResult(success: true, user: _currentUser, token: _token);
        } else {
          return LoginResult(success: false, message: data['message']);
        }
      } else {
        return LoginResult(success: false, message: 'Login failed');
      }
    } catch (e) {
      return LoginResult(success: false, message: 'Network error: $e');
    }
  }

  Future<RegisterResult> register(String name, String email, String password) async {
    try {
      final response = await http.post(
        Uri.parse('${_config.userServiceUrl}/users'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({
          'name': name,
          'email': email,
          'password': password,
        }),
      );

      if (response.statusCode == 201) {
        final userId = response.body;
        
        // Auto-login after successful registration
        final loginResult = await login(email, password);
        if (loginResult.success) {
          return RegisterResult(success: true, userId: userId);
        } else {
          return RegisterResult(success: false, message: 'Registration successful but login failed');
        }
      } else {
        return RegisterResult(success: false, message: 'Registration failed');
      }
    } catch (e) {
      return RegisterResult(success: false, message: 'Network error: $e');
    }
  }

  Future<void> logout() async {
    _token = null;
    _currentUser = null;
    await _storage.clearAuth();
  }

  Map<String, String> getAuthHeaders() {
    final headers = <String, String>{'Content-Type': 'application/json'};
    if (_token != null) {
      headers['Authorization'] = 'Bearer $_token';
    }
    return headers;
  }

  Future<bool> validateToken() async {
    if (_token == null) return false;
    
    try {
      final response = await http.get(
        Uri.parse('${_config.userServiceUrl}/users/profile'),
        headers: getAuthHeaders(),
      );
      
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }
}

class LoginResult {
  final bool success;
  final User? user;
  final String? token;
  final String? message;

  LoginResult({
    required this.success,
    this.user,
    this.token,
    this.message,
  });
}

class RegisterResult {
  final bool success;
  final String? userId;
  final String? message;

  RegisterResult({
    required this.success,
    this.userId,
    this.message,
  });
}