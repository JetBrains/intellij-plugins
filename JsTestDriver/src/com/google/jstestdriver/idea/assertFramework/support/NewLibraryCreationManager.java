package com.google.jstestdriver.idea.assertFramework.support;

import com.google.jstestdriver.idea.util.ProjectRootUtils;
import com.intellij.ide.scriptingContext.ScriptingLibraryMappings;
import com.intellij.lang.javascript.library.JSLibraryManager;
import com.intellij.lang.javascript.library.JSLibraryMappings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.scripting.ScriptingLibraryModel;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;

import javax.swing.*;
import java.io.File;
import java.util.List;

/*
 * All operations should be executed on EDT.
 */
class NewLibraryCreationManager implements ExtractDirectoryTypeManager.ChangeListener {

  private final Project myProject;
  private final JTextField myNewLibraryNameTextField;
  private final String myAssertionFrameworkName;
  private final JSLibraryManager myScriptingLibraryManager;
  private File myExtractDirectory;

  private NewLibraryCreationManager(Project project, JTextField newLibraryNameTextField, String assertionFrameworkName) {
    myProject = project;
    myNewLibraryNameTextField = newLibraryNameTextField;
    myAssertionFrameworkName = assertionFrameworkName;
    myScriptingLibraryManager = ServiceManager.getService(myProject, JSLibraryManager.class);
    initNewLibraryName();
  }

  private void initNewLibraryName() {
    String libraryName = findAvailableLibraryName();
    myNewLibraryNameTextField.setText(libraryName);
  }

  private String findAvailableLibraryName() {
    myScriptingLibraryManager.reset();

    final String initialLibraryName = myAssertionFrameworkName + " adapter for JsTestDriver";
    String libraryName = initialLibraryName;
    boolean available = checkGlobalLibraryAvailabilityByName(libraryName);
    int id = 1;
    while (!available) {
      libraryName = initialLibraryName + " #" + id;
      available = checkGlobalLibraryAvailabilityByName(libraryName);
      id++;
    }
    return libraryName;
  }

  public DialogWrapper.ValidationInfo validate() {
    String libraryName = myNewLibraryNameTextField.getText();
    boolean libraryNameAvailable = checkGlobalLibraryAvailabilityByName(libraryName);
    if (!libraryNameAvailable) {
      return new DialogWrapper.ValidationInfo("Library '" + libraryName + "' is already created.", myNewLibraryNameTextField);
    }
    return null;
  }

  private boolean checkGlobalLibraryAvailabilityByName(String libraryName) {
    return myScriptingLibraryManager.getLibraryByName(libraryName) == null;
  }

  public static NewLibraryCreationManager install(Project project, JTextField newJavaScriptLibraryTextField, String assertionFrameworkName) {
    return new NewLibraryCreationManager(project, newJavaScriptLibraryTextField, assertionFrameworkName);
  }

  @Override
  public void onExtractDirectoryChanged(File extractDirectory) {
    boolean insideContentRoots = ProjectRootUtils.isInsideContentRoots(myProject, extractDirectory);
    myNewLibraryNameTextField.setEnabled(!insideContentRoots);
    myExtractDirectory = extractDirectory;
  }

  public boolean installCodeAssistance(final List<VirtualFile> extractedAdapterSourceFiles) {
    return ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
      @Override
      public Boolean compute() {
        boolean insideContentRoots = ProjectRootUtils.isInsideContentRoots(myProject, myExtractDirectory);
        if (insideContentRoots) {
          return true;
        }
        return createLibrary(extractedAdapterSourceFiles);
      }
    });
  }

  private boolean createLibrary(List<VirtualFile> extractedAdapterSourceFiles) {
    ScriptingLibraryModel libraryModel = myScriptingLibraryManager.createLibrary(
        myNewLibraryNameTextField.getText(),
        VfsUtil.toVirtualFileArray(extractedAdapterSourceFiles),
        VirtualFile.EMPTY_ARRAY,
        ArrayUtil.EMPTY_STRING_ARRAY
    );
    boolean success = false;
    try {
      if (libraryModel != null) {
        myScriptingLibraryManager.commitChanges();
        success = true;
        ScriptingLibraryMappings libraryMappings = ServiceManager.getService(myProject, JSLibraryMappings.class);
        libraryMappings.associate(myProject.getBaseDir(), libraryModel.getName());
      }
    } catch (Exception ignore) {
    }
    if (!success) {
      Messages.showErrorDialog("Unable to create '" + myNewLibraryNameTextField.getText() + "' JavaScript library",
                               "Adding " + myAssertionFrameworkName + " adapter support for JsTestDriver");
    }
    return success;
  }
}
