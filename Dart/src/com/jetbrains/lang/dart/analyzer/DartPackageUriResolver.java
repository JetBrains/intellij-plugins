package com.jetbrains.lang.dart.analyzer;

import com.google.dart.engine.source.Source;
import com.google.dart.engine.source.UriKind;
import com.google.dart.engine.source.UriResolver;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

import static com.jetbrains.lang.dart.util.DartResolveUtil.PACKAGE_PREFIX;
import static com.jetbrains.lang.dart.util.DartResolveUtil.PACKAGE_SCHEME;

public class DartPackageUriResolver extends UriResolver {
  private final @NotNull Project myProject;
  private final @NotNull VirtualFile myPackagesFolder;

  public DartPackageUriResolver(final @NotNull Project project, final @NotNull VirtualFile packagesFolder) {
    myProject = project;
    myPackagesFolder = packagesFolder;
  }

  public Source fromEncoding(final UriKind kind, final URI uri) {
    if (kind != UriKind.PACKAGE_URI) return null;

    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(uri.getPath());
    final DartFileBasedSource source = file == null ? null : DartFileBasedSource.getSource(myProject, file);

    if (source != null && source.getUriKind() != UriKind.PACKAGE_URI) {
      DartInProcessAnnotator.LOG.warn("DartPackageFileUriResolver.fromEncoding: unexpected uri kind for file " + uri);
    }

    return source;
  }

  public Source resolveAbsolute(final URI uri) {
    if (!PACKAGE_SCHEME.equals(uri.getScheme())) return null;

    final VirtualFile file = DartResolveUtil.getPackagePrefixImportedFile(myProject, myPackagesFolder,
                                                                          PACKAGE_PREFIX + uri.getSchemeSpecificPart());
    final DartFileBasedSource source = file == null ? null : DartFileBasedSource.getSource(myProject, file);
    return source != null && source.getUriKind() == UriKind.PACKAGE_URI ? source : null;
  }
}
