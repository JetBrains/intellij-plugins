package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramElementsProvider;
import com.intellij.diagram.actions.DiagramAddElementAction;
import com.intellij.diagram.extras.DiagramExtras;
import com.intellij.diagram.settings.DiagramConfigElement;
import com.intellij.diagram.settings.DiagramConfigGroup;
import com.intellij.lang.javascript.flex.FlexBundle;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kirill Safonov
 * @author Konstantin Bulenkov
 */
public class JSUmlExtras extends DiagramExtras<Object> {
  private static final DiagramElementsProvider[] PROVIDERS = {new JSSupersProvider(), new JSImplementationsProvider()};

  private static final JSUmlDndProvider DND_PROVIDER = new JSUmlDndProvider();


  private static final DiagramConfigGroup[] ADDITIONAL_SETTINGS_GROUPS;

  static {
    DiagramConfigGroup dependenciesGroup = new DiagramConfigGroup(FlexBundle.message("uml.dependencies.settings.group.title"));
    for (JSDependenciesSettingsOption option : JSDependenciesSettingsOption.values()) {
      dependenciesGroup.addElement(new DiagramConfigElement(option.getDisplayName(), true));
    }
    ADDITIONAL_SETTINGS_GROUPS = new DiagramConfigGroup[]{dependenciesGroup};
  }

  @Override
  public DiagramElementsProvider<Object>[] getElementsProviders() {
    //noinspection unchecked
    return PROVIDERS;
  }

  @Override
  public JSUmlDndProvider getDnDProvider() {
    return DND_PROVIDER;
  }

  @Override
  public DiagramAddElementAction getAddElementHandler() {
    return DEFAULT_ADD_HANDLER;
  }

  @NotNull
  @Override
  public DiagramConfigGroup[] getAdditionalDiagramSettings() {
    return ADDITIONAL_SETTINGS_GROUPS;
  }
}
