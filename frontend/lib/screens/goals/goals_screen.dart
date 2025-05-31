import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import '../../providers/user_provider.dart';
import '../../providers/goal_provider.dart';
import '../../widgets/app_drawer.dart';

class GoalsScreen extends StatefulWidget {
  const GoalsScreen({super.key});

  @override
  State<GoalsScreen> createState() => _GoalsScreenState();
}

class _GoalsScreenState extends State<GoalsScreen> with TickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _loadGoals();
  }

  void _loadGoals() {
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
        title: const Text('Goals'),
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(text: 'Active'),
            Tab(text: 'Completed'),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadGoals,
          ),
        ],
      ),
      drawer: const AppDrawer(),
      body: Consumer<GoalProvider>(
        builder: (context, goalProvider, child) {
          if (goalProvider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          return TabBarView(
            controller: _tabController,
            children: [
              _buildGoalsList(goalProvider.activeGoals, false),
              _buildGoalsList(goalProvider.completedGoals, true),
            ],
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _showCreateGoalDialog,
        child: const Icon(Icons.add),
      ),
    );
  }

  Widget _buildGoalsList(List goals, bool isCompleted) {
    if (goals.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              isCompleted ? Icons.check_circle_outline : Icons.track_changes_outlined,
              size: 64,
              color: Colors.grey[400],
            ),
            const SizedBox(height: 16),
            Text(
              isCompleted ? 'No completed goals yet' : 'No active goals',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                color: Colors.grey[600],
              ),
            ),
            const SizedBox(height: 8),
            Text(
              isCompleted 
                  ? 'Complete some goals to see them here'
                  : 'Create your first goal to get started!',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: Colors.grey[500],
              ),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: () async => _loadGoals(),
      child: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: goals.length,
        itemBuilder: (context, index) {
          final goal = goals[index];
          return Card(
            margin: const EdgeInsets.only(bottom: 12),
            child: ListTile(
              contentPadding: const EdgeInsets.all(16),
              leading: Stack(
                alignment: Alignment.center,
                children: [
                  SizedBox(
                    width: 48,
                    height: 48,
                    child: CircularProgressIndicator(
                      value: goal.progressPercentage / 100,
                      backgroundColor: Colors.grey[300],
                      strokeWidth: 4,
                    ),
                  ),
                  Text(
                    '${goal.progressPercentage.toInt()}%',
                    style: const TextStyle(
                      fontSize: 10,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
              title: Text(
                goal.description,
                style: const TextStyle(fontWeight: FontWeight.w600),
              ),
              subtitle: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SizedBox(height: 4),
                  Text('Target: ${goal.targetDate.toString().split(' ')[0]}'),
                  const SizedBox(height: 4),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                    decoration: BoxDecoration(
                      color: _getStatusColor(goal.status).withOpacity(0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      _getStatusText(goal.status),
                      style: TextStyle(
                        color: _getStatusColor(goal.status),
                        fontSize: 12,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                ],
              ),
              trailing: const Icon(Icons.arrow_forward_ios, size: 16),
              onTap: () => context.go('/goals/${goal.goalId}'),
            ),
          );
        },
      ),
    );
  }

  Color _getStatusColor(status) {
    switch (status.toString()) {
      case 'GoalStatus.completed':
        return Colors.green;
      case 'GoalStatus.inProgress':
        return Colors.blue;
      default:
        return Colors.orange;
    }
  }

  String _getStatusText(status) {
    switch (status.toString()) {
      case 'GoalStatus.completed':
        return 'Completed';
      case 'GoalStatus.inProgress':
        return 'In Progress';
      default:
        return 'Pending';
    }
  }

  void _showCreateGoalDialog() {
    final descriptionController = TextEditingController();
    DateTime targetDate = DateTime.now().add(const Duration(days: 30));

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Create New Goal'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: descriptionController,
              decoration: const InputDecoration(
                labelText: 'Goal Description',
                border: OutlineInputBorder(),
              ),
              maxLines: 3,
            ),
            const SizedBox(height: 16),
            ListTile(
              title: const Text('Target Date'),
              subtitle: Text(targetDate.toString().split(' ')[0]),
              trailing: const Icon(Icons.calendar_today),
              onTap: () async {
                final picked = await showDatePicker(
                  context: context,
                  initialDate: targetDate,
                  firstDate: DateTime.now(),
                  lastDate: DateTime.now().add(const Duration(days: 365)),
                );
                if (picked != null) {
                  setState(() {
                    targetDate = picked;
                  });
                }
              },
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () async {
              if (descriptionController.text.isNotEmpty) {
                final userProvider = Provider.of<UserProvider>(context, listen: false);
                final goalProvider = Provider.of<GoalProvider>(context, listen: false);
                
                await goalProvider.createGoal(
                  userProvider.currentUser!.userId,
                  descriptionController.text,
                  targetDate,
                );
                
                if (mounted) {
                  Navigator.pop(context);
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Goal created successfully!')),
                  );
                }
              }
            },
            child: const Text('Create'),
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }
}