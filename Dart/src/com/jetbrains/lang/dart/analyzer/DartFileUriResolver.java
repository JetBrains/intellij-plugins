package com.jetbrains.lang.dart.analyzer;

import com.google.dart.engine.source.Source;
import com.google.dart.engine.source.UriKind;
import com.google.dart.engine.source.UriResolver;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.net.URI;

public class DartFileUriResolver extends UriResolver {

  private final Project myProject;

  public DartFileUriResolver(final Project project) {
    myProject = project;
  }

  public Source fromEncoding(final UriKind kind, final URI uri) {
    if (kind != UriKind.FILE_URI) return null;

    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(uri.getPath());
    final DartFileBasedSource source = file == null ? null : DartFileBasedSource.getSource(myProject, file);

    if (source != null && source.getUriKind() != UriKind.PACKAGE_URI) {
      DartInProcessAnnotator.LOG.warn("DartFileUriResolver.fromEncoding: unexpected uri kind for file " + uri);
    }

    return source;
  }

  public Source resolveAbsolute(final URI uri) {
    if (!"file".equals(uri.getScheme())) return null;

    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(uri.getPath());
    final DartFileBasedSource source = file == null ? null : DartFileBasedSource.getSource(myProject, file);
    return source != null && source.getUriKind() == UriKind.FILE_URI ? source : null;
  }
}
