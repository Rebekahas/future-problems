import 'dart:async';

import 'package:flutter/material.dart';

import '../../core/models/user.dart';

import 'auth_service.dart';
import 'auth_response_handler.dart';

class AuthController extends ChangeNotifier {
  AuthController(this._authService);

  final AuthService _authService;

  bool isLoading = false;
  String? errorMessage;
  User? registeredUser;

  Future<void> register({
    required String firstName,
    required String email,
    required String password,
    String lastName = '',
  }) async {
    isLoading = true;
    errorMessage = null;
    notifyListeners();

    await Future.delayed(
      const Duration(seconds: 5),
    ); // TODO: Remove this line after backend connection (mimicks loading)

    try {
      final data = await _authService.register(
        firstName: firstName,
        lastName: lastName,
        email: email,
        password: password,
      );

      registeredUser = User.fromJson(data);
    } catch (e) {
      errorMessage = AuthResponseHandler.handleRegistrationError(
        extractErrorCode(e),
      );
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  Future<void> login({required String email, required String password}) async {
    isLoading = true;
    errorMessage = null;
    notifyListeners();

    await Future.delayed(
      const Duration(seconds: 5),
    ); // TODO: Remove after backend connection

    try {
      await _authService.login(email: email, password: password);
    } catch (e) {
      errorMessage = AuthResponseHandler.handleLoginError(extractErrorCode(e));
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }
}

int extractErrorCode(Object e) {
  final message = e.toString();
  if (message.contains('400')) {
    return 400;
  }
  if (message.contains('409')) {
    return 409;
  }
  if (message.contains('429')) {
    return 429;
  }
  if (message.contains('500')) {
    return 500;
  }
  return -1;
}
