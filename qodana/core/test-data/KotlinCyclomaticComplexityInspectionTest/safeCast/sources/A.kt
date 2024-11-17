
// Complexity[safeCast]: 1
fun safeCast(y: Any): String? {
  val x: String? = y as? String
  return x
}