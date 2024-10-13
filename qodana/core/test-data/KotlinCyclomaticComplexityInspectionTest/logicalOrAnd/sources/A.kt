// Complexity[logicalAndOr]: 1 + 2 (if) + 1 (||) + 2 (&&) = 6
fun logicalAndOr(a: Int, b: Int): Boolean {
  if (a + b > 10 || b % 6 == 0 && a % 3 == 0) {
    return true
  }
  else if (a < b && a > 10) {
    return true
  } else {
    return false
  }
}