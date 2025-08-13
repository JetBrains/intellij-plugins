// Total complexity: 2 + 4 + 4 = 10

// Complexity[something]: 1 + 1 (for) = 2
fun something() {
  // Complexity[nested1]: 1 + 1 (if) + 2 (||) = 4
  fun nested1(b: Int): Boolean {
    // Complexity[nested2]: 1 + 1 (if) + 1 (||) + 1 (for) = 4
    fun nested2(b: Int): Boolean {
      val a = 5
      if (a + b > 10 || b % 6 == 0) {
        return true
      }
      for (i in 1..10) {
        println(nested1(i))
      }
      return false
    }

    val a = 5
    if (a + b > 10 || b % 6 == 0 || b % 3 == 0) {
      return true
    }
    return false
  }

  for (i in 1..10) {
    println(nested1(i))
  }
}