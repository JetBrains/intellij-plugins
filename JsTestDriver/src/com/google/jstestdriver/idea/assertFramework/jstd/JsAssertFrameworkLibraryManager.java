package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.assertFramework.jstd.jsSrc.JstdDefaultAssertionFrameworkSrcMarker;
import com.google.jstestdriver.idea.util.VfsUtils;
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

import java.util.List;

public class JsAssertFrameworkLibraryManager {

  private static final Logger LOG = Logger.getInstance(JsAssertFrameworkLibraryManager.class);

  public static final String LIBRARY_NAME = "JsTestDriver Assertion Framework";

  private JsAssertFrameworkLibraryManager() {}

  public static ScriptingLibraryModel createScriptingLibraryModelAndAssociateIt(final @NotNull Project project,
                                                                                final VirtualFile rootForAssociation) {
    return ApplicationManager.getApplication().runWriteAction(new Computable<ScriptingLibraryModel>() {

      @Override
      public ScriptingLibraryModel compute() {
        JSLibraryManager libraryManager = ServiceManager.getService(project, JSLibraryManager.class);
        JSLibraryMappings mappings = ServiceManager.getService(project, JSLibraryMappings.class);

        ScriptingLibraryModel scriptingLibraryModel = libraryManager.getLibraryByName(LIBRARY_NAME);
        if (scriptingLibraryModel != null) {
          libraryManager.removeLibrary(scriptingLibraryModel);
          mappings.disassociate(rootForAssociation, LIBRARY_NAME);
          libraryManager.commitChanges();
          LOG.info("Removing '" + LIBRARY_NAME + "' library and disassociating it from " + rootForAssociation);
        }
        VirtualFile[] arrayVirtualFiles = getAdditionalSourceFiles();
        scriptingLibraryModel = libraryManager.createLibrary(
          LIBRARY_NAME,
          arrayVirtualFiles,
          VirtualFile.EMPTY_ARRAY,
          ArrayUtil.EMPTY_STRING_ARRAY
        );
        libraryManager.commitChanges();
        mappings.associate(rootForAssociation, LIBRARY_NAME);
        LOG.info("Library '" + LIBRARY_NAME + "' has been associated with " + rootForAssociation);
        return scriptingLibraryModel;
      }
    });
  }

  @NotNull
  private static VirtualFile[] getAdditionalSourceFiles() {
    List<VirtualFile> files = VfsUtils.findVirtualFilesByResourceNames(
      JstdDefaultAssertionFrameworkSrcMarker.class,
      new String[]{"Asserts.js", "TestCase.js"}
    );
    return VfsUtil.toVirtualFileArray(files);
  }
}
