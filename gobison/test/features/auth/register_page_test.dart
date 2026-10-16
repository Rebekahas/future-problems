import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gobison/features/auth/presentation/register_page.dart';

void main() {
  Widget createTestWidget() {
    return const MaterialApp(home: RegisterPage());
  }

  Future<void> submitForm(WidgetTester tester) async {
    await tester.ensureVisible(find.text('Create Account'));
    await tester.tap(find.text('Create Account'));
    await tester.pumpAndSettle();
  }

  Future<void> fillRegistrationForm(
    WidgetTester tester, {
    String firstName = 'Rebekah',
    String lastName = 'Soneye',
    String email = 'rebekah123',
    String password = 'password123',
  }) async {
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Enter your first name'),
      firstName,
    );
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Enter your last name'),
      lastName,
    );
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Enter your UMNetID'),
      email,
    );
    await tester.enterText(
      find.widgetWithText(TextFormField, 'At least 8 characters'),
      password,
    );
  }

  group('RegisterPage validation', () {
    testWidgets('shows required-field errors when submitted empty', (
      WidgetTester tester,
    ) async {
      await tester.pumpWidget(createTestWidget());

      await submitForm(tester);

      expect(find.text('This field is required'), findsNWidgets(2));
      expect(find.text('Please enter your UMNetId'), findsOneWidget);
      expect(find.text('Please enter a password'), findsOneWidget);
    });

    testWidgets('rejects a UMNetID containing @ or a domain', (
      WidgetTester tester,
    ) async {
      await tester.pumpWidget(createTestWidget());

      await fillRegistrationForm(tester, email: 'rebekah@myumanitoba.ca');

      await submitForm(tester);

      expect(
        find.text(
          'Enter the UMNetId part of your email only, without @ or a domain',
        ),
        findsOneWidget,
      );
    });

    testWidgets('rejects a password shorter than 8 characters', (
      WidgetTester tester,
    ) async {
      await tester.pumpWidget(createTestWidget());

      await fillRegistrationForm(tester, password: 'short');

      await submitForm(tester);

      expect(
        find.text('Password must be at least 8 characters'),
        findsOneWidget,
      );
    });

    testWidgets('accepts valid registration field values', (
      WidgetTester tester,
    ) async {
      await tester.pumpWidget(createTestWidget());

      await fillRegistrationForm(tester);

      await submitForm(tester);

      expect(find.text('This field is required'), findsNothing);
      expect(find.text('Please enter your email'), findsNothing);
      expect(find.text('Please enter a password'), findsNothing);
      expect(
        find.text(
          'Enter the UMNetId part of your email only, without @ or a domain',
        ),
        findsNothing,
      );
      expect(find.text('Password must be at least 8 characters'), findsNothing);
    });
  });
}
