import '../models/user.dart';
import '../models/user_profile.dart';

class SessionData {
  final User user;
  final UserProfile profile;

  const SessionData({required this.user, required this.profile});
}

class SessionService {
  Future<SessionData?> loadCurrentSession() async {
    return const SessionData(
      user: User(
        id: 'usr_123',
        firstName: 'Rebekah',
        lastName: 'Soneye',
        email: 'rebekah@example.com',
      ),
      profile: UserProfile(
        userId: 'usr_123',
        firstName: 'Rebekah',
        lastName: 'Soneye',
        age: 22,
        gender: 'Female',
        description: 'My car is quite messy, but I\'ll get you to campus safe!',
      ),
    );

    //TODO: Implement the actual session loading logic here.
    //The current implementation is a placeholder that returns a hardcoded SessionData object.
  }
}
