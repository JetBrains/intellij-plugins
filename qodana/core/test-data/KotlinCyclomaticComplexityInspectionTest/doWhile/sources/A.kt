// Complexity[doWhileLoop]: 1 + 1 (do while) + 1 (do while) + 1 (if) = 4
fun doWhileLoop() {
  var j = 0
  do {
    if (j % 2 == 0) {
      print("$j ")
    }
    j += 1

    do {
      j--
    } while (j > 10)

  } while (true)
}