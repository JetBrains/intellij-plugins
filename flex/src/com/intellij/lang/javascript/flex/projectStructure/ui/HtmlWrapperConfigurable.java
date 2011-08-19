package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.options.HtmlWrapperOptions;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.NamedConfigurable;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

public class HtmlWrapperConfigurable extends NamedConfigurable<HtmlWrapperOptions> {
  private JPanel myMainPanel;
  private JRadioButton myNoWrapperRadioButton;
  private JRadioButton myWrapperFromSDKRadioButton;
  private JComboBox myComboBox1;
  private JRadioButton myCustomTemplateRadioButton;
  private final HtmlWrapperOptions myHtmlWrapperOptions;

  public HtmlWrapperConfigurable(final HtmlWrapperOptions htmlWrapperOptions) {
    myHtmlWrapperOptions = htmlWrapperOptions;
  }

  @Nls
  public String getDisplayName() {
    return "HTML Wrapper";
  }

  public void setDisplayName(final String name) {
  }

  public String getBannerSlogan() {
    return "HTML Wrapper";
  }

  public Icon getIcon() {
    return null;
  }

  public HtmlWrapperOptions getEditableObject() {
    return myHtmlWrapperOptions;
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  public boolean isModified() {
    return false;
  }

  public void apply() throws ConfigurationException {
    applyTo(myHtmlWrapperOptions);
  }

  public void applyTo(final HtmlWrapperOptions htmlWrapperOptions) {
  }

  public void reset() {
  }

  public void disposeUIResources() {
  }
}
