// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBLabel;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.server.DartRemoteDebugConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.DartRemoteDebugParameters;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public class DartRemoteDebugConfigurationEditor extends SettingsEditor<DartRemoteDebugConfiguration> {

  private JPanel myMainPanel;
  private ComboboxWithBrowseButton myDartProjectCombo;
  private JBLabel myHintLabel;

  private final SortedSet<NameAndPath> myComboItems = new TreeSet<>();

  public DartRemoteDebugConfigurationEditor(@NotNull final Project project) {
    initDartProjectsCombo(project);
    myHintLabel.setCopyable(true);
  }

  private void initDartProjectsCombo(@NotNull final Project project) {
    myDartProjectCombo.getComboBox().setRenderer(SimpleListCellRenderer.create("", NameAndPath::getPresentableText));

    if (!project.isDefault()) {
      for (VirtualFile pubspecFile : FilenameIndex.getVirtualFilesByName(PUBSPEC_YAML, GlobalSearchScope.projectScope(project))) {
        myComboItems.add(new NameAndPath(PubspecYamlUtil.getDartProjectName(pubspecFile), pubspecFile.getParent().getPath()));
      }

      if (myComboItems.isEmpty()) {
        for (VirtualFile contentRoot : ProjectRootManager.getInstance(project).getContentRoots()) {
          if (FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, GlobalSearchScopesCore.directoryScope(project, contentRoot, true))) {
            myComboItems.add(new NameAndPath(null, contentRoot.getPath()));
          }
        }
      }
    }

    myDartProjectCombo.getComboBox().setModel(new DefaultComboBoxModel<>(myComboItems.toArray()));

    myDartProjectCombo.addBrowseFolderListener(null, null, project, FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                                               new TextComponentAccessor<>() {
                                                 @Override
                                                 public String getText(final JComboBox combo) {
                                                   final Object item = combo.getSelectedItem();
                                                   return item instanceof NameAndPath ? ((NameAndPath)item).myPath : "";
                                                 }

                                                 @Override
                                                 public void setText(final JComboBox combo, @NotNull final String path) {
                                                   setSelectedProjectPath(FileUtil.toSystemIndependentName(path));
                                                 }
                                               });
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myMainPanel;
  }

  @Override
  protected void resetEditorFrom(@NotNull final DartRemoteDebugConfiguration config) {
    final DartRemoteDebugParameters params = config.getParameters();
    setSelectedProjectPath(params.getDartProjectPath());
  }

  private void setSelectedProjectPath(@NotNull final String projectPath) {
    if (projectPath.isEmpty()) return;

    final VirtualFile pubspecFile = LocalFileSystem.getInstance().findFileByPath(projectPath + "/" + PUBSPEC_YAML);
    final String projectName = pubspecFile == null ? null : PubspecYamlUtil.getDartProjectName(pubspecFile);
    final NameAndPath item = new NameAndPath(projectName, projectPath);

    if (!myComboItems.contains(item)) {
      myComboItems.add(item);
      myDartProjectCombo.getComboBox().setModel(new DefaultComboBoxModel(myComboItems.toArray()));
    }

    myDartProjectCombo.getComboBox().setSelectedItem(item);
  }

  @Override
  protected void applyEditorTo(@NotNull final DartRemoteDebugConfiguration config) {
    final DartRemoteDebugParameters params = config.getParameters();
    final Object selectedItem = myDartProjectCombo.getComboBox().getSelectedItem();
    params.setDartProjectPath(selectedItem instanceof NameAndPath ? ((NameAndPath)selectedItem).myPath : "");
  }

  private static class NameAndPath implements Comparable<NameAndPath> {
    @Nullable private final String myName;
    @NotNull private final String myPath;

    NameAndPath(@Nullable final String name, @NotNull final String path) {
      myName = name;
      myPath = path;
    }

    public String getPresentableText() {
      return myName == null ? FileUtil.toSystemDependentName(myPath) : myName + " (" + FileUtil.toSystemDependentName(myPath) + ")";
    }

    @Override
    public String toString() {
      return getPresentableText();
    }

    @Override
    public boolean equals(final Object o) {
      return (o instanceof NameAndPath) && myPath.equals(((NameAndPath)o).myPath);
    }

    @Override
    public int hashCode() {
      return myPath.hashCode();
    }

    @Override
    public int compareTo(final NameAndPath o) {
      return myPath.compareTo(o.myPath); // root project goes first, before its subprojects
    }
  }
}
