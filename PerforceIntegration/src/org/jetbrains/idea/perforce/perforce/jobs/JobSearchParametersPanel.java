package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.util.ArrayUtilRt;
import com.intellij.util.ui.NamedColorUtil;
import org.jetbrains.idea.perforce.PerforceBundle;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class JobSearchParametersPanel {
  private final JPanel myPanel;
  private final JTextField myJobName;
  private final JTextField myUser;
  private final JTextField myDateBefore;
  private final JTextField myDateAfter;
  private final JTextField myDescription;
  private final StatusComboBox myStatus;

  public JobSearchParametersPanel() {
    myPanel = new JPanel(new GridBagLayout());
    final GridBagConstraints gb = DefaultGb.create();
    gb.fill = GridBagConstraints.HORIZONTAL;
    gb.anchor = GridBagConstraints.WEST;

    myPanel.add(new JLabel(PerforceBundle.message("job.search.name.pattern")), gb);
    gb.gridx = 1;
    myJobName = new JTextField();
    myPanel.add(myJobName, gb);

    ++ gb.gridy;
    gb.gridx = 0;

    myPanel.add(new JLabel(PerforceBundle.message("job.search.status")), gb);
    gb.gridx = 1;
    myStatus = new StatusComboBox();
    myPanel.add(myStatus, gb);

    ++ gb.gridy;
    gb.gridx = 0;
    myPanel.add(new JLabel(PerforceBundle.message("job.search.user.pattern")), gb);
    gb.gridx = 1;
    myUser = new JTextField();
    myPanel.add(myUser, gb);

    ++ gb.gridy;
    gb.gridx = 0;
    myPanel.add(new JLabel(PerforceBundle.message("job.search.date.before")), gb);
    gb.gridx = 1;
    myDateBefore = new JTextField();
    myPanel.add(myDateBefore, gb);

    ++ gb.gridy;
    gb.gridx = 1;
    final JLabel formatLabel = new JLabel(PerforceBundle.message("job.search.date.format"));
    formatLabel.setForeground(NamedColorUtil.getInactiveTextColor());
    myPanel.add(formatLabel, gb);

    ++ gb.gridy;
    gb.gridx = 0;
    myPanel.add(new JLabel(PerforceBundle.message("job.search.date.after")), gb);
    gb.gridx = 1;
    myDateAfter = new JTextField();
    myPanel.add(myDateAfter, gb);

    ++ gb.gridy;
    gb.gridx = 1;
    final JLabel formatLabel2 = new JLabel(PerforceBundle.message("job.search.date.format"));
    formatLabel2.setForeground(NamedColorUtil.getInactiveTextColor());
    myPanel.add(formatLabel2, gb);

    ++ gb.gridy;
    gb.gridx = 0;
    myPanel.add(new JLabel(PerforceBundle.message("job.search.description")), gb);
    gb.gridx = 1;
    myDescription = new JTextField();
    myPanel.add(myDescription, gb);

    /*myFreeFields = new ArrayList<AnyFieldGroup>();
    for (int i = 0; i < 4; i++) {
      final AnyFieldGroup field = new AnyFieldGroup(freeFields);
      myFreeFields.add(field);
      field.addSelf(myPanel, gb);
    }*/
  }

  public JComponent getPreferredFocusTarget() {
    return myJobName;
  }

  private static boolean notEmpty(final JTextField f) {
    final String t = f.getText();
    return t != null && t.trim().length() > 0;
  }

  public JobsSearchSpecificator createSpecificator() {
    final FullSearchSpecificator result = new FullSearchSpecificator();
    if (notEmpty(myJobName)) {
      result.addStandardConstraint(FullSearchSpecificator.Parts.jobname, myJobName.getText().trim());
    }
    if (notEmpty(myUser)) {
      result.addStandardConstraint(FullSearchSpecificator.Parts.user, myUser.getText().trim());
    }
    if (! "*".equals(myStatus.getSelectedItem())) {
      result.addStandardConstraint(FullSearchSpecificator.Parts.status, myStatus.getSelectedItem().trim());
    }
    if (notEmpty(myDateBefore)) {
      result.addStandardConstraint(FullSearchSpecificator.Parts.dateBefore, myDateBefore.getText().trim());
    }
    if (notEmpty(myDateAfter)) {
      result.addStandardConstraint(FullSearchSpecificator.Parts.dateAfter, myDateAfter.getText().trim());
    }
    if (notEmpty(myDescription)) {
      result.addStandardConstraint(FullSearchSpecificator.Parts.description, myDescription.getText().trim());
    }
    /*for (AnyFieldGroup field : myFreeFields) {
      if (notEmpty(field.myText)) {
        result.addOtherFieldConstraint((String) field.myNameField.getSelectedItem(), field.mySign.getSelectedItem(), field.getValue());
      }
    }*/
    return result;
  }

  private static final class AnyFieldGroup {
    private final JComboBox myNameField;
    private final SignComboBox mySign;
    private final JTextField myText;

    private AnyFieldGroup(final List<String> freeFields) {
      myNameField = new JComboBox(ArrayUtilRt.toStringArray(freeFields));
      myNameField.setEditable(false);
      mySign = new SignComboBox();
      myText = new JTextField();
    }

    public void addSelf(final JPanel main, final GridBagConstraints gb) {
      gb.gridx = 0;
      ++ gb.gridy;
      main.add(myNameField, gb);
      ++ gb.gridx;
      gb.fill = GridBagConstraints.NONE;
      main.add(mySign, gb);
      gb.fill = GridBagConstraints.HORIZONTAL;
      ++ gb.gridx;
      main.add(myText, gb);
    }

    public void enable(final boolean value) {
      myNameField.setEnabled(value);
      mySign.setEnabled(value);
      myText.setEnabled(value);
    }

    public String getValue() {
      return myText.getText();
    }
  }

  public JPanel getComponent() {
    return myPanel;
  }

  private static final class StatusComboBox extends JComboBox<String> {
    private StatusComboBox() {
      //noinspection HardCodedStringLiteral
      super(new String[] {"*", "open", "closed", "suspended"});
      setSelectedIndex(0);
      setEditable(false);
    }

    @Override
    public String getSelectedItem() {
      return (String) super.getSelectedItem();
    }
  }

  private static final class SignComboBox extends JComboBox<String> {
    private SignComboBox() {
      super(new String[]{"=", "<", ">", "<=", ">="});
      setSelectedIndex(0);
      setEditable(false);
    }

    @Override
    public String getSelectedItem() {
      return (String) super.getSelectedItem();
    }
  }
}
