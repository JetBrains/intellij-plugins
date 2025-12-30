// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.angular2.cli.AngularCliUtil
import org.angular2.cli.actions.AngularCliAddDependencyAction
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

class AngularCliAddQuickFix(private val myPackageJson: VirtualFile, private val myPackageName: String,
                            private val myVersionSpec: String, private val myReinstall: Boolean) : LocalQuickFix, HighPriorityAction {

  @Nls
  override fun getName(): String {
    return Angular2Bundle.message(if (myReinstall)
                                    "angular.quickfix.json.ng-add.name.reinstall"
                                  else
                                    "angular.quickfix.json.ng-add.name.run",
                                  myPackageName)
  }

  @Nls
  override fun getFamilyName(): String {
    return Angular2Bundle.message("angular.quickfix.json.ng-add.family")
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    if (AngularCliUtil.hasAngularCLIPackageInstalled(myPackageJson)) {
      AngularCliAddDependencyAction.runAndShowConsoleLater(
        project, myPackageJson.parent, myPackageName, myVersionSpec.trim { it <= ' ' }, !myReinstall)
    }
    else {
      AngularCliUtil.notifyAngularCliNotInstalled(project, myPackageJson.parent,
                                                  Angular2Bundle.message("angular.quickfix.json.ng-add.error.cant-run"))
    }
  }

  override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
    return IntentionPreviewInfo.EMPTY
  }
}
