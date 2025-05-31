class ConfigService {
  static final ConfigService _instance = ConfigService._internal();
  factory ConfigService() => _instance;
  ConfigService._internal();

  // Environment-based configuration
  static const String _env = String.fromEnvironment('ENV', defaultValue: 'development');
  
  String get environment => _env;
  
  String get userServiceUrl {
    switch (_env) {
      case 'production':
        return const String.fromEnvironment('PROD_USER_SERVICE_URL', 
               defaultValue: 'https://api.achievesync.com/user');
      case 'staging':
        return const String.fromEnvironment('STAGING_USER_SERVICE_URL',
               defaultValue: 'https://staging-api.achievesync.com/user');
      default:
        return const String.fromEnvironment('DEV_USER_SERVICE_URL',
               defaultValue: 'http://localhost:8081/api');
    }
  }
  
  String get goalServiceUrl {
    switch (_env) {
      case 'production':
        return const String.fromEnvironment('PROD_GOAL_SERVICE_URL',
               defaultValue: 'https://api.achievesync.com/goal');
      case 'staging':
        return const String.fromEnvironment('STAGING_GOAL_SERVICE_URL',
               defaultValue: 'https://staging-api.achievesync.com/goal');
      default:
        return const String.fromEnvironment('DEV_GOAL_SERVICE_URL',
               defaultValue: 'http://localhost:8082/api');
    }
  }

  bool get isProduction => _env == 'production';
  bool get isDevelopment => _env == 'development';
  bool get isStaging => _env == 'staging';

  // Feature flags
  bool get enableOfflineMode => true;
  bool get enablePushNotifications => !isDevelopment;
  bool get enableAnalytics => isProduction;
  bool get enableDebugLogging => isDevelopment;
  
  // API Configuration
  int get apiTimeout => 30000; // 30 seconds
  int get maxRetries => 3;
  Duration get retryDelay => const Duration(seconds: 2);
}