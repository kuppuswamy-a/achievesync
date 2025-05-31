import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import '../../providers/user_provider.dart';
import '../../providers/goal_provider.dart';
import '../../widgets/app_drawer.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  @override
  void initState() {
    super.initState();
    _loadData();
  }

  void _loadData() {
    final userProvider = Provider.of<UserProvider>(context, listen: false);
    final goalProvider = Provider.of<GoalProvider>(context, listen: false);
    
    if (userProvider.currentUser != null) {
      goalProvider.loadUserGoals(userProvider.currentUser!.userId);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Dashboard'),
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
            return const Center(
              child: CircularProgressIndicator(),
            );
          }

          return RefreshIndicator(
            onRefresh: () async {
              _loadData();
              await userProvider.refreshConsistencyPoints();
            },
            child: SingleChildScrollView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildWelcomeCard(userProvider),
                  const SizedBox(height: 16),
                  _buildStatsCards(userProvider, goalProvider),
                  const SizedBox(height: 16),
                  _buildRecentGoals(goalProvider),
                ],
              ),
            ),
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => context.go('/goals'),
        child: const Icon(Icons.add),
      ),
    );
  }

  Widget _buildWelcomeCard(UserProvider userProvider) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            CircleAvatar(
              radius: 30,
              backgroundColor: Theme.of(context).primaryColor,
              child: Text(
                userProvider.currentUser!.name[0].toUpperCase(),
                style: const TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                ),
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Welcome back, ${userProvider.currentUser!.name}!',
                    style: Theme.of(context).textTheme.headlineSmall,
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Keep up the great work on your goals',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: Colors.grey[600],
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatsCards(UserProvider userProvider, GoalProvider goalProvider) {
    return Row(
      children: [
        Expanded(
          child: Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  Icon(
                    Icons.stars,
                    size: 32,
                    color: Theme.of(context).primaryColor,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    '${userProvider.consistencyPoints?.totalPoints ?? 0}',
                    style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  Text(
                    'Consistency Points',
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                ],
              ),
            ),
          ),
        ),
        const SizedBox(width: 16),
        Expanded(
          child: Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  Icon(
                    Icons.track_changes,
                    size: 32,
                    color: Colors.green,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    '${goalProvider.activeGoals.length}',
                    style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  Text(
                    'Active Goals',
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                ],
              ),
            ),
          ),
        ),
        const SizedBox(width: 16),
        Expanded(
          child: Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  Icon(
                    Icons.check_circle,
                    size: 32,
                    color: Colors.orange,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    '${goalProvider.completedGoals.length}',
                    style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  Text(
                    'Completed',
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildRecentGoals(GoalProvider goalProvider) {
    final recentGoals = goalProvider.goals.take(3).toList();
    
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'Recent Goals',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            TextButton(
              onPressed: () => context.go('/goals'),
              child: const Text('See All'),
            ),
          ],
        ),
        const SizedBox(height: 8),
        if (recentGoals.isEmpty)
          Card(
            child: Padding(
              padding: const EdgeInsets.all(32),
              child: Center(
                child: Column(
                  children: [
                    Icon(
                      Icons.track_changes,
                      size: 48,
                      color: Colors.grey[400],
                    ),
                    const SizedBox(height: 16),
                    Text(
                      'No goals yet',
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Create your first goal to get started!',
                      style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                        color: Colors.grey[600],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          )
        else
          ...recentGoals.map((goal) => Card(
            margin: const EdgeInsets.only(bottom: 8),
            child: ListTile(
              leading: CircularProgressIndicator(
                value: goal.progressPercentage / 100,
                backgroundColor: Colors.grey[300],
              ),
              title: Text(goal.description),
              subtitle: Text(
                'Target: ${goal.targetDate.toString().split(' ')[0]}',
              ),
              trailing: Text(
                '${goal.progressPercentage.toInt()}%',
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
              onTap: () => context.go('/goals/${goal.goalId}'),
            ),
          )),
      ],
    );
  }
}