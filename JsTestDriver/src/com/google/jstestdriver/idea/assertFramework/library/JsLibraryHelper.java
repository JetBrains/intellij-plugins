package com.google.jstestdriver.idea.assertFramework.library;

import com.intellij.lang.javascript.library.JSLibraryManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.webcore.ScriptingFrameworkDescriptor;
import com.intellij.webcore.libraries.ScriptingLibraryModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JsLibraryHelper {

  private static final Logger LOG = Logger.getInstance(JsLibraryHelper.class);

  private final List<VirtualFile> myLibraryFiles;
  private final JSLibraryManager myJsLibraryManager;
  private final String myAvailableLibraryName;
  private final ScriptingLibraryModel myReusableJsLibraryModel;

  public JsLibraryHelper(@NotNull Project project,
                         @NotNull String desiredLibraryName,
                         @NotNull List<VirtualFile> libraryFiles,
                         @NotNull ScriptingFrameworkDescriptor frameworkDescriptor) {
    myJsLibraryManager = ServiceManager.getService(project, JSLibraryManager.class);
    myAvailableLibraryName = findAvailableJsLibraryName(desiredLibraryName);
    myLibraryFiles = libraryFiles;
    myReusableJsLibraryModel = findReusableJavaScriptLibraryModel(myJsLibraryManager,
                                                                  desiredLibraryName,
                                                                  libraryFiles,
                                                                  frameworkDescriptor);
  }

  @Nullable
  private static ScriptingLibraryModel findReusableJavaScriptLibraryModel(@NotNull JSLibraryManager jsLibraryManager,
                                                                          @NotNull String desiredLibraryName,
                                                                          @NotNull List<VirtualFile> expectedSourceFiles,
                                                                          @NotNull ScriptingFrameworkDescriptor frameworkDescriptor) {
    ScriptingLibraryModel[] libraryModels = jsLibraryManager.getAllLibraries();
    for (ScriptingLibraryModel libraryModel : libraryModels) {
      if (libraryModel != null) {
        ScriptingFrameworkDescriptor libraryFrameworkDescriptor = libraryModel.getFrameworkDescriptor();
        if (libraryFrameworkDescriptor != null) {
          String libraryFrameworkName = libraryFrameworkDescriptor.getFrameworkName();
          if (libraryFrameworkName != null && libraryFrameworkName.equals(frameworkDescriptor.getFrameworkName())) {
            return libraryModel;
          }
        }
        else if (StringUtil.startsWith(libraryModel.getName(), desiredLibraryName)) {
          if (scriptingLibraryModelConsistsOf(libraryModel, expectedSourceFiles)) {
            return libraryModel;
          }
        }
      }
    }
    return null;
  }

  private static boolean scriptingLibraryModelConsistsOf(@NotNull ScriptingLibraryModel libraryModel,
                                                         @NotNull Collection<VirtualFile> expectedSourceFiles) {
    if (!isEmpty(libraryModel.getDocUrls()) || !isEmpty(libraryModel.getCompactFiles())) {
      return false;
    }
    Collection<VirtualFile> sourceFiles = libraryModel.getSourceFiles();
    if (sourceFiles.size() != expectedSourceFiles.size()) {
      return false;
    }
    for (VirtualFile sourceFile : sourceFiles) {
      boolean found = false;
      for (VirtualFile expected : expectedSourceFiles) {
        if (sourceFile.getName().equals(expected.getName())) {
          found = true;
          break;
        }
      }
      if (!found) {
        return false;
      }
    }
    return true;
  }

  public boolean hasReusableLibraryModel() {
    return myReusableJsLibraryModel != null;
  }

  private static <E> boolean isEmpty(@Nullable Collection<E> collection) {
    return collection == null || collection.isEmpty();
  }

  @NotNull
  public String findAvailableJsLibraryName(@NotNull String initialLibraryName) {
    myJsLibraryManager.reset();

    String libraryName = initialLibraryName;
    boolean available = myJsLibraryManager.getLibraryByName(libraryName) == null;
    int id = 1;
    while (!available) {
      libraryName = initialLibraryName + " #" + id;
      available = myJsLibraryManager.getLibraryByName(libraryName) == null;
      id++;
    }
    return libraryName;
  }

  public boolean doesJavaScriptLibraryModelExist(@NotNull String libraryName) {
    return myJsLibraryManager.getLibraryByName(libraryName) != null;
  }

  @NotNull
  public String getJsLibraryName() {
    if (myReusableJsLibraryModel != null) {
      return myReusableJsLibraryModel.getName();
    }
    return myAvailableLibraryName;
  }

  @NotNull
  public ScriptingLibraryModel getOrCreateJsLibraryModel(@NotNull String libraryName) {
    if (myReusableJsLibraryModel != null) {
      return myReusableJsLibraryModel;
    }
    ScriptingLibraryModel libraryModel = myJsLibraryManager.createLibrary(
      libraryName,
      VfsUtilCore.toVirtualFileArray(myLibraryFiles),
      VirtualFile.EMPTY_ARRAY,
      ArrayUtil.EMPTY_STRING_ARRAY,
      ScriptingLibraryModel.LibraryLevel.GLOBAL,
      false
    );
    LOG.info("JavaScript library '" + libraryModel.getName() + "' has been successfully created.");
    return libraryModel;
  }

  public void commit() {
    myJsLibraryManager.commitChanges();
  }
}
