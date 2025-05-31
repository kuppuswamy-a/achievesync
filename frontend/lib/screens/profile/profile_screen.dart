import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/user_provider.dart';
import '../../providers/goal_provider.dart';
import '../../widgets/app_drawer.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  @override
  void initState() {
    super.initState();
    _loadData();
  }

  void _loadData() {
    final userProvider = Provider.of<UserProvider>(context, listen: false);
    final goalProvider = Provider.of<GoalProvider>(context, listen: false);
    
    if (userProvider.currentUser != null) {
      userProvider.refreshConsistencyPoints();
      goalProvider.loadUserGoals(userProvider.currentUser!.userId);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Profile'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadData,
          ),
        ],
      ),
      drawer: const AppDrawer(),
      body: Consumer2<UserProvider, GoalProvider>(
        builder: (context, userProvider, goalProvider, child) {
          if (userProvider.currentUser == null) {
            return const Center(child: CircularProgressIndicator());
          }

          return RefreshIndicator(
            onRefresh: () async => _loadData(),
            child: SingleChildScrollView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  _buildProfileHeader(userProvider),
                  const SizedBox(height: 24),
                  _buildStatsGrid(userProvider, goalProvider),
                  const SizedBox(height: 24),
                  _buildAchievements(userProvider, goalProvider),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildProfileHeader(UserProvider userProvider) {
    final user = userProvider.currentUser!;
    
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: [
            CircleAvatar(
              radius: 50,
              backgroundColor: Theme.of(context).primaryColor,
              child: Text(
                user.name[0].toUpperCase(),
                style: const TextStyle(
                  fontSize: 36,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                ),
              ),
            ),
            const SizedBox(height: 16),
            Text(
              user.name,
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              user.email,
              style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                color: Colors.grey[600],
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Member since ${user.createdAt.toString().split(' ')[0]}',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: Colors.grey[500],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatsGrid(UserProvider userProvider, GoalProvider goalProvider) {
    return GridView.count(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      crossAxisCount: 2,
      crossAxisSpacing: 16,
      mainAxisSpacing: 16,
      children: [
        _buildStatCard(
          'Consistency Points',
          '${userProvider.consistencyPoints?.totalPoints ?? 0}',
          Icons.stars,
          Theme.of(context).primaryColor,
        ),
        _buildStatCard(
          'Total Goals',
          '${goalProvider.goals.length}',
          Icons.track_changes,
          Colors.blue,
        ),
        _buildStatCard(
          'Completed Goals',
          '${goalProvider.completedGoals.length}',
          Icons.check_circle,
          Colors.green,
        ),
        _buildStatCard(
          'Active Goals',
          '${goalProvider.activeGoals.length}',
          Icons.pending_actions,
          Colors.orange,
        ),
      ],
    );
  }

  Widget _buildStatCard(String title, String value, IconData icon, Color color) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 32,
              color: color,
            ),
            const SizedBox(height: 8),
            Text(
              value,
              style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                fontWeight: FontWeight.bold,
                color: color,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              title,
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: Colors.grey[600],
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAchievements(UserProvider userProvider, GoalProvider goalProvider) {
    final completedGoals = goalProvider.completedGoals.length;
    final totalPoints = userProvider.consistencyPoints?.totalPoints ?? 0;
    
    final achievements = <Map<String, dynamic>>[];
    
    // Goal-based achievements
    if (completedGoals >= 1) {
      achievements.add({
        'title': 'First Goal Achiever',
        'description': 'Completed your first goal',
        'icon': Icons.flag,
        'color': Colors.green,
        'unlocked': true,
      });
    }
    
    if (completedGoals >= 5) {
      achievements.add({
        'title': 'Goal Crusher',
        'description': 'Completed 5 goals',
        'icon': Icons.military_tech,
        'color': Colors.blue,
        'unlocked': true,
      });
    }
    
    if (completedGoals >= 10) {
      achievements.add({
        'title': 'Achievement Master',
        'description': 'Completed 10 goals',
        'icon': Icons.emoji_events,
        'color': Colors.orange,
        'unlocked': true,
      });
    }
    
    // Points-based achievements
    if (totalPoints >= 100) {
      achievements.add({
        'title': 'Point Collector',
        'description': 'Earned 100 consistency points',
        'icon': Icons.stars,
        'color': Colors.purple,
        'unlocked': true,
      });
    }
    
    if (totalPoints >= 500) {
      achievements.add({
        'title': 'Consistency King',
        'description': 'Earned 500 consistency points',
        'icon': Icons.crown,
        'color': Colors.amber,
        'unlocked': true,
      });
    }

    // Add locked achievements
    if (completedGoals < 1) {
      achievements.add({
        'title': 'First Goal Achiever',
        'description': 'Complete your first goal',
        'icon': Icons.flag,
        'color': Colors.grey,
        'unlocked': false,
      });
    }
    
    if (completedGoals < 5) {
      achievements.add({
        'title': 'Goal Crusher',
        'description': 'Complete 5 goals',
        'icon': Icons.military_tech,
        'color': Colors.grey,
        'unlocked': false,
      });
    }

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Achievements',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            if (achievements.isEmpty)
              const Center(
                child: Padding(
                  padding: EdgeInsets.all(24),
                  child: Text(
                    'Complete goals to unlock achievements!',
                    style: TextStyle(color: Colors.grey),
                  ),
                ),
              )
            else
              ...achievements.map((achievement) => ListTile(
                leading: CircleAvatar(
                  backgroundColor: achievement['unlocked'] 
                      ? achievement['color'] 
                      : Colors.grey[300],
                  child: Icon(
                    achievement['icon'],
                    color: achievement['unlocked'] 
                        ? Colors.white 
                        : Colors.grey[600],
                  ),
                ),
                title: Text(
                  achievement['title'],
                  style: TextStyle(
                    fontWeight: FontWeight.w600,
                    color: achievement['unlocked'] 
                        ? null 
                        : Colors.grey[600],
                  ),
                ),
                subtitle: Text(
                  achievement['description'],
                  style: TextStyle(
                    color: achievement['unlocked'] 
                        ? Colors.grey[600] 
                        : Colors.grey[500],
                  ),
                ),
                trailing: achievement['unlocked'] 
                    ? const Icon(Icons.check_circle, color: Colors.green)
                    : const Icon(Icons.lock, color: Colors.grey),
              )),
          ],
        ),
      ),
    );
  }
}