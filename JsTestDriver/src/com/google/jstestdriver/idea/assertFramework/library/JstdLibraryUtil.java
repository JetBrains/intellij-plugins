package com.google.jstestdriver.idea.assertFramework.library;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.assertFramework.jstd.jsSrc.JstdDefaultAssertionFrameworkSrcMarker;
import com.intellij.ProjectTopics;
import com.intellij.webcore.libraries.ScriptingLibraryMappings;
import com.intellij.lang.javascript.library.JSLibraryManager;
import com.intellij.lang.javascript.library.JSLibraryMappings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerAdapter;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.roots.ModuleRootAdapter;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.webcore.libraries.ScriptingLibraryManager;
import com.intellij.webcore.libraries.ScriptingLibraryModel;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sergey Simonchik
 */
public class JstdLibraryUtil {

  public static final String LIBRARY_NAME = "JsTestDriver Assertion Framework";

  private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
  private static volatile Boolean JSTD_LIBRARY_EXISTS = null;
  private static volatile List<VirtualFile> JSTD_LIBRARY_MAPPINGS = null;

  private static void init(@NotNull Project project) {
    if (INITIALIZED.compareAndSet(false, true)) {
      doInit(project);
    }
  }

  private static void doInit(@NotNull final Project project) {
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
      @Override
      public void run() {
        JSLibraryManager libraryManager = ServiceManager.getService(project, JSLibraryManager.class);
        LibraryTable libraryTable = libraryManager.getLibraryTable(ScriptingLibraryModel.LibraryLevel.GLOBAL);
        libraryTable.addListener(new MyLibraryChangeWatcher());

        ProjectManagerListener rootListener = new ProjectRootsWatcher(project);
        ProjectManager.getInstance().addProjectManagerListener(rootListener);
      }
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
      correctJstdLibExists = ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
        @Override
        public Boolean compute() {
          VirtualFile libVirtualFile = VfsUtil.findFileByURL(
            JstdDefaultAssertionFrameworkSrcMarker.class.getResource("TestCase.js")
          );
          if (libVirtualFile == null) {
            return false;
          }
          ScriptingLibraryManager libraryManager = ServiceManager.getService(project, JSLibraryManager.class);
          ScriptingLibraryModel libraryModel = libraryManager.getLibraryByName(JstdLibraryUtil.LIBRARY_NAME);
          if (libraryModel == null) {
            return false;
          }
          return libraryModel.containsFile(libVirtualFile);
        }
      });
      JSTD_LIBRARY_EXISTS = correctJstdLibExists;
    }
    return correctJstdLibExists;
  }

  private static boolean isCorrectMapping(@NotNull Project project, @NotNull VirtualFile file) {
    List<VirtualFile> roots = getCachedUsageScope(project);
    String filePath = file.getPath();
    for (VirtualFile root : roots) {
      if (root == null) {
        return true;
      }
      if (filePath.startsWith(root.getPath())) {
        return true;
      }
    }
    return false;
  }

  private static List<VirtualFile> getCachedUsageScope(@NotNull final Project project) {
    List<VirtualFile> mappings = JSTD_LIBRARY_MAPPINGS;
    if (mappings == null) {
      mappings = ApplicationManager.getApplication().runReadAction(new Computable<List<VirtualFile>>() {
        @Override
        public List<VirtualFile> compute() {
          return getUsageScope(project);
        }
      });
      JSTD_LIBRARY_MAPPINGS = mappings;
    }
    return mappings;
  }

  private static List<VirtualFile> getUsageScope(@NotNull Project project) {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    JSLibraryMappings libraryMappings = ServiceManager.getService(project, JSLibraryMappings.class);
    Map<VirtualFile, ScriptingLibraryModel> allMappings = libraryMappings.getMappings();
    List<VirtualFile> roots = Lists.newArrayList();
    for (Map.Entry<VirtualFile, ScriptingLibraryModel> entry : allMappings.entrySet()) {
      ScriptingLibraryModel libraryModel = entry.getValue();
      final List<ScriptingLibraryModel> libraryModels;
      if (libraryModel instanceof ScriptingLibraryMappings.CompoundLibrary) {
        ScriptingLibraryMappings.CompoundLibrary compoundLibrary = (ScriptingLibraryMappings.CompoundLibrary) libraryModel;
        libraryModels = ImmutableList.copyOf(compoundLibrary.getLibraries());
      } else {
        libraryModels = Collections.singletonList(libraryModel);
      }
      for (ScriptingLibraryModel model : libraryModels) {
        if (LIBRARY_NAME.equals(model.getName())) {
          roots.add(entry.getKey());
        }
      }
    }
    return roots;
  }

  private static class MyLibraryChangeWatcher implements LibraryTable.Listener {
    @Override
    public void afterLibraryAdded(Library newLibrary) {
      libraryChanged();
    }

    @Override
    public void afterLibraryRenamed(Library library) {
      libraryChanged();
    }

    @Override
    public void beforeLibraryRemoved(Library library) {
    }

    @Override
    public void afterLibraryRemoved(Library library) {
      libraryChanged();
    }
  }

  private static class ProjectRootsWatcher extends ProjectManagerAdapter {
    private final Map<Project, MessageBusConnection> myConnections = new HashMap<Project, MessageBusConnection>();

    private ProjectRootsWatcher(@NotNull Project project) {
      projectOpened(project);
    }

    @Override
    public void projectOpened(Project project) {
      if (myConnections.containsKey(project)) {
        return;
      }
      MessageBusConnection conn = project.getMessageBus().connect();
      myConnections.put(project, conn);
      conn.subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootAdapter() {
        @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
        @Override
        public void rootsChanged(final ModuleRootEvent event) {
          JSTD_LIBRARY_MAPPINGS = null;
        }
      });
    }

    @Override
    public void projectClosed(Project project) {
      final MessageBusConnection conn = myConnections.remove(project);
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

}
