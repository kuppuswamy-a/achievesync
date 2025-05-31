import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/goal_provider.dart';
import '../../models/goal.dart';

class GoalDetailScreen extends StatefulWidget {
  final String goalId;

  const GoalDetailScreen({super.key, required this.goalId});

  @override
  State<GoalDetailScreen> createState() => _GoalDetailScreenState();
}

class _GoalDetailScreenState extends State<GoalDetailScreen> {
  @override
  void initState() {
    super.initState();
    _loadGoal();
  }

  void _loadGoal() {
    Provider.of<GoalProvider>(context, listen: false).loadGoal(widget.goalId);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Goal Details'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadGoal,
          ),
        ],
      ),
      body: Consumer<GoalProvider>(
        builder: (context, goalProvider, child) {
          if (goalProvider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          final goal = goalProvider.selectedGoal;
          if (goal == null) {
            return const Center(
              child: Text('Goal not found'),
            );
          }

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildGoalHeader(goal),
                const SizedBox(height: 24),
                _buildProgressSection(goal),
                const SizedBox(height: 24),
                _buildActionButtons(goal),
                const SizedBox(height: 24),
                _buildGoalDetails(goal),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildGoalHeader(Goal goal) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    goal.description,
                    style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  decoration: BoxDecoration(
                    color: _getStatusColor(goal.status).withOpacity(0.1),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Text(
                    _getStatusText(goal.status),
                    style: TextStyle(
                      color: _getStatusColor(goal.status),
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Icon(Icons.calendar_today, size: 16, color: Colors.grey[600]),
                const SizedBox(width: 8),
                Text(
                  'Target: ${goal.targetDate.toString().split(' ')[0]}',
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: Colors.grey[600],
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildProgressSection(Goal goal) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Progress',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            Center(
              child: SizedBox(
                width: 120,
                height: 120,
                child: Stack(
                  alignment: Alignment.center,
                  children: [
                    SizedBox(
                      width: 120,
                      height: 120,
                      child: CircularProgressIndicator(
                        value: goal.progressPercentage / 100,
                        backgroundColor: Colors.grey[300],
                        strokeWidth: 8,
                      ),
                    ),
                    Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          '${goal.progressPercentage.toInt()}%',
                          style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        Text(
                          'Complete',
                          style: Theme.of(context).textTheme.bodySmall,
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            LinearProgressIndicator(
              value: goal.progressPercentage / 100,
              backgroundColor: Colors.grey[300],
              minHeight: 8,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildActionButtons(Goal goal) {
    if (goal.status == GoalStatus.completed) {
      return const SizedBox.shrink();
    }

    return Column(
      children: [
        SizedBox(
          width: double.infinity,
          child: ElevatedButton.icon(
            onPressed: () => _showProgressUpdateDialog(goal),
            icon: const Icon(Icons.trending_up),
            label: const Text('Update Progress'),
          ),
        ),
        const SizedBox(height: 8),
        if (goal.progressPercentage < 100)
          SizedBox(
            width: double.infinity,
            child: OutlinedButton.icon(
              onPressed: () => _completeGoal(goal),
              icon: const Icon(Icons.check_circle),
              label: const Text('Mark as Complete'),
            ),
          ),
      ],
    );
  }

  Widget _buildGoalDetails(Goal goal) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Details',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            _buildDetailRow('Created', goal.createdAt.toString().split(' ')[0]),
            _buildDetailRow('Last Updated', goal.updatedAt.toString().split(' ')[0]),
            _buildDetailRow('Status', _getStatusText(goal.status)),
            _buildDetailRow('Progress', '${goal.progressPercentage.toInt()}%'),
          ],
        ),
      ),
    );
  }

  Widget _buildDetailRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 100,
            child: Text(
              label,
              style: TextStyle(
                fontWeight: FontWeight.w500,
                color: Colors.grey[600],
              ),
            ),
          ),
          Expanded(
            child: Text(value),
          ),
        ],
      ),
    );
  }

  Color _getStatusColor(GoalStatus status) {
    switch (status) {
      case GoalStatus.completed:
        return Colors.green;
      case GoalStatus.inProgress:
        return Colors.blue;
      default:
        return Colors.orange;
    }
  }

  String _getStatusText(GoalStatus status) {
    switch (status) {
      case GoalStatus.completed:
        return 'Completed';
      case GoalStatus.inProgress:
        return 'In Progress';
      default:
        return 'Pending';
    }
  }

  void _showProgressUpdateDialog(Goal goal) {
    final progressController = TextEditingController(
      text: goal.progressPercentage.toString(),
    );
    final notesController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Update Progress'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: progressController,
              decoration: const InputDecoration(
                labelText: 'Progress Percentage (0-100)',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.number,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: notesController,
              decoration: const InputDecoration(
                labelText: 'Notes (optional)',
                border: OutlineInputBorder(),
              ),
              maxLines: 3,
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
              final progress = double.tryParse(progressController.text);
              if (progress != null && progress >= 0 && progress <= 100) {
                final goalProvider = Provider.of<GoalProvider>(context, listen: false);
                final success = await goalProvider.updateGoalProgress(
                  goal.goalId,
                  progress,
                  notesController.text.isEmpty ? null : notesController.text,
                );
                
                if (mounted) {
                  Navigator.pop(context);
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Text(success 
                          ? 'Progress updated successfully!'
                          : 'Failed to update progress'),
                    ),
                  );
                }
              }
            },
            child: const Text('Update'),
          ),
        ],
      ),
    );
  }

  void _completeGoal(Goal goal) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Complete Goal'),
        content: const Text('Are you sure you want to mark this goal as complete?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () async {
              final goalProvider = Provider.of<GoalProvider>(context, listen: false);
              final success = await goalProvider.completeGoal(goal.goalId);
              
              if (mounted) {
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: Text(success 
                        ? 'Goal completed! 🎉'
                        : 'Failed to complete goal'),
                  ),
                );
              }
            },
            child: const Text('Complete'),
          ),
        ],
      ),
    );
  }
}