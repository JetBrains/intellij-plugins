class Bar {
  bool flag = false;
  Bar() {
  }

  extracted(bool flag) {
    if (flag) {
      return 'true';
    }
    return 'false';
  }

  String testMe(bool flag) {
    extracted(flag);
  }
}