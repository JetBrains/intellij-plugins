// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;
import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.javascript.flex.mxml.schema.FlexMxmlNSDescriptor;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.MxmlFileType;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.filters.position.NamespaceFilter;
import com.intellij.psi.filters.position.RootTagFilter;
import com.intellij.psi.meta.MetaDataContributor;
import com.intellij.psi.meta.MetaDataRegistrar;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

// In fact it is not an application component any more.
public final class FlexApplicationComponent implements MetaDataContributor, StandardResourceProvider {
  public static final FileType SWF_FILE_TYPE = SwfFileType.SWF_FILE_TYPE;

  public static final Language DECOMPILED_SWF = new Language(JavaScriptSupportLoader.ECMA_SCRIPT_L4, "Decompiled SWF") {
  };

  public static final LanguageFileType MXML = MxmlFileType.MXML;

  @NonNls public static final String HTTP_WWW_ADOBE_COM_2006_FLEX_CONFIG = "http://www.adobe.com/2006/flex-config";

  public static final String[] AIR_VERSIONS =
    {"1.0", "1.1", "1.5", "1.5.1", "1.5.2", "1.5.3", "2.0", "2.5", "2.6", "2.7", "3.0", "3.1", "3.2", "3.3", "3.4", "3.5", "3.6", "3.7",
      "3.8", "3.9", "4.0", "13.0", "14.0", "15.0", "16.0", "17.0", "18.0", "19.0", "20.0", "21.0", "22.0", "23.0", "24.0", "25.0", "26.0",
      "27.0", "28.0", "29.0", "30.0", "31.0", "32.0", "33.0", "33.1", "34.0", "35.0"};

  @Override
  public void contributeMetaData(@NotNull final MetaDataRegistrar registrar) {
    registrar.registerMetaData(
      new RootTagFilter(new NamespaceFilter(MxmlJSClass.MXML_URIS)),
      FlexMxmlNSDescriptor.class
    );
  }

  @Override
  public void registerResources(ResourceRegistrar registrar) {
    registrar.addStdResource(HTTP_WWW_ADOBE_COM_2006_FLEX_CONFIG, "/schemas/FlexCompilerSchema.xsd", FlexApplicationComponent.class);

    registrar.addStdResource(JSAttributeImpl.URN_FLEX_META, "/schemas/KnownMetaData.dtd", FlexApplicationComponent.class);

    for (String version : AIR_VERSIONS) {
      registerAirDescriptorSchema(registrar, version);
    }
  }

  private static void registerAirDescriptorSchema(final ResourceRegistrar registrar, final String version) {
    registrar.addStdResource(FlexCommonUtils.AIR_NAMESPACE_BASE + version,
                             "/schemas/AIR_Descriptor." + version + ".xsd",
                             FlexApplicationComponent.class);
  }
}
