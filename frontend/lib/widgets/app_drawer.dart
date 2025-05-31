import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import '../providers/user_provider.dart';

class AppDrawer extends StatelessWidget {
  const AppDrawer({super.key});

  @override
  Widget build(BuildContext context) {
    return Drawer(
      child: Column(
        children: [
          Consumer<UserProvider>(
            builder: (context, userProvider, child) {
              return UserAccountsDrawerHeader(
                accountName: Text(userProvider.currentUser?.name ?? 'User'),
                accountEmail: Text(userProvider.currentUser?.email ?? ''),
                currentAccountPicture: CircleAvatar(
                  backgroundColor: Colors.white,
                  child: Text(
                    userProvider.currentUser?.name[0].toUpperCase() ?? 'U',
                    style: TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                      color: Theme.of(context).primaryColor,
                    ),
                  ),
                ),
                decoration: BoxDecoration(
                  color: Theme.of(context).primaryColor,
                ),
              );
            },
          ),
          ListTile(
            leading: const Icon(Icons.dashboard),
            title: const Text('Dashboard'),
            onTap: () {
              Navigator.pop(context);
              context.go('/dashboard');
            },
          ),
          ListTile(
            leading: const Icon(Icons.track_changes),
            title: const Text('Goals'),
            onTap: () {
              Navigator.pop(context);
              context.go('/goals');
            },
          ),
          ListTile(
            leading: const Icon(Icons.person),
            title: const Text('Profile'),
            onTap: () {
              Navigator.pop(context);
              context.go('/profile');
            },
          ),
          const Divider(),
          ListTile(
            leading: const Icon(Icons.settings),
            title: const Text('Settings'),
            onTap: () {
              Navigator.pop(context);
              // TODO: Navigate to settings
            },
          ),
          const Spacer(),
          ListTile(
            leading: const Icon(Icons.logout),
            title: const Text('Logout'),
            onTap: () {
              Provider.of<UserProvider>(context, listen: false).logout();
              Navigator.pop(context);
              context.go('/');
            },
          ),
        ],
      ),
    );
  }
}