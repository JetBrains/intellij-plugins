package com.jetbrains.lang.dart.sdk;

import com.intellij.ide.browsers.chrome.ChromeSettings;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DartSdkUtil {
  private static final Map<Pair<File, Long>, String> ourVersions = new HashMap<Pair<File, Long>, String>();

  @Nullable
  public static String getSdkVersion(final @NotNull String sdkHomePath) {
    final File versionFile = new File(sdkHomePath + "/version");
    if (versionFile.isFile()) {
      final String cachedVersion = ourVersions.get(Pair.create(versionFile, versionFile.lastModified()));
      if (cachedVersion != null) return cachedVersion;
    }

    final String version = readVersionFile(sdkHomePath);
    if (version != null) {
      final String revision = readSdkRevision(sdkHomePath);
      final String versionWithRevision = revision == null || version.endsWith(revision) ? version : version + "_r" + revision;
      ourVersions.put(Pair.create(versionFile, versionFile.lastModified()), versionWithRevision);
      return versionWithRevision;
    }

    return null;
  }

  private static String readVersionFile(final String sdkHomePath) {
    final File versionFile = new File(sdkHomePath + "/version");
    if (versionFile.isFile() && versionFile.length() < 100) {
      try {
        return FileUtil.loadFile(versionFile).trim();
      }
      catch (IOException e) {
        /* ignore */
      }
    }
    return null;
  }

  @Nullable
  static String readSdkRevision(final @NotNull String sdkHomePath) {
    final File revisionFile = new File(sdkHomePath + "/revision");
    if (revisionFile.isFile() && revisionFile.length() < 100) {
      try {
        return FileUtil.loadFile(revisionFile).trim();
      }
      catch (IOException ignore) {/* unlucky */}
    }
    return null;
  }

  @Contract("null->false")
  public static boolean isDartSdkHome(final String path) {
    return path != null && !path.isEmpty() && new File(path + "/lib/core/core.dart").isFile();
  }

  public static void initDartSdkAndDartiumControls(final @Nullable Project project,
                                                   final @NotNull TextFieldWithBrowseButton dartSdkPathComponent,
                                                   final @NotNull JBLabel versionLabel,
                                                   final @NotNull TextFieldWithBrowseButton dartiumPathComponent,
                                                   final @NotNull Computable<ChromeSettings> currentDartiumSettingsRetriever,
                                                   final @NotNull JButton dartiumSettingsButton,
                                                   final @NotNull JBCheckBox checkedModeCheckBox,
                                                   final @NotNull Computable<Boolean> isResettingControlsComputable) {
    final String sdkHomePath = dartSdkPathComponent.getText().trim();
    versionLabel.setText(sdkHomePath.isEmpty() ? "" : getSdkVersion(sdkHomePath));

    final TextComponentAccessor<JTextField> textComponentAccessor = new TextComponentAccessor<JTextField>() {
      @Override
      public String getText(final JTextField component) {
        return component.getText();
      }

      @Override
      public void setText(final JTextField component, @NotNull String text) {
        if (!text.isEmpty() && !isDartSdkHome(text)) {
          final String probablySdkPath = text + "/dart-sdk";
          if (isDartSdkHome(probablySdkPath)) {
            component.setText(FileUtilRt.toSystemDependentName(probablySdkPath));
            return;
          }
        }

        component.setText(FileUtilRt.toSystemDependentName(text));
      }
    };

    final ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> browseFolderListener =
      new ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>("Select Dart SDK path", null, dartSdkPathComponent, project,
                                                                           FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                                                                           textComponentAccessor);
    dartSdkPathComponent.addBrowseFolderListener(project, browseFolderListener);

    dartiumPathComponent.addBrowseFolderListener("Select Dartium browser path", null, project,
                                                 FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor());

    dartSdkPathComponent.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(final DocumentEvent e) {
        final String sdkHomePath = dartSdkPathComponent.getText().trim();
        versionLabel.setText(sdkHomePath.isEmpty() ? "" : getSdkVersion(sdkHomePath));

        if (!isResettingControlsComputable.compute() && isDartSdkHome(sdkHomePath)) {
          final String dartiumPath = DartiumUtil.getDartiumPathForSdk(sdkHomePath);
          if (dartiumPath != null) {
            dartiumPathComponent.setText(FileUtilRt.toSystemDependentName(dartiumPath));
          }
        }
      }
    });

    dartiumSettingsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        ShowSettingsUtil.getInstance().editConfigurable(dartiumSettingsButton,
                                                        currentDartiumSettingsRetriever.compute().createConfigurable());
      }
    });

    checkedModeCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        DartiumUtil.setCheckedMode(currentDartiumSettingsRetriever.compute().getEnvironmentVariables(), checkedModeCheckBox.isSelected());
      }
    });
  }

  @Nullable
  public static String getErrorMessageIfWrongSdkRootPath(final @NotNull String sdkRootPath) {
    if (sdkRootPath.isEmpty()) return DartBundle.message("error.path.to.sdk.not.specified");

    final File sdkRoot = new File(sdkRootPath);
    if (!sdkRoot.isDirectory()) return DartBundle.message("error.folder.specified.as.sdk.not.exists");

    if (!isDartSdkHome(sdkRootPath)) return DartBundle.message("error.sdk.not.found.in.specified.location");

    return null;
  }

  public static String getDartExePath(final @NotNull DartSdk sdk) {
    return sdk.getHomePath() + (SystemInfo.isWindows ? "/bin/dart.exe" : "/bin/dart");
  }

  public static String getPubPath(final @NotNull DartSdk sdk) {
    return getPubPath(sdk.getHomePath());
  }

  public static String getPubPath(final @NotNull String sdkRoot) {
    return sdkRoot + (SystemInfo.isWindows ? "/bin/pub.bat" : "/bin/pub");
  }
}
