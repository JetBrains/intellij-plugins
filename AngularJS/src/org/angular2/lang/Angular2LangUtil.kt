// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.angular2.Angular2Framework.Companion.instance

object Angular2LangUtil {
  const val ANGULAR_CORE_PACKAGE: String = "@angular/core"
  const val ANGULAR_COMMON_PACKAGE: String = "@angular/common"
  const val ANGULAR_FORMS_PACKAGE: String = "@angular/forms"
  const val ANGULAR_ROUTER_PACKAGE: String = "@angular/router"
  const val ANGULAR_CLI_PACKAGE: String = "@angular/cli"
  const val `$IMPLICIT`: String = "\$implicit"
  const val EVENT_EMITTER: String = "EventEmitter"
  const val OUTPUT_CHANGE_SUFFIX: String = "Change"

  @JvmStatic
  fun isAngular2Context(context: PsiElement): Boolean {
    return instance.isInContext(context)
  }

  @JvmStatic
  fun isAngular2Context(project: Project, context: VirtualFile): Boolean {
    return instance.isInContext(context, project)
  }

  @JvmStatic
  fun isAngular2Context(file: PsiFile, offset: Int): Boolean {
    return file.findElementAt(offset)?.let { instance.isInContext(it) } ?: false
  }
}