package org.angularjs.navigation;

import com.intellij.lang.javascript.index.AngularSymbolIndex;
import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.util.ArrayUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularGotoSymbolContributor implements ChooseByNameContributor {
  @NotNull
  @Override
  public String[] getNames(Project project, boolean includeNonProjectItems) {
    return ArrayUtil.toStringArray(FileBasedIndex.getInstance().getAllKeys(AngularSymbolIndex.INDEX_ID, project));
  }

  @NotNull
  @Override
  public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
    final JSNamedElementProxy item = AngularIndexUtil.resolve(project, AngularSymbolIndex.INDEX_ID, name);
    return item != null ? new NavigationItem[] {item} : NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY;
  }
}
