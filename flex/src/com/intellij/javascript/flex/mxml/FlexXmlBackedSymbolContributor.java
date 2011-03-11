package com.intellij.javascript.flex.mxml;

import com.intellij.lang.javascript.index.JavaScriptClassContributor;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.util.ArrayUtil;
import gnu.trove.THashSet;

import java.util.Collection;

public class FlexXmlBackedSymbolContributor implements ChooseByNameContributor {
  public String[] getNames(Project project, boolean includeNonProjectItems) {
    Collection<String> result = new THashSet<String>();
    result.addAll(FlexXmlBackedMembersIndex.getSymbolNames(project));
    JavaScriptClassContributor.collectAllMxmlClassesNames(project, result);
    return ArrayUtil.toStringArray(result);
  }

  public NavigationItem[] getItemsByName(String name, final String pattern, Project project, boolean includeNonProjectItems) {
    Collection<NavigationItem> result = new THashSet<NavigationItem>();
    result.addAll(FlexXmlBackedMembersIndex.getItemsByName(name, project));
    result.addAll(JavaScriptClassContributor.getItemsByNameStatic(name, pattern, project, includeNonProjectItems));
    return result.toArray(new NavigationItem[result.size()]);
  }
}
