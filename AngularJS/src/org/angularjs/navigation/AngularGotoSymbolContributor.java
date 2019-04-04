package org.angularjs.navigation;

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.util.ArrayUtil;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularSymbolIndex;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularGotoSymbolContributor implements ChooseByNameContributor {
  @NotNull
  @Override
  public String[] getNames(Project project, boolean includeNonProjectItems) {
    return ArrayUtil.toStringArray(AngularIndexUtil.getAllKeys(AngularSymbolIndex.KEY, project));
  }

  @NotNull
  @Override
  public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
    final JSImplicitElement item = AngularIndexUtil.resolve(project, AngularSymbolIndex.KEY, name);
    return item != null ? new NavigationItem[] {item} : NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY;
  }
}
