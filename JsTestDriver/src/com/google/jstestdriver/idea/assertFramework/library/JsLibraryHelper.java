package com.google.jstestdriver.idea.assertFramework.library;

import com.intellij.ide.scriptingContext.ScriptingLibraryMappings;
import com.intellij.lang.javascript.library.JSLibraryManager;
import com.intellij.lang.javascript.library.JSLibraryMappings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.scripting.ScriptingLibraryModel;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JsLibraryHelper {

  private static final Logger LOG = Logger.getInstance(JsLibraryHelper.class);

  private final Project myProject;
  private final JSLibraryManager myScriptingLibraryManager;

  public JsLibraryHelper(@NotNull Project project) {
    myProject = project;
    myScriptingLibraryManager = ServiceManager.getService(project, JSLibraryManager.class);
  }

  @Nullable
  public ScriptingLibraryModel lookupLibraryByContent(@NotNull Collection<VirtualFile> expectedSourceFiles) {
    ScriptingLibraryModel[] libraryModels = myScriptingLibraryManager.getLibraries();
    for (ScriptingLibraryModel libraryModel : libraryModels) {
      if (scriptingLibraryModelConsistsOf(libraryModel, expectedSourceFiles)) {
        return libraryModel;
      }
    }
    return null;
  }

  public static boolean scriptingLibraryModelConsistsOf(@NotNull ScriptingLibraryModel libraryModel,
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
          if (sourceFile.getLength() == expected.getLength()) {
            try {
              byte[] content1 = sourceFile.contentsToByteArray();
              byte[] content2 = expected.contentsToByteArray();
              found = Arrays.equals(content1, content2);
              if (found) {
                break;
              }
            }
            catch (IOException ignored) {
            }
          }
        }
      }
      if (!found) {
        return false;
      }
    }
    return true;
  }

  private static <E> boolean isEmpty(Collection<E> collection) {
    return collection == null || collection.isEmpty();
  }

  public String findAvailableJsLibraryName(@NotNull String initialLibraryName) {
    myScriptingLibraryManager.reset();

    String libraryName = initialLibraryName;
    boolean available = getScriptingLibraryModel(libraryName) == null;
    int id = 1;
    while (!available) {
      libraryName = initialLibraryName + " #" + id;
      available = getScriptingLibraryModel(libraryName) == null;
      id++;
    }
    return libraryName;
  }

  @Nullable
  public ScriptingLibraryModel getScriptingLibraryModel(@NotNull String libraryName) {
    return myScriptingLibraryManager.getLibraryByName(libraryName);
  }

  @Nullable
  public ScriptingLibraryModel createJsLibrary(final String libraryName, final List<VirtualFile> sourceFiles) {
    Computable<ScriptingLibraryModel> task = new Computable<ScriptingLibraryModel>() {
      @Override
      @Nullable
      public ScriptingLibraryModel compute() {
        try {
          ScriptingLibraryModel libraryModel = getScriptingLibraryModel(libraryName);
          if (libraryModel != null) {
            if (scriptingLibraryModelConsistsOf(libraryModel, sourceFiles)) {
              LOG.info("Library '" + libraryModel.getName() + "' will be reused, new library creation isn't needed.");
              return libraryModel;
            } else {
              myScriptingLibraryManager.removeLibrary(libraryModel);
              myScriptingLibraryManager.commitChanges();
              LOG.info("Library '" + libraryModel.getName() + "' has been removed (it's impossible to reuse).");
            }
          }
          libraryModel = myScriptingLibraryManager.createLibrary(
            libraryName,
            VfsUtil.toVirtualFileArray(sourceFiles),
            VirtualFile.EMPTY_ARRAY,
            ArrayUtil.EMPTY_STRING_ARRAY
          );
          myScriptingLibraryManager.commitChanges();
          LOG.info("Library '" + libraryModel.getName() + "' has been successfully created.");
          return libraryModel;
        } catch (Exception ex) {
          LOG.error("Can not create JavaScript Library '" + libraryName + "'", ex);
          return null;
        }
      }
    };
    return ApplicationManager.getApplication().runWriteAction(task);
  }

  public boolean associateLibraryWithProject(@NotNull final ScriptingLibraryModel libraryModel) {
    Computable<Boolean> task = new Computable<Boolean>() {
      @Override
      @NotNull
      public Boolean compute() {
        try {
          ScriptingLibraryMappings libraryMappings = ServiceManager.getService(myProject, JSLibraryMappings.class);
          boolean isAssociated = libraryMappings.isAssociatedWithProject(libraryModel.getName());
          if (isAssociated) {
            LOG.info("Library '" + libraryModel.getName() + "' is already associated with the project");
            return true;
          }
          libraryMappings.associateWithProject(libraryModel.getName());
          LOG.info("Library '" + libraryModel.getName() + "' has been successfully associated with the project");
          return true;
        } catch (Exception ex) {
          LOG.error(ex);
          return false;
        }
      }
    };
    return ApplicationManager.getApplication().runWriteAction(task);
  }
}
