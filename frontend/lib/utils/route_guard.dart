import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../services/auth_service.dart';

class RouteGuard {
  static final AuthService _auth = AuthService();

  static String? authGuard(BuildContext context, GoRouterState state) {
    final isAuthenticated = _auth.isAuthenticated;
    final isLoginRoute = state.matchedLocation == '/' || state.matchedLocation == '/login';
    
    if (!isAuthenticated && !isLoginRoute) {
      return '/';
    }
    
    if (isAuthenticated && isLoginRoute) {
      return '/dashboard';
    }
    
    return null;
  }

  static bool requiresAuth(String route) {
    const publicRoutes = ['/', '/login', '/register'];
    return !publicRoutes.contains(route);
  }
}

class AuthGuardWidget extends StatefulWidget {
  final Widget child;
  final VoidCallback? onUnauthenticated;

  const AuthGuardWidget({
    super.key,
    required this.child,
    this.onUnauthenticated,
  });

  @override
  State<AuthGuardWidget> createState() => _AuthGuardWidgetState();
}

class _AuthGuardWidgetState extends State<AuthGuardWidget> {
  final AuthService _auth = AuthService();
  bool _isChecking = true;

  @override
  void initState() {
    super.initState();
    _checkAuth();
  }

  Future<void> _checkAuth() async {
    // Validate token if exists
    if (_auth.token != null) {
      final isValid = await _auth.validateToken();
      if (!isValid) {
        await _auth.logout();
        if (widget.onUnauthenticated != null) {
          widget.onUnauthenticated!();
        } else if (mounted) {
          context.go('/');
        }
      }
    }
    
    if (mounted) {
      setState(() {
        _isChecking = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isChecking) {
      return const Scaffold(
        body: Center(
          child: CircularProgressIndicator(),
        ),
      );
    }

    if (!_auth.isAuthenticated) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (widget.onUnauthenticated != null) {
          widget.onUnauthenticated!();
        } else {
          context.go('/');
        }
      });
      
      return const Scaffold(
        body: Center(
          child: CircularProgressIndicator(),
        ),
      );
    }

    return widget.child;
  }
}