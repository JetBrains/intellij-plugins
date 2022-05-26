package com.jetbrains.plugins.meteor;

import com.intellij.lang.javascript.library.JSPredefinedLibraryProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.reference.SoftReference;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webcore.libraries.ScriptingLibraryModel;
import com.jetbrains.plugins.meteor.tsStubs.MeteorStubPath;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Provides Meteor typescript stubs library ("Definitely Typed")
 * <br>
 */
public final class MeteorJSPredefinedLibraryProvider extends JSPredefinedLibraryProvider {
  private static volatile SoftReference<VirtualFile> stubFile = null;

  public static void resetFile() {
    stubFile = null;
  }

  @Override
  public ScriptingLibraryModel @NotNull [] getPredefinedLibraries(@NotNull Project project) {
    if (!MeteorFacade.getInstance().isMeteorProject(project)) return ScriptingLibraryModel.EMPTY_ARRAY;

    final Set<VirtualFile> libFiles = getLibraryFiles();
    ScriptingLibraryModel libraryModel = ScriptingLibraryModel.createPredefinedLibrary(
      MeteorProjectStartupActivity.METEOR_LIBRARY_NAME,
      VfsUtilCore.toVirtualFileArray(libFiles),
      "http://docs.meteor.com",
      false
    );
    return new ScriptingLibraryModel[]{libraryModel};
  }

  @NotNull
  private static Set<VirtualFile> getLibraryFiles() {
    VirtualFile file = SoftReference.dereference(stubFile);
    if (file == null) {
      file = MeteorStubPath.getLastMeteorLib();
      stubFile = new SoftReference<>(file);
    }
    return ContainerUtil.newHashSet(file);
  }
}
