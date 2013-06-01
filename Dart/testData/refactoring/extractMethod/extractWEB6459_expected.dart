class A  {
  int extracted(String digits, int i) {
    int digit = digits.charCodeAt(i) - '0'.charCodeAt(0);
    return digit;
  }

  int getDigit(String digits, int i) {
    var digit = extracted(digits, i);
    return digit;
  }
}