package com.intellij.tapestry.core;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tapestry.core.model.TapestryLibrary;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.util.CachedUserDataCache;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexey Chmutov
 */
abstract class ElementsCachedMap extends CachedUserDataCache<Map<String, PresentationLibraryElement>, Module> {
  private final boolean myCacheComponents;
  private final boolean myCachePages;
  private final boolean myCacheMixin;
  private final boolean myCacheAbstractComponents;

  ElementsCachedMap(@NonNls String keyName, boolean cacheComponents, boolean cachePages, boolean cacheMixin) {
    this(keyName, cacheComponents, cachePages, cacheMixin, false);
  }

  ElementsCachedMap(@NonNls String keyName,
                           boolean cacheComponents,
                           boolean cachePages,
                           boolean cacheMixin,
                           boolean cacheAbstractComponents) {
    super(keyName);
    myCacheComponents = cacheComponents;
    myCachePages = cachePages;
    myCacheMixin = cacheMixin;
    myCacheAbstractComponents = cacheAbstractComponents;
    assert myCachePages || myCacheComponents || myCacheMixin || myCacheAbstractComponents;
  }

  @Override
  protected Map<String, PresentationLibraryElement> computeValue(Module module) {
    Map<String, PresentationLibraryElement> map = new HashMap<>();
    TapestryProject project = TapestryModuleSupportLoader.getTapestryProject(module);
    assert project != null;
    for (TapestryLibrary library : project.getLibraries()) {
      if (myCacheComponents) computeKeyAndAddAll(map, library.getComponents().values(), library.getShortName());
      if (myCacheAbstractComponents) computeKeyAndAddAll(map, library.getAbstractComponents().values(), library.getShortName());
      if (myCachePages) computeKeyAndAddAll(map, library.getPages().values(), library.getShortName());
      if (myCacheMixin) computeKeyAndAddAll(map, library.getMixins().values(), library.getShortName());
    }
    if (myCacheComponents) computeKeyAndAddAll(map, project.getBuiltinComponents(), null);
    if (myCachePages) computeKeyAndAddAll(map, project.getBuiltinPages(), null);
    return map;
  }

  @Override
  protected Object[] getDependencies(Module module) {
    return TapestryProject.JAVA_STRUCTURE_DEPENDENCY;
  }

  @Override
  protected Project getProject(Module projectOwner) {
    return projectOwner.getProject();
  }

  private void computeKeyAndAddAll(Map<String, PresentationLibraryElement> map,
                                   Collection<PresentationLibraryElement> elements,
                                   @Nullable String name) {
    for (PresentationLibraryElement element : elements) {
      String key = element == null ? null : computeKey(element);
      if (key != null) {
        if(!StringUtil.isEmpty(name)) {
          key = name + "/" + key;
        }
        map.put(key, element);
      }
    }
  }

  @Nullable
  protected abstract String computeKey(PresentationLibraryElement element);
}
