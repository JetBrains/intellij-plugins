package com.intellij.javascript.flex.mxml;

import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.navigation.JavaScriptClassContributor;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.util.ArrayUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class FlexXmlBackedSymbolContributor implements ChooseByNameContributor {
  @Override
  @NotNull
  public String[] getNames(Project project, boolean includeNonProjectItems) {
    Collection<String> result = new THashSet<>();
    result.addAll(FlexXmlBackedMembersIndex.getSymbolNames(project));
    JavaScriptClassContributor.collectAllMxmlClassesNames(project, result);
    return ArrayUtil.toStringArray(result);
  }

  @Override
  @NotNull
  public NavigationItem[] getItemsByName(String name, final String pattern, Project project, boolean includeNonProjectItems) {
    Collection<NavigationItem> result = new THashSet<>();
    result.addAll(FlexXmlBackedMembersIndex.getItemsByName(name, project));
    for (NavigationItem item : JavaScriptClassContributor.getItemsByNameStatic(name, project, includeNonProjectItems)) {
      if (item instanceof XmlBackedJSClassImpl) {
        result.add(item);
      }
    }
    return result.toArray(NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY);
  }
}
