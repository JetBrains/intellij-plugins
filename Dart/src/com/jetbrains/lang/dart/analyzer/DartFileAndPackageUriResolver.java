package com.jetbrains.lang.dart.analyzer;

import com.google.dart.engine.source.Source;
import com.google.dart.engine.source.UriResolver;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

import static com.jetbrains.lang.dart.util.DartUrlResolver.*;

public class DartFileAndPackageUriResolver extends UriResolver {

  private final @NotNull Project myProject;
  private final @NotNull DartUrlResolver myDartUrlResolver;

  public DartFileAndPackageUriResolver(final @NotNull Project project, final @NotNull DartUrlResolver dartUrlResolver) {
    myProject = project;
    myDartUrlResolver = dartUrlResolver;
  }

  public Source resolveAbsolute(final URI uri) {
    final String scheme = uri.getScheme();
    if (FILE_SCHEME.equals(scheme) ||
        PACKAGE_SCHEME.equals(scheme) ||
        (ApplicationManager.getApplication().isUnitTestMode() && TEMP_SCHEME.equals(scheme))) {
      final VirtualFile file = myDartUrlResolver.findFileByDartUrl(uri.toString());
      return file == null ? null : DartFileBasedSource.getSource(myProject, file);
    }

    return null;
  }
}
