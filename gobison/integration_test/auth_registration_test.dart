import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:gobison/main.dart' as app;

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('App opens the registration page', (tester) async {
    app.main();
    await tester.pumpAndSettle();

    expect(find.text('Join GoBison'), findsOneWidget);
    expect(find.text('Create Account'), findsOneWidget);
  });

  testWidgets('Empty registration form shows required errors', (tester) async {
    app.main();
    await tester.pumpAndSettle();

    final createAccountButton = find.widgetWithText(
      ElevatedButton,
      'Create Account',
    );

    await tester.ensureVisible(createAccountButton);
    await tester.pumpAndSettle();
    await tester.tap(createAccountButton);
    await tester.pumpAndSettle();

    final errors = tester
        .widgetList<InputDecorator>(find.byType(InputDecorator))
        .map((field) => field.decoration.errorText)
        .whereType<String>()
        .toList();

    expect(
      errors,
      unorderedEquals([
        'This field is required',
        'Please enter your UMNetId',
        'Please enter a password',
      ]),
    );
  });
}
