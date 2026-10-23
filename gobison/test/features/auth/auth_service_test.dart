import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

import 'package:gobison/core/network/api_config.dart';
import 'package:gobison/features/auth/auth_service.dart';

void main() {
  group('AuthService', () {
    group('register', () {
      test(
        'sends the correct request and returns successful response',
        () async {
          late http.Request capturedRequest;

          final client = MockClient((request) async {
            capturedRequest = request;

            return http.Response(
              jsonEncode({
                'userId': '123',
                'firstName': 'Rebekah',
                'lastName': 'Soneye',
                'email': 'rebekah123@myumanitoba.ca',
              }),
              201,
            );
          });

          final service = AuthService(client: client);

          final result = await service.register(
            firstName: 'Rebekah',
            lastName: 'Soneye',
            email: 'rebekah123@myumanitoba.ca',
            password: 'password123',
          );

          expect(capturedRequest.method, 'POST');
          expect(
            capturedRequest.url,
            Uri.parse('${ApiConfig.baseUrl}/api/v1/auth/register'),
          );
          expect(capturedRequest.headers['content-type'], 'application/json');
          expect(capturedRequest.headers['accept'], 'application/json');

          expect(jsonDecode(capturedRequest.body), {
            'firstName': 'Rebekah',
            'lastName': 'Soneye',
            'email': 'rebekah123@myumanitoba.ca',
            'password': 'password123',
          });

          expect(result['userId'], '123');
          expect(result['firstName'], 'Rebekah');
          expect(result['email'], 'rebekah123@myumanitoba.ca');
        },
      );

      test('accepts a 200 response', () async {
        final client = MockClient(
          (_) async => http.Response('{"success": true}', 200),
        );

        final service = AuthService(client: client);

        final result = await service.register(
          firstName: 'Rebekah',
          email: 'rebekah123',
          password: 'password123',
        );

        expect(result['success'], isTrue);
      });

      test('throws an exception when registration fails', () async {
        final client = MockClient(
          (_) async =>
              http.Response('{"message":"Email already registered"}', 409),
        );

        final service = AuthService(client: client);

        expect(
          () => service.register(
            firstName: 'Rebekah',
            email: 'rebekah123',
            password: 'password123',
          ),
          throwsA(
            isA<Exception>().having(
              (error) => error.toString(),
              'message',
              allOf(
                contains('Registration failed (409)'),
                contains('Email already registered'),
              ),
            ),
          ),
        );
      });
    });

    group('login', () {
      test(
        'sends the correct request and returns successful response',
        () async {
          late http.Request capturedRequest;

          final client = MockClient((request) async {
            capturedRequest = request;

            return http.Response(
              '{"userId":"123","email":"rebekah123@myumanitoba.ca"}',
              200,
            );
          });

          final service = AuthService(client: client);

          final result = await service.login(
            email: 'rebekah123@myumanitoba.ca',
            password: 'password123',
          );

          expect(capturedRequest.method, 'POST');
          expect(
            capturedRequest.url,
            Uri.parse('${ApiConfig.baseUrl}/api/v1/auth/login'),
          );

          expect(jsonDecode(capturedRequest.body), {
            'email': 'rebekah123@myumanitoba.ca',
            'password': 'password123',
          });

          expect(result['userId'], '123');
          expect(result['email'], 'rebekah123@myumanitoba.ca');
        },
      );

      test('throws an exception when login fails', () async {
        final client = MockClient(
          (_) async => http.Response('{"message":"Invalid credentials"}', 401),
        );

        final service = AuthService(client: client);

        expect(
          () => service.login(
            email: 'rebekah123@myumanitoba.ca',
            password: 'wrongpassword',
          ),
          throwsA(
            isA<Exception>().having(
              (error) => error.toString(),
              'message',
              allOf(
                contains('Login failed (401)'),
                contains('Invalid credentials'),
              ),
            ),
          ),
        );
      });
    });
  });
}
