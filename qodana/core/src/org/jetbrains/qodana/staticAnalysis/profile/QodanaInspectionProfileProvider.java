package org.jetbrains.qodana.staticAnalysis.profile;

import com.intellij.codeInspection.InspectionApplicationException;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public interface QodanaInspectionProfileProvider {
  ExtensionPointName<QodanaInspectionProfileProvider> EP_NAME =
    ExtensionPointName.create("org.intellij.qodana.inspectionProfileProvider");

  QodanaInspectionProfile provideProfile(@NotNull String profileName, @Nullable Project project);

  List<String> getAllProfileNames(@Nullable Project project);

  static @Nullable QodanaInspectionProfile runProviders(@NotNull String profileName, @Nullable Project project) {
    for (QodanaInspectionProfileProvider provider : EP_NAME.getExtensionList()) {
      QodanaInspectionProfile profile = provider.provideProfile(profileName, project);
      if (profile != null) return profile;
    }
    return null;
  }

  static @NotNull List<QodanaInspectionProfile> allProfiles(@Nullable Project project) {
    var allExtensions = EP_NAME.getExtensionList();
    return allExtensions.stream()
      .flatMap(provider -> provider
        .getAllProfileNames(project).stream()
        .map(profileName -> {
               try {
                 return provider.provideProfile(profileName, project);
               }
               catch (InspectionApplicationException e) {
                 return null;
               }
             }
        )
        .filter(Objects::nonNull)
      )
      .toList();
  }
}
