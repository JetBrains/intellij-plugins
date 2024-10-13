class A(private val mutableList: MutableList<Int>) {
  // Complexity[constructor]: 1 + 1 (for) + 1 (if) + 1 (&&) = 4
  constructor(list: List<Int>): this(mutableListOf()) {
    for (num in list) {
      if (num > 0 && num % 2 == 0) {
        mutableList.add(num)
      }
    }
  }
}