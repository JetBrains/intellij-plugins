// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.sdk;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;

public class FlexmojosSdkDataConfigurable implements AdditionalDataConfigurable {
  private Sdk mySdk;
  private final FlexmojosSdkForm myFlexmojosSdkForm;

  public FlexmojosSdkDataConfigurable() {
    myFlexmojosSdkForm = new FlexmojosSdkForm();
  }

  @Override
  public void setSdk(final Sdk sdk) {
    mySdk = sdk;
  }

  @Override
  public JComponent createComponent() {
    return myFlexmojosSdkForm.getMainPanel();
  }

  @Override
  public boolean isModified() {
    final FlexmojosSdkAdditionalData data = (FlexmojosSdkAdditionalData)mySdk.getSdkAdditionalData();
    if (data != null) {
      if (!myFlexmojosSdkForm.getAdlPath().equals(data.getAdlPath())) return true;
      if (!myFlexmojosSdkForm.getAirRuntimePath().equals(data.getAirRuntimePath())) return true;
    }
    return false;
  }

  @Override
  public void apply() throws ConfigurationException {
    final FlexmojosSdkAdditionalData data = (FlexmojosSdkAdditionalData)mySdk.getSdkAdditionalData();
    if (data != null) {
      data.setAdlPath(myFlexmojosSdkForm.getAdlPath());
      data.setAirRuntimePath(myFlexmojosSdkForm.getAirRuntimePath());
    }
  }

  @Override
  public void reset() {
    final FlexmojosSdkAdditionalData data = (FlexmojosSdkAdditionalData)mySdk.getSdkAdditionalData();
    myFlexmojosSdkForm.setFlexCompilerClasspath(data == null ? Collections.emptyList() : data.getFlexCompilerClasspath());
    myFlexmojosSdkForm.setAdlPath(data == null ? "" : data.getAdlPath());
    myFlexmojosSdkForm.setAirRuntimePath(data == null ? "" : data.getAirRuntimePath());
  }

  @Override
  public void disposeUIResources() {
  }

  private static final class FlexmojosSdkForm {
    private JComponent myMainPanel;
    private JTextArea myClasspathTextArea;
    private LabeledComponent<TextFieldWithBrowseButton> myAdlComponent;
    private LabeledComponent<TextFieldWithBrowseButton> myAirRuntimeComponent;

    private FlexmojosSdkForm() {
      initAdlChooser();
      initAirRuntimeChooser();
    }

    private void initAdlChooser() {
      final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
        @Override
        public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
          return super.isFileVisible(file, showHiddenFiles) &&
                 (file.isDirectory() || (file.getName().startsWith("adl") && isExecutableExtension(file.getExtension())));
        }

        private boolean isExecutableExtension(final String extension) {
          return SystemInfo.isWindows ? "exe".equalsIgnoreCase(extension) : extension == null || "uexe".equalsIgnoreCase(extension);
        }
      };

      myAdlComponent.getComponent()
        .addBrowseFolderListener("Select ADL executable file", null, null, descriptor, new TextComponentAccessor<>() {
          @Override
          public void setText(final JTextField component, @NotNull final String text) {
            component.setText(text);
            final String adlPath = FileUtil.toSystemDependentName(text);
            if (adlPath.endsWith(FlexSdkUtils.ADL_RELATIVE_PATH)) {
              final String runtimePath =
                adlPath.substring(0, adlPath.length() - FlexSdkUtils.ADL_RELATIVE_PATH.length()) + FlexSdkUtils.AIR_RUNTIME_RELATIVE_PATH;
              myAirRuntimeComponent.getComponent().setText(runtimePath);
            }
          }

          @Override
          public String getText(final JTextField component) {
            return component.getText();
          }
        });
    }

    private void initAirRuntimeChooser() {
      final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, true, false, false, false) {
        @Override
        public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
          return super.isFileVisible(file, showHiddenFiles) && (file.isDirectory() || ("zip".equalsIgnoreCase(file.getExtension())));
        }
      };
      myAirRuntimeComponent.getComponent().addBrowseFolderListener("Select AIR Runtime",
                                                                   "Select AIR Runtime as a directory like <Flex SDK>/runtimes/AIR/win/ or as a .zip file",
                                                                   null, descriptor);
    }

    JComponent getMainPanel() {
      return myMainPanel;
    }

    void setFlexCompilerClasspath(final Collection<String> classpathEntries) {
      myClasspathTextArea.setText(StringUtil.join(classpathEntries, s -> FileUtil.toSystemDependentName(s), "\n"));
    }

    void setAdlPath(final String adlPath) {
      myAdlComponent.getComponent().setText(FileUtil.toSystemDependentName(adlPath));
    }

    String getAdlPath() {
      return FileUtil.toSystemIndependentName(myAdlComponent.getComponent().getText().trim());
    }

    void setAirRuntimePath(final String airRuntimePath) {
      myAirRuntimeComponent.getComponent().setText(FileUtil.toSystemDependentName(airRuntimePath));
    }

    String getAirRuntimePath() {
      return FileUtil.toSystemIndependentName(myAirRuntimeComponent.getComponent().getText().trim());
    }
  }
}
