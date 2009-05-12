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

package org.osmorc.frameworkintegration.impl.knopflerfish.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import org.osmorc.frameworkintegration.impl.knopflerfish.KnopflerfishRunProperties;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.FrameworkRunPropertiesEditor;

import javax.swing.*;
import java.util.HashMap;

/**
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class KnopflerfishRunPropertiesEditor implements FrameworkRunPropertiesEditor
{
  public KnopflerfishRunPropertiesEditor()
  {

  }

  public JPanel getUI()
  {
    return _mainPanel;
  }

  public void resetEditorFrom(OsgiRunConfiguration osgiRunConfiguration)
  {
    _presentationModel.getBean().load(osgiRunConfiguration.getAdditionalProperties());
  }

  public void applyEditorTo(OsgiRunConfiguration osgiRunConfiguration) throws ConfigurationException
  {
    osgiRunConfiguration.putAdditionalProperties(_presentationModel.getBean().getProperties());
  }

  private void createUIComponents()
  {
    _presentationModel = new PresentationModel<KnopflerfishRunProperties>(
        new KnopflerfishRunProperties(new HashMap<String, String>()));
    _debugCheckbox =
        BasicComponentFactory.createCheckBox(_presentationModel.getModel(KnopflerfishRunProperties.DEBUG_MODE), "");
    _systemPackages =
        BasicComponentFactory.createTextField(_presentationModel.getModel(KnopflerfishRunProperties.SYSTEM_PACKAGES));
    _bootDelegation =
        BasicComponentFactory.createTextField(_presentationModel.getModel(KnopflerfishRunProperties.BOOT_DELEGATION));

  }


  private JPanel _mainPanel;
  private JCheckBox _debugCheckbox;
  private JTextField _systemPackages;
  private JTextField _bootDelegation;
  private PresentationModel<KnopflerfishRunProperties> _presentationModel;
}