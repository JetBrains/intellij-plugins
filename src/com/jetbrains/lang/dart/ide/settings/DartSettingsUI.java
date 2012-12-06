package com.jetbrains.lang.dart.ide.settings;

import com.intellij.lang.javascript.library.JSLibraryManager;
import com.intellij.lang.javascript.library.JSLibraryMappings;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webcore.ScriptingFrameworkDescriptor;
import com.intellij.webcore.libraries.ScriptingLibraryModel;
import com.intellij.webcore.libraries.ui.ScriptingContextsConfigurable;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.util.DartSdkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartSettingsUI {
  private JPanel myMainPanel;
  private TextFieldWithBrowseButton myPathChooser;
  private JLabel mySetupLabel;
  private HyperlinkLabel mySetupScopeLabel;
  private final Project myProject;

  public DartSettingsUI(final Project project) {
    myProject = project;
    myPathChooser.getButton().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        final VirtualFile file = FileChooser.chooseFile(descriptor, myMainPanel, null, null);
        if (file != null && getExecutablePathByFolderPath(file.getPath(), "dart") == null) {
          Messages.showOkCancelDialog(
            myProject,
            DartBundle.message("dart.sdk.bad.home.path.to.dartvm"), DartBundle.message("dart.sdk.name"),
            icons.DartIcons.Dart_16
          );
        }
        else if (file != null && getExecutablePathByFolderPath(file.getPath(), "dart") != null) {
          myPathChooser.setText(FileUtil.toSystemIndependentName(file.getPath()));
          updateUI();
          if (!SystemInfo.isWindows && getSettings().getAnalyzer() == null) {
            Messages.showErrorDialog(
              myProject,
              DartBundle.message("dart.sdk.bad.analyzer.path", getSettings().getAnalyzerPath()),
              DartBundle.message("dart.warning")
            );
          }
        }
      }
    });
    mySetupScopeLabel.addHyperlinkListener(new HyperlinkListener() {
      @Override
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && isDartSDKConfigured()) {
          JSLibraryManager libraryManager = ServiceManager.getService(myProject, JSLibraryManager.class);
          final JSLibraryMappings mappings = ServiceManager.getService(project, JSLibraryMappings.class);
          ShowSettingsUtil.getInstance().editConfigurable(
            project,
            new ScriptingContextsConfigurable(project, mappings)
          );
          AccessToken writeToken = ApplicationManager.getApplication().acquireWriteActionLock(getClass());
          libraryManager.commitChanges();
          writeToken.finish();
        }
        else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && !isDartSDKConfigured() && isDartSDKPathValid()) {
          updateOrCreateDartLibrary();
          updateUI();
        }
      }
    });
  }

  private boolean isDartSDKConfigured() {
    final JSLibraryMappings mappings = ServiceManager.getService(myProject, JSLibraryMappings.class);
    return ContainerUtil.exists(mappings.getSingleLibraries(), new Condition<ScriptingLibraryModel>() {
      @Override
      public boolean value(ScriptingLibraryModel model) {
        return DartBundle.message("dart.sdk.name").equals(model.getName());
      }
    });
  }

  private void updateUI() {
    if (!isDartSDKConfigured()) {
      mySetupLabel.setText(DartBundle.message("dart.sdk.not.configured"));
      mySetupScopeLabel.setHyperlinkText(DartBundle.message("dart.sdk.configure"));
      return;
    }
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      @Override
      public void run() {
        final String executable = getExecutablePathByFolderPath(FileUtil.toSystemIndependentName(myPathChooser.getText()), "dart");
        if (executable == null) {
          // bad
          mySetupLabel.setText(DartBundle.message("dart.sdk.bad.path"));
        }
        else {
          mySetupLabel
            .setText(
              DartBundle.message("dart.sdk.setup", DartSdkUtil.getSdkVersion(FileUtil.toSystemDependentName(myPathChooser.getText()))));
          mySetupScopeLabel.setHyperlinkText(DartBundle.message("dart.sdk.edit.usage.scope"));
        }
      }
    });
  }

  public void updateOrCreateDartLibrary() {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        JSLibraryManager libraryManager = ServiceManager.getService(myProject, JSLibraryManager.class);
        final File rootDir = new File(FileUtil.toSystemDependentName(myPathChooser.getText()));
        final List<File> dartFiles = findDartFiles(rootDir);
        final List<VirtualFile> vFiles = new ArrayList<VirtualFile>();
        for (File file : dartFiles) {
          VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(file);
          if (vf == null) {
            vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
          }
          if (vf != null) {
            vFiles.add(vf);
          }
        }
        ScriptingLibraryModel libraryModel = libraryManager.getLibraryByName(DartBundle.message("dart.sdk.name"));
        if (libraryModel != null) {
          libraryManager.removeLibrary(libraryModel);
        }
        libraryModel = libraryManager.createLibrary(
          DartBundle.message("dart.sdk.name"),
          VfsUtilCore.toVirtualFileArray(vFiles),
          VirtualFile.EMPTY_ARRAY,
          ArrayUtil.EMPTY_STRING_ARRAY,
          ScriptingLibraryModel.LibraryLevel.GLOBAL,
          false
        );
        libraryModel.setFrameworkDescriptor(new ScriptingFrameworkDescriptor(
          DartBundle.message("dart.sdk.name"),
          DartSdkUtil.getSdkVersion(FileUtil.toSystemDependentName(myPathChooser.getText())))
        );
        libraryManager.commitChanges();
      }
    });
  }

  private static List<File> findDartFiles(@NotNull File rootDir) {
    final File libRoot = new File(rootDir, "lib");
    if (!libRoot.exists()) {
      return Collections.emptyList();
    }
    final List<File> result = new ArrayList<File>();
    final Processor<File> fileProcessor = new Processor<File>() {
      @Override
      public boolean process(File file) {
        if (file.isFile() && file.getName().endsWith("" + DartFileType.DEFAULT_EXTENSION)) {
          result.add(file);
        }
        return true;
      }
    };
    for (File child : libRoot.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return !"html".equals(file.getName()) && !"_internal".equals(file.getName());
      }
    })) {
      FileUtil.processFilesRecursively(child, fileProcessor);
    }

    File htmlDartium = new File(new File(libRoot, "html"), "dartium");
    if (htmlDartium.exists()) {
      FileUtil.processFilesRecursively(htmlDartium, fileProcessor);
    }

    return result;
  }

  @Nullable
  private static String getExecutablePathByFolderPath(String folderPath, String name) {
    final String folderUrl = VfsUtilCore.pathToUrl(folderPath);
    final String candidate = folderUrl + "/bin/" + getExecutableName(name);
    if (fileExists(candidate)) {
      return FileUtil.toSystemIndependentName(VfsUtilCore.urlToPath(candidate));
    }
    return null;
  }

  private static String getExecutableName(String name) {
    if (SystemInfo.isWindows) {
      return name + ".exe";
    }
    return name;
  }

  private static boolean fileExists(@Nullable String filePath) {
    return filePath != null && checkFileExists(VirtualFileManager.getInstance().findFileByUrl(filePath));
  }

  private static boolean checkFileExists(@Nullable VirtualFile file) {
    return file != null && file.exists();
  }

  public JPanel getMainPanel() {
    return myMainPanel;
  }

  public DartSettings getSettings() {
    if (!isDartSDKPathValid()) {
      return DartSettingsUtil.getSettings();
    }
    return new DartSettings(FileUtil.toSystemIndependentName(myPathChooser.getText()));
  }

  private boolean isDartSDKPathValid() {
    return getExecutablePathByFolderPath(FileUtil.toSystemIndependentName(myPathChooser.getText()), "dart") != null;
  }

  public void setSettings(DartSettings settings) {
    myPathChooser.setText(FileUtil.toSystemDependentName(settings.getSdkPath()));
    updateUI();
  }
}
