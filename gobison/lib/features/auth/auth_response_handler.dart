class AuthResponseHandler {
  static String handleLoginError(int statusCode) {
    switch (statusCode) {
      case 400:
        return 'Please enter a valid email and password.';
      case 401:
        return 'Invalid email or password.';
      case 429:
        return 'Too many login attempts. Please try again later.';
      case 500:
        return 'It\'s not you, it\'s us. Please try again later.';
      default:
        return 'An unexpected error occurred. Please try again.';
    }
  }

  static String handleRegistrationError(int statusCode) {
    switch (statusCode) {
      case 400:
        return 'Please check your information and try again.';
      case 409:
        return 'An account with this email already exists.';
      case 429:
        return 'Too many registration attempts. Please try again later.';
      case 500:
        return 'It\'s not you, it\'s us. Please try again later.';
      default:
        return 'Unable to register. Please try again.';
    }
  }
}
