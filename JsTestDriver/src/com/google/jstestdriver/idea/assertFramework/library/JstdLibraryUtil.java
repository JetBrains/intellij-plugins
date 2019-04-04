package com.google.jstestdriver.idea.assertFramework.library;

import com.google.jstestdriver.idea.assertFramework.jstd.jsSrc.JstdDefaultAssertionFrameworkSrcMarker;
import com.intellij.lang.javascript.library.JSLibraryManager;
import com.intellij.lang.javascript.library.JSLibraryMappings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.webcore.libraries.ScriptingLibraryModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sergey Simonchik
 */
public class JstdLibraryUtil {

  public static final String LIBRARY_NAME = "JsTestDriver Assertion Framework";

  private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
  private static volatile Boolean JSTD_LIBRARY_EXISTS;

  private static void init(@NotNull Project project) {
    if (INITIALIZED.compareAndSet(false, true)) {
      doInit(project);
    }
  }

  private static void doInit(@NotNull final Project project) {
    ApplicationManager.getApplication().runReadAction(() -> {
      JSLibraryManager libraryManager = JSLibraryManager.getInstance(project);
      LibraryTable libraryTable = libraryManager.getLibraryTable(ScriptingLibraryModel.LibraryLevel.GLOBAL);
      libraryTable.addListener(new MyLibraryChangeWatcher());
    });
  }

  private static void libraryChanged() {
    JSTD_LIBRARY_EXISTS = null;
  }

  public static boolean isFileInJstdLibScope(@NotNull Project project, @NotNull VirtualFile file) {
    init(project);
    boolean result = false;
    boolean libExists = doesCorrectJstdLibExist(project);
    if (libExists) {
      result = isCorrectMapping(project, file);
    }
    return result;
  }

  private static boolean doesCorrectJstdLibExist(@NotNull final Project project) {
    Boolean correctJstdLibExists = JSTD_LIBRARY_EXISTS;
    if (correctJstdLibExists == null) {
      correctJstdLibExists = ReadAction.compute(() -> {
        VirtualFile libVirtualFile = VfsUtil.findFileByURL(
          JstdDefaultAssertionFrameworkSrcMarker.class.getResource("TestCase.js")
        );
        if (libVirtualFile == null) {
          return false;
        }
        JSLibraryManager libraryManager = JSLibraryManager.getInstance(project);
        for (ScriptingLibraryModel libraryModel : libraryManager.getAllLibraries()) {
          if (libraryModel == null) {
            continue;
          }
          String libraryName = libraryModel.getName();
          if (libraryName != null && libraryName.startsWith(LIBRARY_NAME)) {
            Library library = libraryModel.getOriginalLibrary();
            if (library instanceof LibraryEx) {
              LibraryEx libraryEx = (LibraryEx)library;
              if (libraryEx.isDisposed()) {
                continue;
              }
            }
            if (libraryModel.containsFile(libVirtualFile)) {
              return true;
            }
          }
        }
        return false;
      });
      JSTD_LIBRARY_EXISTS = correctJstdLibExists;
    }
    return correctJstdLibExists;
  }

  private static boolean isCorrectMapping(@NotNull Project project, @NotNull VirtualFile file) {
    JSLibraryMappings jsLibraryMappings = JSLibraryMappings.getInstance(project);
    // TODO search for all JsTD libraries
    List<VirtualFile> usageScope = jsLibraryMappings.getMappingsByLibraryName(LIBRARY_NAME);
    String filePath = file.getPath();
    for (VirtualFile root : usageScope) {
      if (root == null) {
        return true;
      }
      if (filePath.startsWith(root.getPath())) {
        return true;
      }
    }
    return false;
  }

  private static class MyLibraryChangeWatcher implements LibraryTable.Listener {
    @Override
    public void afterLibraryAdded(@NotNull Library newLibrary) {
      libraryChanged();
    }

    @Override
    public void afterLibraryRenamed(@NotNull Library library) {
      libraryChanged();
    }

    @Override
    public void afterLibraryRemoved(@NotNull Library library) {
      libraryChanged();
    }
  }
}
