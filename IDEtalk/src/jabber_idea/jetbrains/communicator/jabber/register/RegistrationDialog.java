/*
 * Copyright 2000-2006 JetBrains s.r.o.
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
package jetbrains.communicator.jabber.register;

import com.intellij.ui.JBColor;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.idea.IdeaDialog;
import jetbrains.communicator.jabber.JabberFacade;
import jetbrains.communicator.util.TextAcceptor;

import javax.swing.*;
import java.awt.*;

/**
 * @author Kir
 */
public class RegistrationDialog extends IdeaDialog implements TextAcceptor {
  private final JabberFacade myFacade;
  private final IDEFacade myIdeFacade;

  private RegistrationForm myRegistrationForm;
  private final JLabel myErrorLabel = new JLabel();

  public RegistrationDialog(JabberFacade facade, IDEFacade ideFacade) {
    super(true);
    myFacade = facade;
    myIdeFacade = ideFacade;
    init();
  }

  public RegistrationDialog(JabberFacade facade, IDEFacade ideFacade, Component parent) {
    super(parent, true);
    myFacade = facade;
    myIdeFacade = ideFacade;
    init();
  }

  @Override
  protected void init() {
    setModal(false);
    setTitle("IDEtalk: Jabber Server Connection Settings");
    super.init();
  }

  @Override
  protected JComponent createCenterPanel() {
    myRegistrationForm = new RegistrationForm(myFacade, myIdeFacade, this);
    return myRegistrationForm.getComponent();
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myRegistrationForm.getPreferredFocusedComponent();
  }

  @Override
  public void doCancelAction() {
    myRegistrationForm.cancel();
    super.doCancelAction();
  }

  @Override
  protected void doOKAction() {
    myRegistrationForm.commit();
    if (myFacade.isConnectedAndAuthenticated()) {
      super.doOKAction();
    }
  }

  @Override
  public void setText(String text) {
    myErrorLabel.setText(text);
  }

  @Override
  protected JComponent createSouthPanel() {
    JPanel result = new JPanel(new BorderLayout());
    result.add(super.createSouthPanel());
    myErrorLabel.setHorizontalAlignment(JLabel.CENTER);
    result.add(myErrorLabel, BorderLayout.NORTH);
    myErrorLabel.setMinimumSize(new Dimension(0, 20));
    myErrorLabel.setForeground(JBColor.red);
    Font font = myErrorLabel.getFont();
    myErrorLabel.setFont(new Font(font.getName(), Font.PLAIN, font.getSize() - 1));
    return result;
  }
}
