package com.intellij.dts.lang.psi

sealed interface DtsArray : DtsValue {
  val dtsValues: List<DtsValue>

  interface Cell : DtsArray {
    val dtsBits: Int?
  }

  interface Byte : DtsArray
}