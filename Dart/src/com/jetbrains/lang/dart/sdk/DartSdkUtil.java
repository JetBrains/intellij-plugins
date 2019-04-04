// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.sdk;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ArrayUtil;
import com.intellij.util.BooleanFunction;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DartSdkUtil {
  private static final Map<Pair<File, Long>, String> ourVersions = new HashMap<>();
  private static final String DART_SDK_KNOWN_PATHS = "DART_SDK_KNOWN_PATHS";

  @Nullable
  public static String getSdkVersion(final @NotNull String sdkHomePath) {
    final File versionFile = new File(sdkHomePath + "/version");
    if (versionFile.isFile()) {
      final String cachedVersion = ourVersions.get(Pair.create(versionFile, versionFile.lastModified()));
      if (cachedVersion != null) return cachedVersion;
    }

    final String version = readVersionFile(sdkHomePath);
    if (version != null) {
      ourVersions.put(Pair.create(versionFile, versionFile.lastModified()), version);
      return version;
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

  @Contract("null->false")
  public static boolean isDartSdkHome(@Nullable final String path) {
    return path != null && !path.isEmpty() && new File(path + "/lib/core/core.dart").isFile();
  }

  public static void initDartSdkControls(final @Nullable Project project,
                                         final @NotNull ComboboxWithBrowseButton dartSdkPathComponent,
                                         final @NotNull JBLabel versionLabel) {
    dartSdkPathComponent.getComboBox().setEditable(true);
    addKnownPathsToCombo(dartSdkPathComponent.getComboBox(), DART_SDK_KNOWN_PATHS, DartSdkUtil::isDartSdkHome);
    if (SystemInfo.isMac && getItemFromCombo(dartSdkPathComponent.getComboBox()).isEmpty()) {
      // no need to check folder presence here; even if it doesn't exist - that's the best we can suggest
      dartSdkPathComponent.getComboBox().getEditor().setItem("/usr/local/opt/dart/libexec");
    }

    final String sdkHomePath = getItemFromCombo(dartSdkPathComponent.getComboBox());
    versionLabel.setText(sdkHomePath.isEmpty() ? "" : getSdkVersion(sdkHomePath));

    final TextComponentAccessor<JComboBox> textComponentAccessor = new TextComponentAccessor<JComboBox>() {
      @Override
      public String getText(final JComboBox component) {
        return getItemFromCombo(component);
      }

      @Override
      public void setText(@NotNull final JComboBox component, @NotNull final String text) {
        if (!text.isEmpty() && !isDartSdkHome(text)) {
          final String probablySdkPath = text + "/dart-sdk";
          if (isDartSdkHome(probablySdkPath)) {
            component.getEditor().setItem(FileUtilRt.toSystemDependentName(probablySdkPath));
            return;
          }
        }

        component.getEditor().setItem(FileUtilRt.toSystemDependentName(text));
      }
    };

    final ComponentWithBrowseButton.BrowseFolderActionListener<JComboBox> browseFolderListener =
      new ComponentWithBrowseButton.BrowseFolderActionListener<>("Select Dart SDK Path", null, dartSdkPathComponent, project,
                                                                 FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                                                                 textComponentAccessor);
    dartSdkPathComponent.addActionListener(browseFolderListener);

    final JTextComponent editorComponent = (JTextComponent)dartSdkPathComponent.getComboBox().getEditor().getEditorComponent();
    editorComponent.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull final DocumentEvent e) {
        final String sdkHomePath = getItemFromCombo(dartSdkPathComponent.getComboBox());
        versionLabel.setText(sdkHomePath.isEmpty() ? "" : getSdkVersion(sdkHomePath));
      }
    });
  }

  @NotNull
  private static String getItemFromCombo(@NotNull final JComboBox combo) {
    return combo.getEditor().getItem().toString().trim();
  }

  @Nullable
  public static String getFirstKnownDartSdkPath() {
    final String[] knownPaths = PropertiesComponent.getInstance().getValues(DART_SDK_KNOWN_PATHS);
    if (knownPaths != null && knownPaths.length > 0 && isDartSdkHome(knownPaths[0])) {
      return knownPaths[0];
    }
    return null;
  }

  private static void addKnownPathsToCombo(@NotNull final JComboBox combo,
                                           @NotNull final String propertyKey,
                                           @NotNull final BooleanFunction<String> pathChecker) {
    final SmartList<String> validPathsForUI = new SmartList<>();

    final String currentPath = getItemFromCombo(combo);
    if (!currentPath.isEmpty()) {
      validPathsForUI.add(currentPath);
    }

    final String[] knownPaths = PropertiesComponent.getInstance().getValues(propertyKey);
    if (knownPaths != null && knownPaths.length > 0) {
      for (String path: knownPaths) {
        final String pathSD = FileUtil.toSystemDependentName(path);
        if (!pathSD.equals(currentPath) && pathChecker.fun(path)) {
          validPathsForUI.add(pathSD);
        }
      }
    }

    combo.setModel(new DefaultComboBoxModel<>(ArrayUtil.toStringArray(validPathsForUI)));
  }

  public static void updateKnownSdkPaths(@NotNull final Project project, @NotNull final String newSdkPath) {
    final DartSdk oldSdk = DartSdk.getDartSdk(project);
    updateKnownPaths(DART_SDK_KNOWN_PATHS, oldSdk == null ? null : oldSdk.getHomePath(), newSdkPath);
  }

  private static void updateKnownPaths(@NotNull final String propertyKey, @Nullable final String oldPath, @NotNull final String newPath) {
    final List<String> knownPaths = new ArrayList<>();

    final String[] oldKnownPaths = PropertiesComponent.getInstance().getValues(propertyKey);
    if (oldKnownPaths != null) {
      knownPaths.addAll(Arrays.asList(oldKnownPaths));
    }

    if (oldPath != null) {
      knownPaths.remove(oldPath);
      knownPaths.add(0, oldPath);
    }

    knownPaths.remove(newPath);
    knownPaths.add(0, newPath);

    if (knownPaths.isEmpty()) {
      PropertiesComponent.getInstance().unsetValue(propertyKey);
    }
    else {
      PropertiesComponent.getInstance().setValues(propertyKey, ArrayUtil.toStringArray(knownPaths));
    }
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
