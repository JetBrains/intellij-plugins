package com.jetbrains.lang.dart.ide.runner;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public class DartConsoleFilter implements Filter {

  private final @NotNull Project myProject;
  private final @Nullable DartSdk mySdk;
  private final @Nullable VirtualFile myContextFile;
  private Collection<VirtualFile> myAllPubspecYamlFiles;

  public DartConsoleFilter(final @NotNull Project project) {
    this(project, null);
  }

  public DartConsoleFilter(final @NotNull Project project, final @Nullable VirtualFile contextFile) {
    myProject = project;
    mySdk = DartSdk.getGlobalDartSdk();
    myContextFile = contextFile;
  }

  @Nullable
  public Result applyFilter(final String line, final int entireLength) {
    final DartPositionInfo info = DartPositionInfo.parsePositionInfo(line);
    if (info == null) return null;

    final VirtualFile file;
    switch (info.type) {
      case FILE:
        file = LocalFileSystem.getInstance().findFileByPath(info.path);
        break;
      case DART:
        // todo where to get source for files like "_RawReceivePortImpl._handleMessage (dart:isolate-patch/isolate_patch.dart:93)"?
        if (mySdk != null) {
          // info.path is either lib name (like "math") or relative path (like "collection/splay_tree.dart")
          if (info.path.contains("/")) {
            file = LocalFileSystem.getInstance().findFileByPath(mySdk.getHomePath() + "/lib/" + info.path);
          }
          else {
            file = DartLibraryIndex.getStandardLibraryFromSdk(myProject, info.path);
          }
        }
        else {
          file = null;
        }
        break;
      case PACKAGE:
        if (myContextFile != null) {
          file = DartResolveUtil.getPackagePrefixImportedFile(myProject, myContextFile, "package:" + info.path);
        }
        else {
          if (myAllPubspecYamlFiles == null) {
            myAllPubspecYamlFiles = FilenameIndex.getVirtualFilesByName(myProject, PUBSPEC_YAML, GlobalSearchScope.projectScope(myProject));
          }

          VirtualFile inPackage = null;
          for (VirtualFile yamlFile : myAllPubspecYamlFiles) {
            inPackage = DartResolveUtil.getPackagePrefixImportedFile(myProject, yamlFile, "package:" + info.path);
            if (inPackage != null) {
              break;
            }
          }
          file = inPackage;
        }
        break;
      default:
        file = null;
    }

    if (file != null && !file.isDirectory()) {
      final int highlightStartOffset = entireLength - line.length() + info.highlightingStartIndex;
      final int highlightEndOffset = entireLength - line.length() + info.highlightingEndIndex;
      return new Result(highlightStartOffset, highlightEndOffset, new OpenFileHyperlinkInfo(myProject, file, info.line, info.column));
    }

    return null;
  }
}
