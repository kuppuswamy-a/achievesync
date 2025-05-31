import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import '../../providers/user_provider.dart';
import '../../widgets/app_drawer.dart';
import '../../services/storage_service.dart';
import '../../services/auth_service.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  final StorageService _storage = StorageService();
  final AuthService _auth = AuthService();
  
  bool _notificationsEnabled = true;
  bool _offlineModeEnabled = true;
  bool _darkModeEnabled = false;
  String _selectedTheme = 'System';

  @override
  void initState() {
    super.initState();
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    // Load saved settings from storage
    final prefs = await _storage.prefs;
    setState(() {
      _notificationsEnabled = prefs.getBool('notifications_enabled') ?? true;
      _offlineModeEnabled = prefs.getBool('offline_mode_enabled') ?? true;
      _darkModeEnabled = prefs.getBool('dark_mode_enabled') ?? false;
      _selectedTheme = prefs.getString('selected_theme') ?? 'System';
    });
  }

  Future<void> _saveSettings() async {
    final prefs = await _storage.prefs;
    await prefs.setBool('notifications_enabled', _notificationsEnabled);
    await prefs.setBool('offline_mode_enabled', _offlineModeEnabled);
    await prefs.setBool('dark_mode_enabled', _darkModeEnabled);
    await prefs.setString('selected_theme', _selectedTheme);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings'),
      ),
      drawer: const AppDrawer(),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _buildSectionHeader('Appearance'),
          _buildThemeSelector(),
          const SizedBox(height: 8),
          _buildSwitchTile(
            'Dark Mode',
            'Enable dark theme',
            Icons.dark_mode,
            _darkModeEnabled,
            (value) => setState(() => _darkModeEnabled = value),
          ),
          
          const SizedBox(height: 24),
          _buildSectionHeader('Notifications'),
          _buildSwitchTile(
            'Push Notifications',
            'Receive reminders and updates',
            Icons.notifications,
            _notificationsEnabled,
            (value) => setState(() => _notificationsEnabled = value),
          ),
          
          const SizedBox(height: 24),
          _buildSectionHeader('Data & Storage'),
          _buildSwitchTile(
            'Offline Mode',
            'Cache data for offline access',
            Icons.cloud_off,
            _offlineModeEnabled,
            (value) => setState(() => _offlineModeEnabled = value),
          ),
          const SizedBox(height: 8),
          ListTile(
            leading: const Icon(Icons.storage),
            title: const Text('Clear Cache'),
            subtitle: const Text('Free up storage space'),
            onTap: _showClearCacheDialog,
          ),
          
          const SizedBox(height: 24),
          _buildSectionHeader('Account'),
          ListTile(
            leading: const Icon(Icons.person),
            title: const Text('Edit Profile'),
            subtitle: const Text('Update your information'),
            onTap: () => _showEditProfileDialog(),
          ),
          ListTile(
            leading: const Icon(Icons.security),
            title: const Text('Change Password'),
            subtitle: const Text('Update your password'),
            onTap: () => _showChangePasswordDialog(),
          ),
          ListTile(
            leading: const Icon(Icons.download),
            title: const Text('Export Data'),
            subtitle: const Text('Download your goals and progress'),
            onTap: _exportData,
          ),
          
          const SizedBox(height: 24),
          _buildSectionHeader('Support'),
          ListTile(
            leading: const Icon(Icons.help),
            title: const Text('Help & FAQ'),
            onTap: () => _showHelpDialog(),
          ),
          ListTile(
            leading: const Icon(Icons.feedback),
            title: const Text('Send Feedback'),
            onTap: () => _showFeedbackDialog(),
          ),
          ListTile(
            leading: const Icon(Icons.info),
            title: const Text('About'),
            onTap: () => _showAboutDialog(),
          ),
          
          const SizedBox(height: 32),
          ElevatedButton.icon(
            onPressed: _logout,
            icon: const Icon(Icons.logout),
            label: const Text('Sign Out'),
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.red,
              foregroundColor: Colors.white,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(
        title,
        style: Theme.of(context).textTheme.titleMedium?.copyWith(
          fontWeight: FontWeight.bold,
          color: Theme.of(context).primaryColor,
        ),
      ),
    );
  }

  Widget _buildSwitchTile(String title, String subtitle, IconData icon, 
                         bool value, ValueChanged<bool> onChanged) {
    return ListTile(
      leading: Icon(icon),
      title: Text(title),
      subtitle: Text(subtitle),
      trailing: Switch(
        value: value,
        onChanged: (newValue) {
          onChanged(newValue);
          _saveSettings();
        },
      ),
    );
  }

  Widget _buildThemeSelector() {
    return ListTile(
      leading: const Icon(Icons.palette),
      title: const Text('Theme'),
      subtitle: Text(_selectedTheme),
      onTap: () => _showThemeSelector(),
    );
  }

  void _showThemeSelector() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Select Theme'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: ['System', 'Light', 'Dark'].map((theme) => 
            RadioListTile<String>(
              title: Text(theme),
              value: theme,
              groupValue: _selectedTheme,
              onChanged: (value) {
                setState(() => _selectedTheme = value!);
                _saveSettings();
                Navigator.pop(context);
              },
            ),
          ).toList(),
        ),
      ),
    );
  }

  void _showClearCacheDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Clear Cache'),
        content: const Text('This will remove all offline data. Are you sure?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () async {
              await _storage.clearAll();
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Cache cleared successfully')),
              );
            },
            child: const Text('Clear'),
          ),
        ],
      ),
    );
  }

  void _showEditProfileDialog() {
    final userProvider = Provider.of<UserProvider>(context, listen: false);
    final user = userProvider.currentUser;
    
    if (user == null) return;

    final nameController = TextEditingController(text: user.name);
    final emailController = TextEditingController(text: user.email);

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Edit Profile'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: nameController,
              decoration: const InputDecoration(labelText: 'Name'),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: emailController,
              decoration: const InputDecoration(labelText: 'Email'),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Profile updated successfully')),
              );
            },
            child: const Text('Save'),
          ),
        ],
      ),
    );
  }

  void _showChangePasswordDialog() {
    final currentPasswordController = TextEditingController();
    final newPasswordController = TextEditingController();
    final confirmPasswordController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Change Password'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: currentPasswordController,
              decoration: const InputDecoration(labelText: 'Current Password'),
              obscureText: true,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: newPasswordController,
              decoration: const InputDecoration(labelText: 'New Password'),
              obscureText: true,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: confirmPasswordController,
              decoration: const InputDecoration(labelText: 'Confirm Password'),
              obscureText: true,
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              if (newPasswordController.text == confirmPasswordController.text) {
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Password changed successfully')),
                );
              } else {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Passwords do not match')),
                );
              }
            },
            child: const Text('Change'),
          ),
        ],
      ),
    );
  }

  void _exportData() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Export feature coming soon!')),
    );
  }

  void _showHelpDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Help & FAQ'),
        content: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              _buildHelpItem('How do I create a goal?', 
                'Tap the + button on the Goals screen and fill in the details.'),
              _buildHelpItem('How do streaks work?', 
                'Streaks track consecutive days of progress updates on your goals.'),
              _buildHelpItem('Can I use the app offline?', 
                'Yes! Enable offline mode in settings to cache data locally.'),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Close'),
          ),
        ],
      ),
    );
  }

  Widget _buildHelpItem(String question, String answer) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(question, style: const TextStyle(fontWeight: FontWeight.bold)),
          const SizedBox(height: 4),
          Text(answer),
        ],
      ),
    );
  }

  void _showFeedbackDialog() {
    final feedbackController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Send Feedback'),
        content: TextField(
          controller: feedbackController,
          decoration: const InputDecoration(
            hintText: 'Tell us what you think...',
            border: OutlineInputBorder(),
          ),
          maxLines: 5,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Feedback sent! Thank you.')),
              );
            },
            child: const Text('Send'),
          ),
        ],
      ),
    );
  }

  void _showAboutDialog() {
    showAboutDialog(
      context: context,
      applicationName: 'AchieveSync',
      applicationVersion: '1.0.0',
      applicationIcon: const Icon(Icons.track_changes, size: 64),
      children: [
        const Text('Your goal achievement companion'),
        const SizedBox(height: 16),
        const Text('Built with Flutter and powered by microservices architecture.'),
      ],
    );
  }

  void _logout() async {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Sign Out'),
        content: const Text('Are you sure you want to sign out?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () async {
              await _auth.logout();
              Navigator.pop(context);
              context.go('/');
            },
            child: const Text('Sign Out'),
          ),
        ],
      ),
    );
  }
}