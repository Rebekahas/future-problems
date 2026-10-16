class AuthValidators {
  static String? validateRequired(String? value) => _validateRequired(value);
  static String? validateEmail(String? value) => _validateEmail(value);
  static String? validatePassword(String? value) => _validatePassword(value);
}

String? _validateRequired(String? value) {
  if (value == null || value.trim().isEmpty) {
    return 'This field is required';
  }
  return null;
}

String? _validateEmail(String? value) {
  if (value == null || value.trim().isEmpty) {
    return 'Please enter your UMNetId';
  }

  final UMNetId = value.trim();

  if (UMNetId.contains('@')) {
    return 'Enter the UMNetId part of your email only, without @ or a domain';
  }

  return null;
}

String? _validatePassword(String? value) {
  if (value == null || value.isEmpty) {
    return 'Please enter a password';
  }

  if (value.length < 8) {
    return 'Password must be at least 8 characters';
  }

  return null;
}
