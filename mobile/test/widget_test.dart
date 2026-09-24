import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/config/app_config.dart';
import 'package:mobile/main.dart';

void main() {
  testWidgets('DigitalLibraryApp launches and renders LoginScreen with brand elements', (WidgetTester tester) async {
    await tester.pumpWidget(
      const ProviderScope(
        child: DigitalLibraryApp(),
      ),
    );

    // Initial pump & frame
    await tester.pumpAndSettle();

    // Verify brand title and login elements
    expect(find.text(AppConfig.appName), findsOneWidget);
    expect(find.text('Digital Library Management System for Sanjay Library'), findsOneWidget);
    expect(find.text('Sign In to Library'), findsOneWidget);
    expect(find.text('Demo Quick Login (Preset Accounts)'), findsOneWidget);
    expect(find.text('Admin (Owner)'), findsOneWidget);
    expect(find.text('Student User'), findsOneWidget);
  });
}
