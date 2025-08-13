package org

class Baz {
  val lazyInt by lazy { 1 + 1 }

  fun foo() {
    lazyInt
  }
}
