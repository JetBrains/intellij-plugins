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

  public ElementsCachedMap(@NonNls String keyName, boolean cacheComponents, boolean cachePages) {
    super(keyName);
    myCacheComponents = cacheComponents;
    myCachePages = cachePages;
    assert myCachePages || myCacheComponents;
  }

  protected Map<String, PresentationLibraryElement> computeValue(Module module) {
    Map<String, PresentationLibraryElement> map = new THashMap<String, PresentationLibraryElement>();
    TapestryProject project = TapestryModuleSupportLoader.getTapestryProject(module);
    assert project != null;
    for (Library library : project.getLibraries()) {
      if (myCacheComponents) computeKeyAndAdd(map, library.getComponents().values());
      if (myCachePages) computeKeyAndAdd(map, library.getPages().values());
    }
    return map;
  }

  @Override
  protected Object[] getDependencies(Module module) {
    return TapestryProject.JAVA_STRUCTURE_DEPENDENCY;
  }

  protected Project getProject(Module projectOwner) {
    return projectOwner.getProject();
  }

  private void computeKeyAndAdd(Map<String, PresentationLibraryElement> map, Collection<PresentationLibraryElement> elements) {
    for (PresentationLibraryElement element : elements) {
      String key = computeKey(element);
      if (key != null) {
        map.put(key, element);
      }
    }
  }

  @Nullable
  protected abstract String computeKey(PresentationLibraryElement element);
}
