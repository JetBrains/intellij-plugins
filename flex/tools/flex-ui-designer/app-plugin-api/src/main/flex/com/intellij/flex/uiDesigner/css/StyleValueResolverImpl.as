package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.EmbedImageManager;
import com.intellij.flex.uiDesigner.EmbedSwfManager;
import com.intellij.flex.uiDesigner.flex.ClassReference;

import flash.system.ApplicationDomain;

import org.flyti.plexus.PlexusManager;

public class StyleValueResolverImpl implements StyleValueResolver {
  private var applicationDomain:ApplicationDomain;

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

  public function StyleValueResolverImpl(applicationDomain:ApplicationDomain) {
    this.applicationDomain = applicationDomain;
  }

  public function resolve(propertyDescriptor:CssDeclaration):* {
    if (propertyDescriptor.value is ClassReferenceImpl) {
      return applicationDomain.getDefinition(ClassReference(propertyDescriptor.value).className);
    }
    else if (propertyDescriptor is CssEmbedSwfDeclaration) {
      var embedSwf:CssEmbedSwfDeclaration = CssEmbedSwfDeclaration(propertyDescriptor);
      return embedSwfManager.get(embedSwf.id, embedSwf.symbol);
    }
    else if (propertyDescriptor is CssEmbedImageDeclaration) {
      var embedImage:CssEmbedImageDeclaration = CssEmbedImageDeclaration(propertyDescriptor);
      return embedImageManager.get(embedImage.id, applicationDomain);
    }
    else {
      return propertyDescriptor.value;
    }
  }
}
}