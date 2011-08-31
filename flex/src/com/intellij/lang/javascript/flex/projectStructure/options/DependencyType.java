package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * @author ksafonov
 */
public class DependencyType implements JDOMExternalizable {

  private static final Logger LOG = Logger.getInstance(DependencyType.class.getName());

  private static final String LINKAGE_TYPE_ATTR = "linkage";
  private static final LinkageType DEFAULT_TYPE = LinkageType.Merged;

  @NotNull
  private LinkageType myLinkageType = DEFAULT_TYPE;

  @NotNull
  public LinkageType getLinkageType() {
    return myLinkageType;
  }

  public void setLinkageType(@NotNull LinkageType linkageType) {
    myLinkageType = linkageType;
  }

  @Override
  public void readExternal(Element element) {
    String linkageType = element.getAttributeValue(LINKAGE_TYPE_ATTR);
    try {
      myLinkageType = LinkageType.valueOf(linkageType);
    }
    catch (IllegalArgumentException e) {
      LOG.warn(e);
      myLinkageType = DEFAULT_TYPE;
    }
  }

  @Override
  public void writeExternal(Element element) {
    element.setAttribute(LINKAGE_TYPE_ATTR, myLinkageType.getSerializedText());
  }

  public void applyTo(DependencyType copy) {
    copy.myLinkageType = myLinkageType;
  }

  public boolean isEqual(DependencyType other) {
    return myLinkageType == other.myLinkageType;
  }
}
