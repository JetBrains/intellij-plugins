package org.angularjs.codeInsight.router;

import com.intellij.diagram.DiagramCategory;
import com.intellij.icons.AllIcons;
import org.angularjs.AngularJSBundle;

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
    public static final DiagramCategory STATE =
      new DiagramCategory(AngularJSBundle.message("category.name.states"), AllIcons.Hierarchy.Class, true);
    public static final DiagramCategory VIEW =
      new DiagramCategory(AngularJSBundle.message("category.name.views"), AllIcons.Actions.Forward, true);
    public static final DiagramCategory TEMPLATE =
      new DiagramCategory(AngularJSBundle.message("category.name.templates"), AllIcons.Actions.EditSource, true);
    public static final DiagramCategory TEMPLATE_PLACEHOLDER = new DiagramCategory(
      AngularJSBundle.message("category.name.templateplaceholders"), AllIcons.Actions.Unselectall, true);
    public static final DiagramCategory TOP_LEVEL_TEMPLATE =
      new DiagramCategory(AngularJSBundle.message("category.name.topleveltemplate"), AllIcons.Actions.EditSource, true);
  }

  private final DiagramCategory myCategory;

  Type(DiagramCategory category) {
    myCategory = category;
  }

  public DiagramCategory getCategory() {
    return myCategory;
  }
}
