package com.jetbrains.plugins.meteor.spacebars.templates;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtilRt;
import com.jetbrains.plugins.meteor.MeteorFacade;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 *
 * Add meteor 'templates' to 'find symbols'
 *
 */
public class MeteorTemplatesSymbolContributor implements ChooseByNameContributor {
  @Override
  public String @NotNull [] getNames(Project project, boolean includeNonProjectItems) {
    if (!MeteorFacade.getInstance().isMeteorProject(project)) return ArrayUtilRt.EMPTY_STRING_ARRAY;

    return ArrayUtilRt.toStringArray(MeteorTemplateIndex.getKeys(project));
  }

  @SuppressWarnings("unchecked")
  @Override
  public NavigationItem @NotNull [] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
    if (!MeteorFacade.getInstance().isMeteorProject(project)) return NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY;

    GlobalSearchScope scope = includeNonProjectItems ? GlobalSearchScope.allScope(project) : GlobalSearchScope.projectScope(project);
    ArrayList declarations = MeteorTemplateIndex.findDeclarations(name, PsiManager.getInstance(project), scope);
    return ((ArrayList<NavigationItem>)declarations).toArray(new NavigationItem[declarations.size()]);
  }
}
