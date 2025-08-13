data class Name(val name: String?)

data class Person(val name: Name?, val surname: Name?)

// Complexity[getName]: 1 + 1 (lambda) = 2
fun getName(person: Person?) {
  val s = person?.surname?.name?.apply {
    print(this)
  }
  println(s)
}