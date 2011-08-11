package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.EmbedImageManager;
import com.intellij.flex.uiDesigner.EmbedSwfManager;
import com.intellij.flex.uiDesigner.ModuleContextEx;
import com.intellij.flex.uiDesigner.flex.ClassReference;

import org.flyti.plexus.PlexusManager;

public class StyleValueResolverImpl implements StyleValueResolver {
  private var moduleContenxt:ModuleContextEx;

  public function StyleValueResolverImpl(moduleContenxt:ModuleContextEx) {
    this.moduleContenxt = moduleContenxt;
  }

  private var _embedSwfManager:EmbedSwfManager;
  private function get embedSwfManager():EmbedSwfManager {
    if (_embedSwfManager == null) {
      _embedSwfManager = EmbedSwfManager(PlexusManager.instance.container.lookup(EmbedSwfManager));
    }

    return _embedSwfManager;
  }

  private var _embedImageManager:EmbedImageManager;
  private function get embedImageManager():EmbedImageManager {
    if (_embedImageManager == null) {
      _embedImageManager = EmbedImageManager(PlexusManager.instance.container.lookup(EmbedImageManager));
    }

    return _embedImageManager;
  }

  public function resolve(propertyDescriptor:CssDeclaration):* {
    if (propertyDescriptor.value is ClassReferenceImpl) {
      return moduleContenxt.getDefinition(ClassReference(propertyDescriptor.value).className);
    }
    else if (propertyDescriptor is CssEmbedSwfDeclaration) {
      return embedSwfManager.get(CssEmbedSwfDeclaration(propertyDescriptor).id, moduleContenxt.swfAssetContainerClassPool);
    }
    else if (propertyDescriptor is CssEmbedImageDeclaration) {
      return embedImageManager.get(CssEmbedImageDeclaration(propertyDescriptor).id, moduleContenxt.imageAssetContainerClassPool);
    }
    else {
      return propertyDescriptor.value;
    }
  }
}
}