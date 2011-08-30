package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.TabbedPaneWrapper;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author ksafonov
 */
public class CompositeConfigurable extends NamedConfigurable {

  public interface Item {
    String getTabTitle();
  }

  private final List<NamedConfigurable> myChildren;
  private final Disposable myDisposable = Disposer.newDisposable();
  private TabbedPaneWrapper myTabs;

  public CompositeConfigurable(List<NamedConfigurable> children, Runnable updateTree) {
    super(false, updateTree);
    myChildren = children;
  }

  @Override
  public void setDisplayName(String name) {
    getMainChild().setDisplayName(name);
  }

  public NamedConfigurable getMainChild() {
    return myChildren.get(0);
  }

  @Override
  public Object getEditableObject() {
    return getMainChild().getEditableObject();
  }

  @Override
  public String getBannerSlogan() {
    return getMainChild().getBannerSlogan();
  }

  @Override
  public JComponent createOptionsPanel() {
    myTabs = new TabbedPaneWrapper(myDisposable);
    for (NamedConfigurable child : myChildren) {
      JPanel p = new JPanel(new BorderLayout());
      p.setBorder(IdeBorderFactory.createEmptyBorder(5));
      p.add(child.createComponent(), BorderLayout.CENTER);
      String tabName = child instanceof Item ? ((Item)child).getTabTitle() : child.getDisplayName();
      myTabs.addTab(tabName, p);
    }
    return myTabs.getComponent();
  }

  @Nls
  @Override
  public String getDisplayName() {
    return getMainChild().getDisplayName();
  }

  @Override
  public Icon getIcon() {
    return getMainChild().getIcon();
  }

  @Override
  public String getHelpTopic() {
    return myChildren.get(myTabs.getSelectedIndex()).getHelpTopic();
  }

  @Override
  public boolean isModified() {
    for (NamedConfigurable child : myChildren) {
      if (child.isModified()) return true;
    }
    return false;
  }

  @Override
  public void apply() throws ConfigurationException {
    for (NamedConfigurable child : myChildren) {
      child.apply();
    }
  }

  @Override
  public void reset() {
    for (NamedConfigurable child : myChildren) {
      child.reset();
    }
  }

  @Override
  public void disposeUIResources() {
    for (NamedConfigurable child : myChildren) {
      child.disposeUIResources();
    }
    Disposer.dispose(myDisposable);
  }
}
