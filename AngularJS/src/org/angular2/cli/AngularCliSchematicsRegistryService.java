// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli;

import com.intellij.javascript.nodejs.packageJson.InstalledPackageVersion;
import com.intellij.javascript.nodejs.packageJson.NodePackageBasicInfo;
import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AngularCliSchematicsRegistryService {

  /**
   * Fast method to get list of all packages that supports ng-add.
   */
  @NotNull
  public abstract List<NodePackageBasicInfo> getPackagesSupportingNgAdd(long timeout);

  /**
   * Fast method to determine whether any version of the package supports ng-add.
   */
  public abstract boolean supportsNgAdd(@NotNull String packageName,
                                        long timeout);

  /**
   * Checks whether any version of package supports ng-add and than
   * fetches exact information from NPM registry about highest package version in provided range,
   * and checks it for schematics property. Assumes that ng-add is supported if schematics
   * property is present. Values are cached.
   */
  public abstract boolean supportsNgAdd(@NotNull String packageName,
                                        @NotNull String versionOrRange,
                                        long timeout);

  /**
   * Fast method to determine whether locally installed package supports ng-add.
   * This method provides exact information by checking contents of schematic
   * json file for ng-add schematic. Values are cached.
   */
  public abstract boolean supportsNgAdd(@NotNull InstalledPackageVersion version);

  @NotNull
  public static AngularCliSchematicsRegistryService getInstance() {
    return ServiceManager.getService(AngularCliSchematicsRegistryService.class);
  }
}
