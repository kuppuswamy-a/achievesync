import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'providers/user_provider.dart';
import 'providers/goal_provider.dart';
import 'screens/auth/login_screen.dart';
import 'screens/dashboard/dashboard_screen.dart';
import 'screens/goals/goals_screen.dart';
import 'screens/goals/goal_detail_screen.dart';
import 'screens/profile/profile_screen.dart';
import 'utils/theme.dart';

void main() {
  runApp(const AchieveSyncApp());
}

class AchieveSyncApp extends StatelessWidget {
  const AchieveSyncApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => UserProvider()),
        ChangeNotifierProvider(create: (_) => GoalProvider()),
      ],
      child: MaterialApp.router(
        title: 'AchieveSync',
        theme: AppTheme.lightTheme,
        darkTheme: AppTheme.darkTheme,
        themeMode: ThemeMode.system,
        routerConfig: _router,
      ),
    );
  }
}

final GoRouter _router = GoRouter(
  routes: [
    GoRoute(
      path: '/',
      builder: (context, state) => const LoginScreen(),
    ),
    GoRoute(
      path: '/dashboard',
      builder: (context, state) => const DashboardScreen(),
    ),
    GoRoute(
      path: '/goals',
      builder: (context, state) => const GoalsScreen(),
    ),
    GoRoute(
      path: '/goals/:id',
      builder: (context, state) => GoalDetailScreen(
        goalId: state.pathParameters['id']!,
      ),
    ),
    GoRoute(
      path: '/profile',
      builder: (context, state) => const ProfileScreen(),
    ),
  ],
);