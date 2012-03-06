package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.EmbedImageManager;
import com.intellij.flex.uiDesigner.EmbedSwfManager;
import com.intellij.flex.uiDesigner.Module;
import com.intellij.flex.uiDesigner.flex.ClassReference;
import com.intellij.flex.uiDesigner.libraries.FlexLibrarySet;

import org.flyti.plexus.PlexusManager;

public class StyleValueResolverImpl implements StyleValueResolver {
  private var module:Module;

  public function StyleValueResolverImpl(module:Module) {
    this.module = module;
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
      return module.getDefinition(ClassReference(propertyDescriptor.value).className);
    }
    else if (propertyDescriptor is CssEmbedSwfDeclaration) {
      return embedSwfManager.get(CssEmbedSwfDeclaration(propertyDescriptor).id, module.getClassPool(FlexLibrarySet.SWF_POOL),
                                 module.project);
    }
    else if (propertyDescriptor is CssEmbedImageDeclaration) {
      return embedImageManager.get(CssEmbedImageDeclaration(propertyDescriptor).id, module.getClassPool(FlexLibrarySet.IMAGE_POOL),
                                   module.project);
    }
    else {
      return propertyDescriptor.value;
    }
  }
}
}