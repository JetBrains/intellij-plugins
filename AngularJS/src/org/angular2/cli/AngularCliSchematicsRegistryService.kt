// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

import com.intellij.javascript.nodejs.packageJson.InstalledPackageVersion
import com.intellij.javascript.nodejs.packageJson.NodePackageBasicInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

abstract class AngularCliSchematicsRegistryService {

  /**
   * Fast method to get list of all packages that supports ng-add.
   */
  abstract fun getPackagesSupportingNgAdd(timeout: Long): List<NodePackageBasicInfo>

  /**
   * Fast method to determine whether any version of the package supports ng-add.
   */
  abstract fun supportsNgAdd(packageName: String,
                             timeout: Long): Boolean

  /**
   * Checks whether any version of package supports ng-add and than
   * fetches exact information from NPM registry about highest package version in provided range,
   * and checks it for schematics property. Assumes that ng-add is supported if schematics
   * property is present. Values are cached.
   */
  abstract fun supportsNgAdd(packageName: String,
                             versionOrRange: String,
                             timeout: Long): Boolean

  /**
   * Fast method to determine whether locally installed package supports ng-add.
   * This method provides exact information by checking contents of schematic
   * json file for ng-add schematic. Values are cached.
   */
  abstract fun supportsNgAdd(version: InstalledPackageVersion): Boolean

  /**
   * Loads schematics available in a particular location. The results are cached
   * and recalculated on every change of package.json in any node_modules directory.
   */
  fun getSchematics(project: Project,
                    cliFolder: VirtualFile): Collection<Schematic> {
    return getSchematics(project, cliFolder, false, true)
  }

  /**
   * Loads schematics available in a particular location. The results are cached
   * and recalculated on every change of package.json in any node_modules directory.
   */
  abstract fun getSchematics(project: Project,
                             cliFolder: VirtualFile,
                             includeHidden: Boolean,
                             logErrors: Boolean): Collection<Schematic>

  /**
   * Clears cache for getSchematics method
   */
  abstract fun clearProjectSchematicsCache()

  companion object {

    @JvmStatic
    val instance: AngularCliSchematicsRegistryService
      get() = ApplicationManager.getApplication().getService(AngularCliSchematicsRegistryService::class.java)
  }
}
