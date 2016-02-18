package com.intellij.coldFusion.projectWizard;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.UI.config.CfmlMappingsForm;
import com.intellij.coldFusion.UI.runner.CfmlRunConfigurationEditor;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.ui.ListCellRendererWrapper;

import javax.swing.*;

/**
 * Created by jetbrains on 11/02/16.
 */
public class CfmlGeneratorPeer {
  private JPanel myPanel;
  private JLabel myWebPathLabel;
  private JTextField myWebPathField;
  private JTextField myWebPortField;
  private JComboBox myLanguageLevel;

  private CfmlProjectWizardData mySettings;
  private CfmlRunConfigurationEditor myConfigurationEditor;

  @SuppressWarnings("unchecked")
  public CfmlGeneratorPeer() {
    myLanguageLevel.setRenderer(new ListCellRendererWrapper<String>() {
      @Override
      public void customize(JList list, String value, int index, boolean selected, boolean hasFocus) {
        if (CfmlLanguage.CF8.equals(value)) {
          setText("ColdFusion 8");
        }
        else if (CfmlLanguage.CF9.equals(value)) {
          setText("ColdFusion 9");
        }
        else if (CfmlLanguage.CF10.equals(value)) {
          setText("ColdFusion 10");
        }
        else if (CfmlLanguage.CF11.equals(value)) {
          setText("ColdFusion 11");
        }
        else if (CfmlLanguage.RAILO.equals(value)) {
          //noinspection SpellCheckingInspection
          setText("Railo");
        }
      }
    });
    myLanguageLevel.addItem(CfmlLanguage.CF8);
    myLanguageLevel.addItem(CfmlLanguage.CF9);
    myLanguageLevel.addItem(CfmlLanguage.CF10);
    myLanguageLevel.addItem(CfmlLanguage.CF11);
    myLanguageLevel.addItem(CfmlLanguage.RAILO);
  }

  public JComponent getComponent() {
    return myPanel;
  }

  public CfmlProjectWizardData getSettings() {
    return new CfmlProjectWizardData(myWebPathField.getText(),
                                     myWebPortField.getText(),
                                     (String) myLanguageLevel.getSelectedItem());
  }

  public boolean validateInIntelliJ() {
    return true;
  }
}
