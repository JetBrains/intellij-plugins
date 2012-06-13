package com.google.jstestdriver.idea.assertFramework.support;

import com.google.jstestdriver.idea.assertFramework.library.JsLibraryHelper;
import com.google.jstestdriver.idea.util.ProjectRootUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.webcore.libraries.ScriptingLibraryModel;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Collection;
import java.util.List;

/*
 * All operations should be executed on EDT.
 */
public class LibraryCreationController implements ExtractedSourceLocationController.ChangeListener {

  private static final Logger LOG = Logger.getInstance(LibraryCreationController.class);

  private final Project myProject;
  private final JTextField myJsLibraryNameTextField;
  private final String myAssertionFrameworkName;
  private final Collection<VirtualFile> myAdapterSourceFiles;
  private final JsLibraryHelper myJsLibraryHelper;
  private File myExtractDirectory;

  private LibraryCreationController(@NotNull Project project,
                                    @NotNull JTextField jsLibraryNameTextField,
                                    @NotNull String assertionFrameworkName,
                                    @NotNull Collection<VirtualFile> adapterSourceFiles) {
    myProject = project;
    myJsLibraryNameTextField = jsLibraryNameTextField;
    myAssertionFrameworkName = assertionFrameworkName;
    myAdapterSourceFiles = adapterSourceFiles;
    myJsLibraryHelper = new JsLibraryHelper(myProject);
    initNewLibraryName();
  }

  private void initNewLibraryName() {
    ScriptingLibraryModel libraryModel = myJsLibraryHelper.lookupLibraryByContent(myAdapterSourceFiles);
    if (libraryModel != null) {
      myJsLibraryNameTextField.setText(libraryModel.getName());
    } else {
      String libraryName = myJsLibraryHelper.findAvailableJsLibraryName(myAssertionFrameworkName + " adapter for JsTestDriver");
      myJsLibraryNameTextField.setText(libraryName);
    }
  }

  @Nullable
  public ValidationInfo validate() {
    String libraryName = myJsLibraryNameTextField.getText();
    if (libraryName.trim().isEmpty()) {
      return new ValidationInfo("Invalid library name", myJsLibraryNameTextField);
    }
    ScriptingLibraryModel libraryModel = myJsLibraryHelper.getScriptingLibraryModel(libraryName);
    if (libraryModel == null || JsLibraryHelper.scriptingLibraryModelConsistsOf(libraryModel, myAdapterSourceFiles)) {
      return null;
    }
    String message = "Library '" + libraryName + "' is already created and cannot be reused.";
    return new ValidationInfo(message, myJsLibraryNameTextField);
  }

  public static LibraryCreationController install(@NotNull Project project,
                                                  @NotNull JTextField jsLibraryNameTextField,
                                                  @NotNull String assertionFrameworkName,
                                                  @NotNull Collection<VirtualFile> adapterSourceFiles) {
    return new LibraryCreationController(project, jsLibraryNameTextField, assertionFrameworkName, adapterSourceFiles);
  }

  @Override
  public void onExtractDirectoryChanged(File extractDirectory) {
    boolean insideContentRoots = ProjectRootUtils.isInsideContentRoots(myProject, extractDirectory);
    myJsLibraryNameTextField.setEnabled(!insideContentRoots);
    myExtractDirectory = extractDirectory;
  }

  public void installCodeAssistance(final List<VirtualFile> extractedAdapterSourceFiles) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        boolean insideContentRoots = ProjectRootUtils.isInsideContentRoots(myProject, myExtractDirectory);
        if (insideContentRoots) {
          return;
        }
        String libraryName = myJsLibraryNameTextField.getText();
        ScriptingLibraryModel libraryModel = myJsLibraryHelper.createJsLibrary(
          libraryName, extractedAdapterSourceFiles
        );
        String dialogTitle = "Adding " + myAssertionFrameworkName + " adapter support for JsTestDriver";
        if (libraryModel == null) {
          Messages.showErrorDialog("Unable to create '" + libraryName + "' JavaScript library", dialogTitle);
          return;
        }
        boolean associated = myJsLibraryHelper.associateLibraryWithProject(libraryModel);
        if (!associated) {
          Messages.showErrorDialog("Unable to associate '" + libraryName + "' JavaScript library with project", dialogTitle);
        }
      }
    });
  }

}
