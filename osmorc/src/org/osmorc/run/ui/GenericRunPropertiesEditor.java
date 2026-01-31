/*
 * Copyright (c) 2007-2010, Osmorc Development Team
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
package org.osmorc.run.ui;

import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.impl.GenericRunProperties;
import org.osmorc.run.OsgiRunConfiguration;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public class GenericRunPropertiesEditor implements FrameworkRunPropertiesEditor {
  private JPanel myMainPanel;
  private JTextField mySystemPackages;
  private JTextField myBootDelegation;
  private JCheckBox myDebugCheckbox;
  private JCheckBox myStartConsoleCheckbox;

  @Override
  public JPanel getUI() {
    return myMainPanel;
  }

  @Override
  public void resetEditorFrom(@NotNull OsgiRunConfiguration runConfiguration) {
    Map<String, String> properties = runConfiguration.getAdditionalProperties();
    mySystemPackages.setText(GenericRunProperties.getSystemPackages(properties));
    myBootDelegation.setText(GenericRunProperties.getBootDelegation(properties));
    myDebugCheckbox.setSelected(GenericRunProperties.isDebugMode(properties));
    myStartConsoleCheckbox.setSelected(GenericRunProperties.isStartConsole(properties));
  }

  @Override
  public void applyEditorTo(@NotNull OsgiRunConfiguration runConfiguration) throws ConfigurationException {
    Map<String, String> properties = new HashMap<>();
    GenericRunProperties.setSystemPackages(properties, mySystemPackages.getText());
    GenericRunProperties.setBootDelegation(properties, myBootDelegation.getText());
    GenericRunProperties.setDebugMode(properties, myDebugCheckbox.isSelected());
    GenericRunProperties.setStartConsole(properties, myStartConsoleCheckbox.isSelected());
    runConfiguration.putAdditionalProperties(properties);
  }
}
