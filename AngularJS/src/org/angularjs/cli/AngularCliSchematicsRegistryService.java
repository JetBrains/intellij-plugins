package org.angularjs.cli;

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
   * Fetches exact information from NPM registry about highest package version in range,
   * checks for schematics property, downloads package tar.gz and checks schematics collection
   * JSON contents for presence of ng-add schematic. Values are cached.
   */
  public abstract boolean supportsNgAdd(@NotNull String packageName,
                                        @NotNull String versionOrRange,
                                        long timeout);

  /**
   * Fast method to determine whether locally installed package supports ng-add.
   * Values are cached.
   */
  public abstract boolean supportsNgAdd(@NotNull InstalledPackageVersion version);

  @NotNull
  public static AngularCliSchematicsRegistryService getInstance() {
    return ServiceManager.getService(AngularCliSchematicsRegistryService.class);
  }
}
