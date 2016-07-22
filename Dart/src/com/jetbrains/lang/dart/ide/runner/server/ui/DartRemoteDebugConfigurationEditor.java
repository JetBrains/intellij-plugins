package com.jetbrains.lang.dart.ide.runner.server.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.FixedSizeButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.ui.*;
import com.intellij.util.PlatformIcons;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.server.DartRemoteDebugConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.DartRemoteDebugParameters;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public class DartRemoteDebugConfigurationEditor extends SettingsEditor<DartRemoteDebugConfiguration> {

  private JPanel myMainPanel;
  private JTextArea myVMArgsArea;
  private FixedSizeButton myCopyButton;
  private JTextField myHostField;
  private PortField myPortField;
  private ComboboxWithBrowseButton myDartProjectCombo;

  @Nullable private final DartSdk mySdk;

  private SortedSet<NameAndPath> myComboItems = new TreeSet<>();

  public DartRemoteDebugConfigurationEditor(@NotNull final Project project) {
    mySdk = DartSdk.getDartSdk(project);

    myHostField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        updateVmArgs();
      }
    });

    myPortField.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent e) {
        updateVmArgs();
      }
    });

    initCopyToClipboardActions();
    initDartProjectsCombo(project);
  }

  public void initCopyToClipboardActions() {
    final DefaultActionGroup group = new DefaultActionGroup();
    group.add(new AnAction("Copy") {
      {
        copyFrom(ActionManager.getInstance().getAction(IdeActions.ACTION_COPY));
      }

      @Override
      public void actionPerformed(final AnActionEvent e) {
        CopyPasteManager.getInstance().setContents(new StringSelection(myVMArgsArea.getText().trim()));
      }
    });

    myVMArgsArea.addMouseListener(
      new PopupHandler() {
        @Override
        public void invokePopup(final Component comp, final int x, final int y) {
          ActionManager.getInstance().createActionPopupMenu(ActionPlaces.UNKNOWN, group).getComponent().show(comp, x, y);
        }
      }
    );

    myCopyButton.setSize(22);
    myCopyButton.setIcon(PlatformIcons.COPY_ICON);
    myCopyButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        CopyPasteManager.getInstance().setContents(new StringSelection(myVMArgsArea.getText().trim()));
      }
    });
  }

  private void initDartProjectsCombo(@NotNull final Project project) {
    myDartProjectCombo.getComboBox().setRenderer(new ListCellRendererWrapper<NameAndPath>() {
      @Override
      public void customize(final JList list,
                            final NameAndPath value,
                            final int index,
                            final boolean selected,
                            final boolean hasFocus) {
        if (value != null) {
          setText(value.getPresentableText());
        }
      }
    });

    for (VirtualFile pubspecFile : FilenameIndex.getVirtualFilesByName(project, PUBSPEC_YAML, GlobalSearchScope.projectScope(project))) {
      myComboItems.add(new NameAndPath(PubspecYamlUtil.getDartProjectName(pubspecFile), pubspecFile.getParent().getPath()));
    }

    if (myComboItems.isEmpty()) {
      for (VirtualFile contentRoot : ProjectRootManager.getInstance(project).getContentRoots()) {
        if (FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, GlobalSearchScopesCore.directoryScope(project, contentRoot, true))) {
          myComboItems.add(new NameAndPath(null, contentRoot.getPath()));
        }
      }
    }

    myDartProjectCombo.getComboBox().setModel(new DefaultComboBoxModel(myComboItems.toArray()));

    myDartProjectCombo.addBrowseFolderListener(null, null, project, FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                                               new TextComponentAccessor<JComboBox>() {
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

  private void updateVmArgs() {
    if (mySdk == null || StringUtil.compareVersionNumbers(mySdk.getVersion(), "1.14") >= 0) {
      final String host = myHostField.getText().trim();
      final boolean localhost = "localhost".equals(host) || "127.0.0.1".equals(host);
      myVMArgsArea.setText("--enable-vm-service:" + myPortField.getNumber() + (localhost ? "" : "/0.0.0.0") + " --pause_isolates_on_start");
    }
    else {
      myVMArgsArea.setText("--debug:" + myPortField.getNumber() + " --break-at-isolate-spawn");
    }
  }

  @Override
  protected void resetEditorFrom(@NotNull final DartRemoteDebugConfiguration config) {
    final DartRemoteDebugParameters params = config.getParameters();
    myHostField.setText(params.getHost());
    myPortField.setNumber(params.getPort());
    setSelectedProjectPath(params.getDartProjectPath());
    updateVmArgs();
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
  protected void applyEditorTo(@NotNull final DartRemoteDebugConfiguration config) throws ConfigurationException {
    final DartRemoteDebugParameters params = config.getParameters();
    params.setHost(myHostField.getText().trim());
    params.setPort(myPortField.getNumber());

    final Object selectedItem = myDartProjectCombo.getComboBox().getSelectedItem();
    params.setDartProjectPath(selectedItem instanceof NameAndPath ? ((NameAndPath)selectedItem).myPath : "");
  }

  private static class NameAndPath implements Comparable<NameAndPath> {
    @Nullable private final String myName;
    @NotNull private final String myPath;

    public NameAndPath(@Nullable final String name, @NotNull final String path) {
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
