class A(private val list: MutableList<Int>) {
  // Complexity[init]: 1 + 1 (for) + 2 (if) = 4
  init {
    for (i in 1..10) {
      if (i % 2 == 0) {
        list.add(i)
      }
      else if (i % 5 == 0) {
        break;
      }
    }
  }
}