// Complexity[whileStatement]: 1 + 2 (while) = 3
fun whileStatement(a: Int, b: Int) {
  while (a < 10) {
    print("$a")

    while (b < 10) {
      print("$b")
    }
  }
}