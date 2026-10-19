import 'package:flutter/material.dart';

import 'features/auth/presentation/register_page.dart';
import 'features/auth/presentation/login_page.dart';
import 'features/home/presentation/home_page.dart';
import 'features/profile/presentation/display_profile_page.dart';
import 'features/profile/presentation/edit_profile_page.dart';

import 'core/models/user.dart';
import 'core/models/user_profile.dart';
import 'core/services/session_service.dart';

void main() {
  runApp(const GoBisonApp());
}

class GoBisonApp extends StatelessWidget {
  const GoBisonApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'GoBison',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF654321)),
        useMaterial3: true,
      ),

      home: const SessionGate(),
      routes: {
        '/register': (_) => const RegisterPage(),
        '/login': (_) => const LoginPage(),
      },
    );
  }
}

class SessionGate extends StatefulWidget {
  const SessionGate({super.key});

  @override
  State<SessionGate> createState() => _SessionGateState();
}

class _SessionGateState extends State<SessionGate> {
  late Future<SessionData?> _sessionFuture;

  @override
  void initState() {
    super.initState();
    _sessionFuture = SessionService().loadCurrentSession();
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<SessionData?>(
      future: _sessionFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Scaffold(
            body: Center(child: CircularProgressIndicator()),
          );
        }

        if (snapshot.hasError) {
          return Scaffold(
            body: Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Text('Unable to load your account.'),
                  const SizedBox(height: 12),
                  ElevatedButton(
                    onPressed: () {
                      setState(() {
                        _sessionFuture = SessionService().loadCurrentSession();
                      });
                    },
                    child: const Text('Try again'),
                  ),
                ],
              ),
            ),
          );
        }

        final session = snapshot.data;

        if (session == null) {
          return const LoginPage();
        }

        return HomePage(
          user: session.user,
          profile: session.profile,
          onProfileTap: () {
            Navigator.of(context).push(
              MaterialPageRoute(
                builder: (_) => DisplayProfilePage(
                  user: session.user,
                  profile: session.profile,
                  currentUserId: session.user.id,
                  onEdit: () {
                    Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (_) => EditProfilePage(
                          user: session.user,
                          profile: session.profile,
                        ),
                      ),
                    );
                  },
                ),
              ),
            );
          },
        );
      },
    );
  }
}
