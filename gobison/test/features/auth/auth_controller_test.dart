import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

import 'package:gobison/core/models/user.dart';
import 'package:gobison/features/auth/auth_controller.dart';
import 'package:gobison/features/auth/auth_service.dart';

class FakeAuthService extends AuthService {
  FakeAuthService()
    : super(
        client: MockClient((_) async {
          return http.Response('{}', 200);
        }),
      );

  Future<Map<String, dynamic>> Function()? onRegister;
  Future<Map<String, dynamic>> Function()? onLogin;

  @override
  Future<Map<String, dynamic>> register({
    required String firstName,
    String lastName = '',
    required String email,
    required String password,
  }) {
    return onRegister!();
  }

  @override
  Future<Map<String, dynamic>> login({
    required String email,
    required String password,
  }) {
    return onLogin!();
  }
}

void main() {
  group('AuthController', () {
    late FakeAuthService authService;
    late AuthController controller;

    setUp(() {
      authService = FakeAuthService();
      controller = AuthController(authService);
    });

    tearDown(() {
      controller.dispose();
    });

    test('register stores the user after success', () async {
      authService.onRegister = () async => {
        'userId': '123',
        'firstName': 'Rebekah',
        'lastName': 'Soneye',
        'email': 'rebekah123@myumanitoba.ca',
        'createdAt': '2026-10-18T12:00:00Z',
      };

      await controller.register(
        firstName: 'Rebekah',
        lastName: 'Soneye',
        email: 'rebekah123@myumanitoba.ca',
        password: 'password123',
      );

      expect(controller.registeredUser, isA<User>());
      expect(controller.errorMessage, isNull);
      expect(controller.isLoading, isFalse);
    });

    test('register displays an error when registration fails', () async {
      authService.onRegister = () async {
        throw Exception('Registration failed (409): Email already registered');
      };

      await controller.register(
        firstName: 'Rebekah',
        email: 'rebekah123@myumanitoba.ca',
        password: 'password123',
      );

      expect(controller.errorMessage, isNotNull);
      expect(controller.errorMessage, isNotEmpty);
      expect(controller.isLoading, isFalse);
    });

    test('login displays an error when login fails', () async {
      authService.onLogin = () async {
        throw Exception('Login failed (401): Invalid credentials');
      };

      await controller.login(
        email: 'rebekah123@myumanitoba.ca',
        password: 'wrongpassword',
      );

      expect(controller.errorMessage, isNotNull);
      expect(controller.errorMessage, isNotEmpty);
      expect(controller.isLoading, isFalse);
    });

    test('sets loading while registration is in progress', () async {
      final completer = Completer<Map<String, dynamic>>();

      authService.onRegister = () => completer.future;

      final future = controller.register(
        firstName: 'Rebekah',
        email: 'rebekah123@myumanitoba.ca',
        password: 'password123',
      );

      expect(controller.isLoading, isTrue);

      completer.complete({
        'userId': '123',
        'firstName': 'Rebekah',
        'lastName': 'Soneye',
        'email': 'rebekah123@myumanitoba.ca',
        'createdAt': '2026-10-18T12:00:00Z',
      });

      await future;

      expect(controller.isLoading, isFalse);
    });

    test('notifies listeners when registration starts and finishes', () async {
      var notificationCount = 0;
      controller.addListener(() {
        notificationCount++;
      });

      authService.onRegister = () async => {
        'userId': '123',
        'firstName': 'Rebekah',
        'lastName': 'Soneye',
        'email': 'rebekah123@myumanitoba.ca',
        'createdAt': '2026-10-18T12:00:00Z',
      };

      await controller.register(
        firstName: 'Rebekah',
        email: 'rebekah123@myumanitoba.ca',
        password: 'password123',
      );

      expect(notificationCount, 2);
    });
  });

  group('extractErrorCode', () {
    test('extracts supported HTTP status codes', () {
      expect(extractErrorCode(Exception('Request failed (400)')), 400);
      expect(extractErrorCode(Exception('Request failed (409)')), 409);
      expect(extractErrorCode(Exception('Request failed (429)')), 429);
      expect(extractErrorCode(Exception('Request failed (500)')), 500);
    });

    test('returns -1 for an unsupported status code', () {
      expect(extractErrorCode(Exception('Request failed (401)')), -1);
    });
  });
}
