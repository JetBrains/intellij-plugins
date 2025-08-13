// Complexity[forLoop]: 1 + 1 (for) + 1 (if) = 3
fun forLoop(): String {
  var j = 0
  for (i in 1..10) {
    print("$i ")
    if (i % 2 == 0) {
      return "65"
    }
    j += i
  }
  println(j)
  return " 7"
}