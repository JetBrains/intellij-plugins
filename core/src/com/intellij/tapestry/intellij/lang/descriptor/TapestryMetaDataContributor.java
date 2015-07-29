package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.filters.position.RootTagFilter;
import com.intellij.psi.filters.position.TargetNamespaceFilter;
import com.intellij.psi.meta.MetaDataContributor;
import com.intellij.psi.meta.MetaDataRegistrar;
import com.intellij.tapestry.core.TapestryConstants;

/**
 * Created by Maxim.Mossienko on 7/28/2015.
 */
public class TapestryMetaDataContributor implements MetaDataContributor {
  @Override
  public void contributeMetaData(MetaDataRegistrar registrar) {
    MetaDataRegistrar.getInstance().registerMetaData(new RootTagFilter(new TargetNamespaceFilter(TapestryXmlExtension.tapestryTemplateNamespaces())),
                                                     TapestryNamespaceDescriptor.class);
    MetaDataRegistrar.getInstance().registerMetaData(new RootTagFilter(new TargetNamespaceFilter(TapestryConstants.PARAMETERS_NAMESPACE)),
                                                     TapestryParametersNamespaceDescriptor.class);
  }
}
