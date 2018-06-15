package org.angularjs.cli;

import com.intellij.javascript.nodejs.packageJson.NodePackageBasicInfo;
import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AngularCliSchematicsRegistryService {

  @NotNull
  public abstract List<NodePackageBasicInfo> getPackagesSupportingNgAdd(long timeout);

  public abstract boolean supportsNgAdd(@NotNull String packageName,
                                        @NotNull String versionOrRange,
                                        long timeout);

  public abstract boolean supportsSchematics(@NotNull String packageName,
                                             @NotNull String versionOrRange,
                                             long timeout);

  @NotNull
  public static AngularCliSchematicsRegistryService getInstance() {
    return ServiceManager.getService(AngularCliSchematicsRegistryService.class);
  }
}
