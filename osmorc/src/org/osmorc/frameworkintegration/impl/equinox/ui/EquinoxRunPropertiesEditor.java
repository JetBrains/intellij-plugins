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
package org.osmorc.frameworkintegration.impl.equinox.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.text.StringUtil;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import org.osmorc.frameworkintegration.impl.equinox.EquinoxRunProperties;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.FrameworkRunPropertiesEditor;
import org.osmorc.run.ui.GenericRunPropertiesEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class EquinoxRunPropertiesEditor implements FrameworkRunPropertiesEditor {
  private JPanel myMainPanel;
  private GenericRunPropertiesEditor myGenericRunPropertiesEditor;
  private JRadioButton myJustTheBundlesRadioButton;
  private JRadioButton myProductRadioButton;
  private JTextField myProductTextField;
  private JRadioButton myApplicationRadioButton;
  private JTextField myApplicationTextField;
  private PresentationModel<EquinoxRunProperties> myPresentationModel;

  public EquinoxRunPropertiesEditor() {
    myJustTheBundlesRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updatePresentation();
      }
    });
    myProductRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updatePresentation();
      }
    });
    myApplicationRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updatePresentation();
      }
    });
  }

  private void createUIComponents() {
    EquinoxRunProperties runProperties = new EquinoxRunProperties(new HashMap<String, String>());
    myPresentationModel = new PresentationModel<EquinoxRunProperties>(runProperties);
    myGenericRunPropertiesEditor = new GenericRunPropertiesEditor<EquinoxRunProperties>(runProperties);
    myProductTextField = BasicComponentFactory.createTextField(myPresentationModel.getModel(EquinoxRunProperties.EQUINOX_PRODUCT));
    myApplicationTextField = BasicComponentFactory.createTextField(myPresentationModel.getModel(EquinoxRunProperties.EQUINOX_APPLICATION));
  }

  private void updatePresentation() {
    if (myJustTheBundlesRadioButton.isSelected()) {
      myProductTextField.setText("");
      myProductTextField.setEnabled(false);
      myPresentationModel.getBean().setEquinoxProduct("");
      myApplicationTextField.setText("");
      myApplicationTextField.setEnabled(false);
      myPresentationModel.getBean().setEquinoxApplication("");
    }
    else if (myProductRadioButton.isSelected()) {
      myApplicationTextField.setText("");
      myApplicationTextField.setEnabled(false);
      myPresentationModel.getBean().setEquinoxApplication("");
      myProductTextField.setEnabled(true);
    }
    else if (myApplicationRadioButton.isSelected()) {
      myProductTextField.setText("");
      myProductTextField.setEnabled(false);
      myPresentationModel.getBean().setEquinoxProduct("");
      myApplicationTextField.setEnabled(true);
    }
  }

  @Override
  public JPanel getUI() {
    return myMainPanel;
  }

  @Override
  public void resetEditorFrom(OsgiRunConfiguration osgiRunConfiguration) {
    myGenericRunPropertiesEditor.resetEditorFrom(osgiRunConfiguration);

    myPresentationModel.getBean().load(osgiRunConfiguration.getAdditionalProperties());
    if (!StringUtil.isEmptyOrSpaces(myPresentationModel.getBean().getEquinoxProduct())) {
      myProductRadioButton.setSelected(true);
    }
    else if (!StringUtil.isEmptyOrSpaces(myPresentationModel.getBean().getEquinoxApplication())) {
      myApplicationRadioButton.setSelected(true);
    }
    else {
      myJustTheBundlesRadioButton.setSelected(true);
    }

    updatePresentation();
  }

  @Override
  public void applyEditorTo(OsgiRunConfiguration osgiRunConfiguration) throws ConfigurationException {
    myGenericRunPropertiesEditor.applyEditorTo(osgiRunConfiguration);
    osgiRunConfiguration.putAdditionalProperties(myPresentationModel.getBean().getProperties());
  }
}
