// Total complexity: 5

open class MyException : Exception("My Exception")

class MyOtherException : MyException()

// Complexity[throwException]: 1 + 3 (catch) + 1 (catch) = 5
fun throwException() {
  try {
    throw MyException()
  } catch (e: MyException) {
    println(e.message)
  } catch (e: MyOtherException) {
    println(e.message)
  } catch (e: Exception) {
    println(e.message)
  }

  try {
    throw MyException()
  } catch (e: Exception) {
    println(e.message)
  }
}