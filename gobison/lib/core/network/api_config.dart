import 'package:flutter/foundation.dart';

class ApiConfig {
  static String get baseUrl {
    if (kIsWeb) {
      final host = Uri.base.host;

      if (host == 'localhost' || host == '127.0.0.1') {
        return 'http://localhost:3000';
      }
    }

    return 'https://your-production-server.com';
  }
}
