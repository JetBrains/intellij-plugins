// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea;

import jetbrains.communicator.util.HardWrapUtil;

import javax.swing.*;

/**
 * @author Kir
 */
public class GetMessageDialog extends IdeaDialog {
  private JLabel myLabel;
  private JTextArea myTextArea;
  private JPanel myPanel;
  private final HardWrapUtil myWrapper;

  public GetMessageDialog(String titleText, String labelText, String optionalOKButtonText) {
    super(false);
    setModal(true);
    setTitle(titleText);
    myLabel.setText(labelText);

    if (optionalOKButtonText != null) {
      setOKButtonText(optionalOKButtonText);
    }

    myWrapper = new HardWrapUtil(myTextArea);
    init();
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myTextArea;
  }

  @Override
  protected JComponent createCenterPanel() {
    return myPanel;
  }

  public String getEnteredText() {
    return isOK() ? myWrapper.getText() : null;
  }

}
