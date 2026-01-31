/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkInstanceManager;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;
import org.osmorc.frameworkintegration.impl.AbstractFrameworkInstanceManager;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

/**
 * Dialog for creating/updating a framework instance.
 *
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class CreateFrameworkInstanceDialog extends DialogWrapper {
  private JPanel myMainPanel;
  private TextFieldWithBrowseButton myBaseFolderChooser;
  private JTextField myNameField;
  private JBLabel myVersionLabel;

  private final FrameworkInstanceDefinition myInstance;
  private final DefaultListModel<FrameworkInstanceDefinition> myModel;
  private final FrameworkIntegrator myIntegrator;

  public CreateFrameworkInstanceDialog(@NotNull FrameworkInstanceDefinition instance, @NotNull DefaultListModel<FrameworkInstanceDefinition> model) {
    super(true);

    myInstance = instance;
    myModel = model;
    myIntegrator = FrameworkIntegratorRegistry.getInstance().findIntegratorByInstanceDefinition(instance);
    assert myIntegrator != null : instance;

    setTitle(OsmorcBundle.message("framework.edit.title", myIntegrator.getDisplayName()));
    setModal(true);

    myBaseFolderChooser.addBrowseFolderListener(null, FileChooserDescriptorFactory.createSingleFolderDescriptor()
      .withTitle(OsmorcBundle.message("framework.path.chooser.title"))
      .withDescription(OsmorcBundle.message("framework.path.chooser.description", myIntegrator.getDisplayName())));
    myBaseFolderChooser.getTextField().setEditable(false);
    myBaseFolderChooser.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull DocumentEvent e) {
        checkInstance(); updateVersion();
      }
    });

    myNameField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull DocumentEvent e) {
        checkInstance();
      }
    });

    myBaseFolderChooser.setText(ObjectUtils.notNull(instance.getBaseFolder(), ""));
    myNameField.setText(ObjectUtils.notNull(instance.getName(), ""));
    myVersionLabel.setText(ObjectUtils.notNull(instance.getVersion(), ""));

    init();

    checkInstance();
  }

  private void checkInstance() {
    FrameworkInstanceDefinition newInstance = createDefinition();
    String message = myIntegrator.getFrameworkInstanceManager().checkValidity(newInstance);

    if (message == null) {
      for (int i = 0; i < myModel.size(); i++) {
        FrameworkInstanceDefinition instance = myModel.get(i);
        if (newInstance.equals(instance) && instance != myInstance) {
          message = OsmorcBundle.message("framework.name.duplicate");
        }
      }
    }

    setErrorText(message);
    setOKActionEnabled(message == null);
  }

  private void updateVersion() {
    String version = null;
    FrameworkInstanceManager manager = myIntegrator.getFrameworkInstanceManager();
    if (manager instanceof AbstractFrameworkInstanceManager) {
      version = ((AbstractFrameworkInstanceManager)manager).getVersion(createDefinition());
    }

    myVersionLabel.setText(ObjectUtils.notNull(version, ""));

    if (StringUtil.isEmptyOrSpaces(myNameField.getText())) {
      String name = myIntegrator.getDisplayName();
      if (version != null) {
        name = name + " (" + version + ")";
      }
      myNameField.setText(name);
    }
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myBaseFolderChooser.getButton();
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Override
  protected String getHelpId() {
    return "reference.settings.project.osgi.new.framework.instance";
  }

  public @NotNull FrameworkInstanceDefinition createDefinition() {
    FrameworkInstanceDefinition framework = new FrameworkInstanceDefinition();
    framework.setName(myNameField.getText().trim());
    framework.setFrameworkIntegratorName(myIntegrator.getDisplayName());
    framework.setBaseFolder(myBaseFolderChooser.getText().trim());
    framework.setVersion(myVersionLabel.getText().trim());
    return framework;
  }
}
