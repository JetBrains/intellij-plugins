package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramElementsProvider;
import com.intellij.diagram.actions.DiagramAddElementAction;
import com.intellij.diagram.extras.DiagramExtras;

/**
 * @author Kirill Safonov
 * @author Konstantin Bulenkov
 */
public class JSUmlExtras extends DiagramExtras<Object> {
  private static final DiagramElementsProvider[] PROVIDERS = {new JSSupersProvider(), new JSImplementationsProvider()};

  private static final JSUmlDndProvider DND_PROVIDER = new JSUmlDndProvider();

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
}
