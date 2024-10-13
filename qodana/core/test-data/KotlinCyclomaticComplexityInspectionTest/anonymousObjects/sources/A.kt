// Total complexity: 2 + 4 = 6

interface Something {
  fun doSomething(num: Int): Boolean
}

// Complexity[main]: 1 + 1 (if) = 2
fun main() {
  val something = object : Something {
    // Complexity[doSomething]: 1 + 3 (if) = 4
    override fun doSomething(num: Int): Boolean {
      if (num % 2 == 0) return true
      if (num < 3) return true
      if (num % 5 == 3) return true
      return false
    }
  }
  if (something.doSomething(3)) {
    println("Something")
  }
}