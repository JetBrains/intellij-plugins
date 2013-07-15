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
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webcore.libraries.ScriptingLibraryMappings;
import com.intellij.webcore.libraries.ScriptingLibraryModel;
import com.intellij.webcore.libraries.ui.ScriptingContextsConfigurable;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.util.DartSdkUtil;
import icons.DartIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author: Fedor.Korotkov
 */
public class DartSettingsUI {
  private JPanel myMainPanel;
  private TextFieldWithBrowseButton myPathChooser;
  private JLabel mySetupLabel;
  private HyperlinkLabel mySetupScopeLabel;
  private JCheckBox myDartSdkEnabled;
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
            DartIcons.Dart_16
          );
        }
        else if (file != null && getExecutablePathByFolderPath(file.getPath(), "dart") != null) {
          myPathChooser.setText(FileUtil.toSystemDependentName(file.getPath()));
          updateUI();
          if (!SystemInfo.isWindows && getSettings().getAnalyzer() == null) {
            Messages.showErrorDialog(
              myProject,
              DartBundle.message("dart.sdk.bad.analyzer.path", getSettings().getAnalyzerUrl()),
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
        else if (!isDartSDKPathValid()) {
          Messages.showOkCancelDialog(
            myProject,
            DartBundle.message("invalid.dart.sdk.path"), DartBundle.message("dart.sdk.name"),
            DartIcons.Dart_16
          );
        }
      }
    });
    myDartSdkEnabled.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final JSLibraryManager libraryManager = JSLibraryManager.getInstance(myProject);
        final ScriptingLibraryMappings libraryMappings = libraryManager.getLibraryMappings();
        final String libName = DartBundle.message("dart.sdk.name");
        if (libraryMappings.isAssociatedWithProject(libName)) {
          libraryMappings.disassociateWithProject(libName);
        }
        else if (libraryManager.getLibraryByName(libName) != null) {
          libraryMappings.associate(null, libName, false);
        }
        AccessToken writeToken = ApplicationManager.getApplication().acquireWriteActionLock(getClass());
        libraryManager.commitChanges();
        writeToken.finish();
        updateUI();
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
    final boolean sdkConfigured = isDartSDKConfigured();
    myDartSdkEnabled.setEnabled(sdkConfigured);
    final JSLibraryManager libraryManager = JSLibraryManager.getInstance(myProject);
    myDartSdkEnabled.setSelected(libraryManager.getLibraryMappings().isAssociatedWithProject(DartBundle.message("dart.sdk.name")));
    if (!sdkConfigured) {
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
          mySetupLabel.setText(
            DartBundle.message("dart.sdk.setup", DartSdkUtil.getSdkVersion(FileUtil.toSystemDependentName(myPathChooser.getText())))
          );
          mySetupScopeLabel.setHyperlinkText(DartBundle.message("dart.sdk.edit.usage.scope"));
        }
      }
    });
  }

  public void updateOrCreateDartLibrary() {
    DartSettings.setUpDartLibrary(myProject, myPathChooser.getText());
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
