package org.angularjs.codeInsight.router;

import com.intellij.diagram.DiagramCategory;
import com.intellij.icons.AllIcons;

/**
 * @author Irina.Chernushina on 3/23/2016.
 */
public enum Type {
  state(Categories.STATE),
  view(Categories.VIEW),
  template(Categories.TEMPLATE),
  templatePlaceholder(Categories.TEMPLATE_PLACEHOLDER),
  topLevelTemplate(Categories.TOP_LEVEL_TEMPLATE);

  public static class Categories {
    public final static DiagramCategory STATE = new DiagramCategory("States", AllIcons.Hierarchy.Class, true);
    public final static DiagramCategory VIEW = new DiagramCategory("Views", AllIcons.Actions.Forward, true);
    public final static DiagramCategory TEMPLATE = new DiagramCategory("Templates", AllIcons.Actions.EditSource, true);
    public final static DiagramCategory TEMPLATE_PLACEHOLDER =
      new DiagramCategory("TemplatePlaceholders", AllIcons.Actions.Unselectall, true);
    public final static DiagramCategory TOP_LEVEL_TEMPLATE = new DiagramCategory("TopLevelTemplate", AllIcons.Actions.EditSource, true);
  }

  private final DiagramCategory myCategory;

  Type(DiagramCategory category) {
    myCategory = category;
  }

  public DiagramCategory getCategory() {
    return myCategory;
  }
}
