// Total complexity: 2 + 1 + 3 + 2 = 8

// Complexity[foo]: 1 + 1 (for) = 2
fun foo() {
  for (i in 1..10) {
    println(something())
  }

  // Complexity[lambda]: 1
  val array = Array(10) { it }

  // Complexity[lambda]: 1 + 2 (if) = 3
  array.forEach {
    if (it % 6 == 0) {
      println(it)
    }
    if (it % 3 == 0) {
      println(it)
    }
  }

  // Complexity[lambda]: 1 + 1 (if) = 2
  array.forEach {
    if (it % 6 == 0) {
      println(it)
    }
  }
}