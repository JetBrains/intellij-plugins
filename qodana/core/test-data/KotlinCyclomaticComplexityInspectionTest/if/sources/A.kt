// Complexity[ifStatement]: 1 + 2 (if) = 3
fun ifStatement(a: Int, b: Int): Boolean {
  if (a + b > 10) {
    return true
  }
  else if (a < b) {
    return true
  } else {
    return false
  }
}