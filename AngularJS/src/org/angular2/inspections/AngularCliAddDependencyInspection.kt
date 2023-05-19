// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.javascript.nodejs.packageJson.NodeInstalledPackageFinder
import com.intellij.json.psi.JsonElementVisitor
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.json.psi.JsonValue
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.angular2.cli.AngularCliSchematicsRegistryService
import org.angular2.cli.AngularCliUtil
import org.angular2.inspections.quickfixes.AngularCliAddQuickFix
import org.angular2.lang.Angular2Bundle

class AngularCliAddDependencyInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : JsonElementVisitor() {
      override fun visitFile(file: PsiFile) {
        val packageJson = PackageJsonUtil.asPackageJsonFile(file)
        if (packageJson != null && AngularCliUtil.findCliJson(file.virtualFile.parent) != null) {
          annotate(packageJson, holder)
        }
      }
    }
  }

  override fun isEnabledByDefault(): Boolean {
    return true
  }

  companion object {

    private const val TIMEOUT: Long = 2000

    private fun annotate(file: JsonFile, holder: ProblemsHolder) {
      val packageJson = file.virtualFile
      val project = file.project
      if (packageJson == null || !JSLibraryUtil.isInProjectAndOutsideOfLibraryRoots(project, packageJson)) return

      val properties = PackageJsonUtil.getDependencies(file, PackageJsonUtil.PROD_DEV_DEPENDENCIES)
      if (properties.isEmpty()) return
      val finder = NodeInstalledPackageFinder(project, packageJson)
      for (property in properties) {
        val nameLiteral = property.nameElement as? JsonStringLiteral
        val versionLiteral = property.value as? JsonStringLiteral
        if (nameLiteral == null) {
          continue
        }

        val packageName = property.name
        val version = versionLiteral?.value ?: ""
        val pkgVersion = finder.findInstalledPackage(packageName)

        if ((pkgVersion != null && AngularCliSchematicsRegistryService.instance.supportsNgAdd(pkgVersion))
            || (pkgVersion == null && AngularCliSchematicsRegistryService.instance.supportsNgAdd(packageName, TIMEOUT))) {
          val message = Angular2Bundle.message("angular.inspection.install-with-ng-add.message",
                                               StringUtil.wrapWithDoubleQuote(packageName))
          val quickFix = AngularCliAddQuickFix(packageJson, packageName, version, pkgVersion != null)
          if (versionLiteral != null) {
            if (pkgVersion == null) {
              holder.registerProblem(versionLiteral, getTextRange(versionLiteral), message, quickFix)
            }
            else if (holder.isOnTheFly) {
              holder.registerProblem(versionLiteral, message, ProblemHighlightType.INFORMATION, quickFix)
            }
          }
          if (holder.isOnTheFly) {
            holder.registerProblem(nameLiteral, message, ProblemHighlightType.INFORMATION, quickFix)
          }
        }
      }
    }

    private fun getTextRange(element: JsonValue): TextRange {
      val range = element.textRange
      return if (element is JsonStringLiteral && range.length > 2
                 && StringUtil.isQuotedString(element.getText())) {
        TextRange(1, range.length - 1)
      }
      else TextRange.create(0, range.length)
    }
  }
}
