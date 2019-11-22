/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.check.go

class GoCheckDeleteParameterNames : GoCheck() {

  override fun check(): Boolean {
    val parameters = goFile?.findMethod("greet")?.signature?.parameters ?: return false
    return parameters.parameterDeclarationList.size != 0 && parameters.definitionList.size == 0
  }

}