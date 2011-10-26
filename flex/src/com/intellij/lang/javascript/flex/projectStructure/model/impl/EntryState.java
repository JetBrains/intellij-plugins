package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.impl.DependencyTypeImpl;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;

/**
* User: ksafonov
*/
@Tag("entry")
public class EntryState {

  @Attribute("module-name")
  public String MODULE_NAME;

  @Attribute("build-configuration-name")
  public String BC_NAME;

  @Attribute("library-id")
  public String LIBRARY_ID;

  @Attribute("library-name")
  public String LIBRARY_NAME;

  @Attribute("library-level")
  public String LIBRARY_LEVEL;

  @Property(surroundWithTag = false)
  public DependencyTypeImpl.State DEPENDENCY_TYPE;

}
