package com.jetbrains.lang.dart.ide.runner.client.ui;

import com.intellij.javascript.debugger.execution.LocalFileMappingTree;
import com.intellij.javascript.debugger.execution.RemoteJavaScriptDebugConfiguration;
import com.intellij.javascript.debugger.impl.JSDebugProcess;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.lang.dart.ide.runner.client.RemoteDartDebugConfiguration;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author nik
 */
public class RemoteDartConfigurationEditorForm extends SettingsEditor<RemoteDartDebugConfiguration> {
  private JPanel myMainPanel;
  private JTextField myUrlTextField;
  private JPanel myMappingTreePanel;
  private JLabel myBrowserLabel;
  private JComboBox myBrowserCombobox;
  private JPanel myLocalFilesMappingPanel;
  private final @Nullable LocalFileMappingTree myMappingTree;

  public RemoteDartConfigurationEditorForm(Project project) {
    myMappingTree = project.isDefault() ? null : new LocalFileMappingTree(project);
  }

  protected void resetEditorFrom(final RemoteDartDebugConfiguration configuration) {
    myUrlTextField.setText(configuration.getFileUrl());
    if (myMappingTree != null) {
      final Map<VirtualFile, String> map = new THashMap<VirtualFile, String>();
      final LocalFileSystem fileSystem = LocalFileSystem.getInstance();
      VirtualFile toSelect = null;
      for (RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean bean : configuration.getMappings()) {
        final VirtualFile file = fileSystem.findFileByPath(bean.getLocalFilePath());
        if (file != null) {
          map.put(file, bean.getRemoteUrl());
          if (toSelect == null || VfsUtilCore.isAncestor(file, toSelect, false)) {
            toSelect = file;
          }
        }
      }
      myMappingTree.reset(map);
      myMappingTree.select(toSelect);
    }
  }

  protected void applyEditorTo(final RemoteDartDebugConfiguration configuration) throws ConfigurationException {
    configuration.setFileUrl(myUrlTextField.getText());
    if (myMappingTree != null) {
      final List<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean> mappings = configuration.getMappings();
      mappings.clear();
      for (Map.Entry<VirtualFile, String> mapping : myMappingTree.getValues().entrySet()) {
        final String remote = mapping.getValue();
        if (remote.length() > 0) {
          final String local = mapping.getKey().getPath();
          mappings.add(new RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean(local, remote));
        }
      }

      final Collection<? extends JSDebugProcess> processes =
        XDebuggerManager.getInstance(myMappingTree.getProject()).getDebugProcesses(JSDebugProcess.class);
      for (JSDebugProcess process : processes) {
        if (process.getSession().getRunProfile() == configuration) {
          process.updateRemoteUrlMappings(mappings);
        }
      }
    }
  }

  protected JComboBox getBrowserCombobox() {
    return myBrowserCombobox;
  }

  protected JLabel getBrowserLabel() {
    return myBrowserLabel;
  }

  @NotNull
  protected JComponent createEditor() {
    if (myMappingTree != null) {
      myMappingTreePanel.add(ScrollPaneFactory.createScrollPane(myMappingTree), BorderLayout.CENTER);
    }
    else {
      myLocalFilesMappingPanel.setVisible(false);
    }
    return myMainPanel;
  }

  protected void disposeEditor() {
  }
}
