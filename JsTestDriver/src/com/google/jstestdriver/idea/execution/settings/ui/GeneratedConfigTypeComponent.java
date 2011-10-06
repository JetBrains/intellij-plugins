package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.generator.JstdConfigGenerator;
import com.google.jstestdriver.idea.execution.generator.JstdGeneratedConfigStructure;
import com.google.jstestdriver.idea.util.CastUtils;
import com.google.jstestdriver.idea.util.TextChangeListener;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class GeneratedConfigTypeComponent extends JPanel {

  public static final GeneratedConfigTypeComponent INSTANCE = new GeneratedConfigTypeComponent();

  private String myJsFilePath;

  public JComponent createComponent(final Project project, final JsFileRunSettingsSection jsFileRunSettingsSection) {
    Box box = Box.createHorizontalBox();
    box.add(Box.createRigidArea(new Dimension(5, 0)));
    box.add(new JLabel("JavaScript test file dependencies will be detected automatically."));
    box.add(Box.createRigidArea(new Dimension(5, 0)));
    final JButton saveAsButton = new JButton("Save As...");
    saveAsButton.setMnemonic('v');
    saveAsButton.addActionListener(new SaveAsActionListener(project));
    jsFileRunSettingsSection.addJsFileTextFieldListener(new TextChangeListener() {
      @Override
      public void textChanged(String oldText, @NotNull String newText) {
        myJsFilePath = newText;
        boolean isFile = new File(myJsFilePath).isFile();
        saveAsButton.setEnabled(isFile);
      }
    });
    box.add(saveAsButton);
    box.add(Box.createHorizontalGlue());
    return box;
  }

  private class SaveAsActionListener implements ActionListener {

    private static final String LAST_OPENED_FILE_PATH = "jstd_last_opened_file_path";
    private static final String JSTD_EXT = ".jstd";
    private static final String MESSAGE_DIALOG_TITLE = "Saving generated JsTestDriver configuration file";

    private final FileFilter JSTD_CONFIG_FILE_FILTER = new FileFilter() {
      @Override
      public boolean accept(File f) {
        return f.isDirectory() || f.getName().endsWith(JSTD_EXT);
      }

      @Override
      public String getDescription() {
        return "JsTestDriver Configuration File (*.jstd)";
      }
    };

    private final Project myProject;

    private SaveAsActionListener(Project project) {
      myProject = project;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      File initialDir = findInitialDir();
      JFileChooser fileChooser = createTunedFileChooser();
      File jsFile = new File(myJsFilePath);
      String fileName = FileUtil.getNameWithoutExtension(jsFile).replace('.', '-');
      fileChooser.setSelectedFile(new File(initialDir, "generated-" + fileName));

      if (fileChooser.showSaveDialog(WindowManager.getInstance().suggestParentWindow(myProject)) !=
          JFileChooser.APPROVE_OPTION) {
        return;
      }
      File selectedFile = fileChooser.getSelectedFile();
      if (selectedFile == null) return;
      PropertiesComponent.getInstance(myProject).setValue(LAST_OPENED_FILE_PATH, selectedFile.getParent());
      File outputFile = findOutputFile(selectedFile, fileChooser.getFileFilter());
      try {
        writeJstdConfigTo(outputFile);
      } catch (Exception ioe) {
        showFailMessage("Saving failed due to internal fail.");
        ioe.printStackTrace();
      }
    }

    private JFileChooser createTunedFileChooser() {
      File initialDir = findInitialDir();
      JFileChooser fileChooser = new JFileChooser(initialDir);
      fileChooser.setFileView(new FileView() {
        @Override
        public Icon getIcon(File f) {
          if (f.isDirectory()) {
            return super.getIcon(f);
          }
          FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(f.getName());
          return fileType.getIcon();
        }
      });
      fileChooser.setMultiSelectionEnabled(false);
      fileChooser.setAcceptAllFileFilterUsed(true);
      fileChooser.addChoosableFileFilter(JSTD_CONFIG_FILE_FILTER);
      fileChooser.setFileFilter(JSTD_CONFIG_FILE_FILTER);

      fileChooser.setDialogTitle("Save As");
      return fileChooser;
    }

    @Nullable
    private File findInitialDir() {
      String path = PropertiesComponent.getInstance(myProject).getValue(LAST_OPENED_FILE_PATH);
      VirtualFile projectBaseDir = myProject.getBaseDir();
      if (path == null && projectBaseDir != null) {
        path = projectBaseDir.getPath();
      }
      File dir = new File(path);
      return dir.isDirectory() ? dir : null;
    }

    @NotNull
    private File findOutputFile(@NotNull final File selectedFile, FileFilter selectedFileFilter) {
      String extension = FileUtil.getExtension(selectedFile.getName());
      if (!extension.isEmpty()) {
        return selectedFile;
      }
      if (selectedFile.exists()) {
        return selectedFile;
      }
      if (selectedFileFilter == JSTD_CONFIG_FILE_FILTER) {
        return new File(selectedFile.getAbsolutePath() + JSTD_EXT);
      }
      return selectedFile;
    }

    private void writeJstdConfigTo(@NotNull final File outputFile) throws IOException {
      ResultWithError<JstdGeneratedConfigStructure, String> configStructureResult = buildConfigStructureResult();
      String errorMessage = configStructureResult.getError();
      if (errorMessage != null) {
        showFailMessage(errorMessage);
        return;
      }
      File outputDir = outputFile.getParentFile();
      if (!outputDir.exists()) {
        showFailMessage("Directory " + outputDir.getAbsolutePath() + " does not exists.");
        return;
      }
      if (!outputDir.isDirectory()) {
        showFailMessage(outputDir.getAbsolutePath() + " should be a directory.");
        return;
      }
      if (outputFile.exists()) {
        if (outputFile.isFile()) {
          int retCode = Messages.showYesNoDialog(myProject, outputFile.getAbsolutePath() + " already exists.\nDo you want to replace it?", "Confirm Save As", Messages.getWarningIcon());
          if (retCode == 1) {
            return;
          }
        } else {
          showFailMessage(outputFile + " exists, but it is not a file.");
          return;
        }
      }

      final JstdGeneratedConfigStructure configStructure = configStructureResult.getResult();
      final String content = configStructure.asFileContent();
      final VirtualFile outputDirVirtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(outputDir);
      if (outputDirVirtualFile == null) {
        throw new RuntimeException("Can't find VirtualFile for outputDir!");
      }
      try {
        final AtomicReference<IOException> refToIoException = new AtomicReference<IOException>();
        final VirtualFile outputFileVirtualFile = ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
          @Override
          public VirtualFile compute() {
            try {
              return outputDirVirtualFile.findOrCreateChildData(null, outputFile.getName());
            } catch (IOException e) {
              refToIoException.set(e);
              return null;
            }
          }
        });
        if (outputFileVirtualFile == null) {
          IOException ioe = refToIoException.get();
          if (ioe != null) {
            throw ioe;
          }
          showUnableToWriteTo(outputFile);
          return;
        }
        final Document document = FileDocumentManager.getInstance().getDocument(outputFileVirtualFile);
        if (document == null) {
          showUnableToWriteTo(outputFile);
          return;
        }
        CommandProcessor.getInstance().executeCommand(myProject, new Runnable() {
          @Override
          public void run() {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
              @Override
              public void run() {
                document.replaceString(0, document.getTextLength(), content);
                PsiDocumentManager.getInstance(myProject).commitDocument(document);
              }
            });
          }
        }, "SaveAsGeneratedJstdConfig", null, document);
        showSuccessMessage(outputFile);
      } catch (IOException e) {
        VirtualFile outputVirtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(outputFile);
        if (outputVirtualFile == null || !outputVirtualFile.exists()) {
          showFailMessage("Unable to create " + outputFile.getAbsolutePath());
        } else {
          showUnableToWriteTo(outputFile);
        }
      }
    }

    @NotNull
    private ResultWithError<JstdGeneratedConfigStructure, String> buildConfigStructureResult() {
      return ApplicationManager.getApplication().runReadAction(new Computable<ResultWithError<JstdGeneratedConfigStructure, String>>() {
        @Override
        public ResultWithError<JstdGeneratedConfigStructure, String> compute() {
          File jsIoFile = new File(myJsFilePath);
          VirtualFile jsVirtualFile = LocalFileSystem.getInstance().findFileByIoFile(jsIoFile);
          if (jsVirtualFile == null || !jsVirtualFile.isValid()) {
            return ResultWithError.newError("Could not find JavaScript file at " + jsIoFile.getAbsolutePath());
          }
          PsiFile psiFile = PsiManager.getInstance(myProject).findFile(jsVirtualFile);
          JSFile jsPsiFile = CastUtils.tryCast(psiFile, JSFile.class);
          if (jsPsiFile == null) {
            return ResultWithError.newError("Could not process " + jsIoFile.getAbsolutePath() + " as JavaScript file.");
          }
          JstdConfigGenerator generator = JstdConfigGenerator.INSTANCE;
          JstdGeneratedConfigStructure configStructure = generator.generateJstdConfigStructure(jsPsiFile);
          return ResultWithError.newResult(configStructure);
        }
      });
    }

    private void showUnableToWriteTo(File outputFile) {
      showFailMessage("Unable to write to " + outputFile.getAbsolutePath());
    }

    private void showFailMessage(String errorMessage) {
      Messages.showErrorDialog(errorMessage, MESSAGE_DIALOG_TITLE);
    }

    private void showSuccessMessage(File outputFile) {
      Messages.showInfoMessage("JsTestDriver configuration file was saved to " + outputFile.getAbsolutePath(), MESSAGE_DIALOG_TITLE);
    }

  }

  private static class ResultWithError<R, E> {
    private final R myResult;
    private final E myError;

    private ResultWithError(R result, E error) {
      myResult = result;
      myError = error;
    }

    public R getResult() {
      return myResult;
    }

    public E getError() {
      return myError;
    }

    public static <R, E> ResultWithError<R, E> newError(E error) {
      return new ResultWithError<R, E>(null, error);
    }

    public static <R, E> ResultWithError<R, E> newResult(R result) {
      return new ResultWithError<R, E>(result, null);
    }
  }

}
