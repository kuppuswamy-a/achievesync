import 'dart:async';
import 'package:connectivity_plus/connectivity_plus.dart';

class ConnectivityService {
  static final ConnectivityService _instance = ConnectivityService._internal();
  factory ConnectivityService() => _instance;
  ConnectivityService._internal();

  final Connectivity _connectivity = Connectivity();
  StreamSubscription<List<ConnectivityResult>>? _subscription;

  bool _isConnected = true;
  final StreamController<bool> _connectionController = StreamController<bool>.broadcast();

  bool get isConnected => _isConnected;
  Stream<bool> get connectionStream => _connectionController.stream;

  Future<void> initialize() async {
    // Check initial connectivity
    final results = await _connectivity.checkConnectivity();
    _updateConnectionStatus(results);

    // Listen for connectivity changes
    _subscription = _connectivity.onConnectivityChanged.listen(_updateConnectionStatus);
  }

  void _updateConnectionStatus(List<ConnectivityResult> results) {
    final wasConnected = _isConnected;
    _isConnected = results.any((result) => 
      result == ConnectivityResult.mobile || 
      result == ConnectivityResult.wifi ||
      result == ConnectivityResult.ethernet
    );

    if (wasConnected != _isConnected) {
      _connectionController.add(_isConnected);
    }
  }

  Future<bool> hasInternetConnection() async {
    try {
      final results = await _connectivity.checkConnectivity();
      return results.any((result) => 
        result == ConnectivityResult.mobile || 
        result == ConnectivityResult.wifi ||
        result == ConnectivityResult.ethernet
      );
    } catch (e) {
      return false;
    }
  }

  void dispose() {
    _subscription?.cancel();
    _connectionController.close();
  }
}