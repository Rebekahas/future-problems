import 'dart:convert';
import 'dart:io';

Future<void> main(List<String> args) async {
  final target = args.isEmpty
      ? 'integration_test/auth_registration_response_test.dart'
      : args.first;

  final server = await HttpServer.bind(InternetAddress.loopbackIPv4, 3000);

  final responseCodes = <String, int>{
    'duplicate@myumanitoba.ca': 409,
    'servererror@myumanitoba.ca': 500,
  };

  server.listen((request) async {
    final response = request.response;

    response.headers.set('Access-Control-Allow-Origin', '*');
    response.headers.set('Access-Control-Allow-Methods', 'POST, OPTIONS');
    response.headers.set('Access-Control-Allow-Headers', 'Content-Type');
    response.headers.contentType = ContentType.json;

    if (request.method == 'OPTIONS') {
      response.statusCode = HttpStatus.noContent;
      await response.close();
      return;
    }

    if (request.method == 'POST' &&
        request.uri.path == '/api/v1/auth/register') {
      try {
        final body = await utf8.decoder.bind(request).join();
        final data = jsonDecode(body) as Map<String, dynamic>;

        response.statusCode =
            responseCodes[data['email']] ?? HttpStatus.badRequest;
        response.write(jsonEncode({'message': 'Mock registration response'}));
      } catch (_) {
        response.statusCode = HttpStatus.badRequest;
        response.write(jsonEncode({'message': 'Invalid request'}));
      }
    } else {
      response.statusCode = HttpStatus.notFound;
      response.write(jsonEncode({'message': 'Unknown endpoint'}));
    }

    await response.close();
  });

  stdout.writeln('Mock backend started at http://localhost:3000');

  try {
    final process = await Process.start(
      'flutter',
      [
        'drive',
        '--driver=test_driver/integration_test.dart',
        '--target=$target',
        '-d',
        'web-server',
        '--browser-name=chrome',
        '--driver-port=4444',
        '--no-headless',
      ],
      runInShell: Platform.isWindows,
      mode: ProcessStartMode.inheritStdio,
    );

    exitCode = await process.exitCode;
  } finally {
    await server.close(force: true);
    stdout.writeln('Mock backend stopped.');
  }
}
