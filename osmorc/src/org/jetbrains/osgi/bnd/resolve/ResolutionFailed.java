/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.osgi.bnd.resolve;

import com.intellij.ui.components.JBList;
import org.osgi.resource.Requirement;
import org.osgi.service.resolver.ResolutionException;

import javax.swing.*;

public class ResolutionFailed {
  private JPanel contentPane;

  private JTextPane _exception;
  private JBList<Requirement> _unresolved;

  public ResolutionFailed(ResolutionException resolutionException) {
    _exception.setText(resolutionException.getMessage());

    DefaultListModel<Requirement> model = new DefaultListModel<>();
    resolutionException.getUnresolvedRequirements().forEach(model::addElement);
    _unresolved.setModel(model);
  }

  public JPanel getContentPane() {
    return contentPane;
  }
}
