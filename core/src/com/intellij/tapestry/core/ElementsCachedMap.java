package com.intellij.tapestry.core;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.tapestry.core.model.Library;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.util.CachedUserDataCache;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * @author Alexey Chmutov
 *         Date: Jun 30, 2009
 *         Time: 2:39:11 PM
 */
abstract class ElementsCachedMap extends CachedUserDataCache<Map<String, PresentationLibraryElement>, Module> {
  private final boolean myCacheComponents;
  private final boolean myCachePages;
  private final boolean myCacheMixin;

  public ElementsCachedMap(@NonNls String keyName, boolean cacheComponents, boolean cachePages, boolean cacheMixin) {
    super(keyName);
    myCacheComponents = cacheComponents;
    myCachePages = cachePages;
    myCacheMixin = cacheMixin;
    assert myCachePages || myCacheComponents || myCacheMixin;
  }

  protected Map<String, PresentationLibraryElement> computeValue(Module module) {
    Map<String, PresentationLibraryElement> map = new THashMap<String, PresentationLibraryElement>();
    TapestryProject project = TapestryModuleSupportLoader.getTapestryProject(module);
    assert project != null;
    for (Library library : project.getLibraries()) {
      if (myCacheComponents) computeKeyAndAddAll(map, library.getComponents().values());
      if (myCachePages) computeKeyAndAddAll(map, library.getPages().values());
      if (myCacheMixin) computeKeyAndAddAll(map, library.getMixins().values());
    }
    if (myCacheComponents) computeKeyAndAddAll(map, project.getBuiltinComponents());
    if (myCachePages) computeKeyAndAddAll(map, project.getBuiltinPages());
    return map;
  }

  @Override
  protected Object[] getDependencies(Module module) {
    return TapestryProject.JAVA_STRUCTURE_DEPENDENCY;
  }

  protected Project getProject(Module projectOwner) {
    return projectOwner.getProject();
  }

  private void computeKeyAndAddAll(Map<String, PresentationLibraryElement> map, Collection<PresentationLibraryElement> elements) {
    for (PresentationLibraryElement element : elements) {
      String key = element == null ? null : computeKey(element);
      if (key != null) {
        map.put(key, element);
      }
    }
  }

  @Nullable
  protected abstract String computeKey(PresentationLibraryElement element);
}
