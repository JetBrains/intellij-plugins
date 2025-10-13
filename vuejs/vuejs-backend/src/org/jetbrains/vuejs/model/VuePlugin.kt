package org.jetbrains.vuejs.model

interface VuePlugin : VueContainer {
  val plugins: List<VuePlugin>
}
