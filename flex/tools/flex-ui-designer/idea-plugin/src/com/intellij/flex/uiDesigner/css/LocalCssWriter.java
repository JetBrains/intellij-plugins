package com.intellij.flex.uiDesigner.css;

import com.intellij.flex.uiDesigner.InjectionUtil;
import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.mxml.AmfExtendedTypes;
import com.intellij.javascript.flex.css.FlexStyleIndexInfo;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.xml.XmlFile;

import java.util.ArrayList;
import java.util.List;

public class LocalCssWriter extends CssWriter {
  private final List<XmlFile> unregisteredDocumentFactories = new ArrayList<XmlFile>();

  public LocalCssWriter(StringRegistry.StringWriter stringWriter) {
    super(stringWriter);
  }

  @Override
  protected void writeClassReference(JSClass jsClass, FlexStyleIndexInfo info) throws InvalidPropertyException {
    final int projectComponentFactoryId;
    if (info != null && info.getAttributeName().equals("skinClass")) {
      projectComponentFactoryId = InjectionUtil.getProjectComponentFactoryId(jsClass, unregisteredDocumentFactories);
    }
    else if (InjectionUtil.isProjectComponent(jsClass)) {
      throw new InvalidPropertyException("class.reference.in.css.support.only.skin.class", jsClass.getQualifiedName());
    }
    else {
      projectComponentFactoryId = -1;
    }

    if (projectComponentFactoryId == -1) {
      super.writeClassReference(jsClass, info);
    }
    else {
      propertyOut.write(AmfExtendedTypes.DOCUMENT_FACTORY_REFERENCE);
      propertyOut.writeUInt29(projectComponentFactoryId);
    }
  }
}
