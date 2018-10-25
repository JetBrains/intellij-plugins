// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.completion.vuex

import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.openapi.project.Project
import org.jetbrains.vuejs.index.DELIMITER
import org.jetbrains.vuejs.index.hasVue

object VueStoreUtils {
  const val VUEX = "vuex"
  const val ACTION = "actions"
  const val MUTATION = "mutations"
  const val STATE = "state"
  const val GETTER = "getters"

  fun hasVuex(project: Project): Boolean {
    val packageJsonData = PackageJsonUtil.getTopLevelPackageJsonData(project)
    if (packageJsonData == null) return false
    return hasVue(project) && packageJsonData.isDependencyOfAnyType(VUEX)
  }


  fun normalizeName(name: String): String {
    return name.substringAfter("$DELIMITER$DELIMITER", name)
  }
}