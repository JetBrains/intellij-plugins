// Total complexity: 3 + 4 = 7

class Class {
  // Complexity[foo]: 1 + 1 (while) + 1 (if) = 3
  fun foo(a: Int) {
    while (a < 10) {
      print("$a")

      if (a == 6) {
        return
      }
    }
  }
  class NestedClass {
    // Complexity[foo]: 1 + 1 (for) + 2 (if) = 4
    fun foo() {
      for (i in 1..10) {
        println(foo())
        if (i % 6 == 0) {
          println(i)
        }
        if (i % 3 == 0) {
          println(i)
        }
      }
    }
  }
}