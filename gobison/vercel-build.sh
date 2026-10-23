
#!/bin/bash
set -e

git clone --depth 1 --branch 3.47.7 https://github.com/flutter/flutter.git /tmp/flutter
export PATH="/tmp/flutter/bin:$PATH"

flutter config --enable-web
flutter pub get

flutter build web --release \
  --dart-define=API_BASE_URL="${API_BASE_URL:-https://example.invalid}"
