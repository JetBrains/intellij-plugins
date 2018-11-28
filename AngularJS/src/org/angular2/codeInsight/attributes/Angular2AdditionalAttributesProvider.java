// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.xml.XmlTag;

import java.util.List;

public interface Angular2AdditionalAttributesProvider {

  ExtensionPointName<Angular2AdditionalAttributesProvider> ADDITIONAL_ATTRIBUTES_PROVIDER_EP =
    ExtensionPointName.create("org.angular2.additionalAttributesProvider");

  List<String> getPrefixes(boolean forCompletion);

  List<Angular2AttributeDescriptor> getDescriptors(XmlTag tag, boolean forCompletion);
}
