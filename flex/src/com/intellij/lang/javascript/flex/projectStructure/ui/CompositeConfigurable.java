package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectStructureElementConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureElement;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.TabbedPaneWrapper;
import com.intellij.ui.navigation.Place;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CompositeConfigurable extends ProjectStructureElementConfigurable<ModifiableFlexBuildConfiguration> implements Place.Navigator {

  public static final String TAB_NAME = "tabName";

  public interface Item {
    String getTabTitle();
  }

  private final FlexBCConfigurable myMainChild;
  private final List<NamedConfigurable> myChildren = new ArrayList<>();
  private final Disposable myDisposable = Disposer.newDisposable();
  private final TabbedPaneWrapper myTabs;

  public CompositeConfigurable(FlexBCConfigurable mainChild, List<NamedConfigurable> otherChildren, Runnable updateTree) {
    super(false, updateTree);
    myMainChild = mainChild;
    myTabs = new TabbedPaneWrapper(myDisposable);
    myChildren.add(mainChild);
    myChildren.addAll(otherChildren);
  }

  @Override
  public void setDisplayName(String name) {
    getMainChild().setDisplayName(name);
  }

  public FlexBCConfigurable getMainChild() {
    return myMainChild;
  }

  @Override
  public ModifiableFlexBuildConfiguration getEditableObject() {
    return getMainChild().getEditableObject();
  }

  @Override
  public String getBannerSlogan() {
    return getMainChild().getBannerSlogan();
  }

  @Override
  public JComponent createOptionsPanel() {
    for (NamedConfigurable child : myChildren) {
      addTab(child);
    }

    return myTabs.getComponent();
  }

  private void addTab(final NamedConfigurable child) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(JBUI.Borders.empty(5));
    p.add(child.createComponent(), BorderLayout.CENTER);
    String tabName = child instanceof Item ? ((Item)child).getTabTitle() : child.getDisplayName();
    myTabs.addTab(tabName, p);
  }

  public List<NamedConfigurable> getChildren() {
    return myChildren;
  }

  public void addChild(final NamedConfigurable child) {
    myChildren.add(child);
    addTab(child);
  }

  public void removeChildAt(final int index) {
    myChildren.remove(index);
    myTabs.removeTabAt(index);
  }

  @Nls
  @Override
  public String getDisplayName() {
    return getMainChild().getDisplayName();
  }

  @Override
  public Icon getIcon(boolean expanded) {
    return FlexBCConfigurable.unwrap(this).getIcon();
  }

  @Override
  public String getHelpTopic() {
    final String helpTopic = myChildren.get(myTabs.getSelectedIndex()).getHelpTopic();
    return helpTopic != null ? helpTopic : getMainChild().getHelpTopic();
  }

  @Override
  public boolean isModified() {
    //for (NamedConfigurable child : myChildren) {
    //  if (child.isModified()) return true;
    //}
    //return false;
    return getMainChild().isModified();
  }

  @Override
  public void apply() throws ConfigurationException {
    //for (NamedConfigurable child : myChildren) {
    //  child.apply();
    //}
    getMainChild().apply();
  }

  @Override
  public void reset() {
    //for (NamedConfigurable child : myChildren) {
    //  child.reset();
    //}
    getMainChild().reset();
  }

  @Override
  public void disposeUIResources() {
    //for (NamedConfigurable child : myChildren) {
    //  child.disposeUIResources();
    //}
    getMainChild().disposeUIResources();
    Disposer.dispose(myDisposable);
  }

  @Override
  public ActionCallback navigateTo(@Nullable final Place place, final boolean requestFocus) {
    if (place == null) {
      return ActionCallback.DONE;
    }

    final Object tabName = place.getPath(TAB_NAME);
    if (tabName instanceof String) {
      for (int i = 0; i < myChildren.size(); i++) {
        final NamedConfigurable child = myChildren.get(i);
        if (tabName.equals(child.getDisplayName())) {
          myTabs.setSelectedIndex(i);
          return Place.goFurther(child, place, requestFocus);
        }
      }
    }
    return ActionCallback.DONE;
  }

  @Override
  public void queryPlace(@NotNull final Place place) {
    final NamedConfigurable child = myChildren.get(myTabs.getSelectedIndex());
    place.putPath(TAB_NAME, child.getDisplayName());
    //Place.queryFurther(child, place); we don't want to localize place to text field level (and actually it is impossible because the field hasn't got focus yet when this method is called)
  }

  @Override
  @Nullable
  public ProjectStructureElement getProjectStructureElement() {
    NamedConfigurable mainChild = getMainChild();
    if (mainChild instanceof ProjectStructureElementConfigurable) {
      return ((ProjectStructureElementConfigurable<?>)mainChild).getProjectStructureElement();
    }
    return null;
  }
}
