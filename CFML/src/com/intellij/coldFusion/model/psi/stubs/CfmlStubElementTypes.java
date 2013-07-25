package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.psi.tree.IStubFileElementType;

/**
 * @author: vnikolaenko
 */
public interface CfmlStubElementTypes {
  CfmlStubElementType<CfmlComponentStub, CfmlComponent>
    COMPONENT_DEFINITION = new CfmlComponentElementTypeImpl("COMPONENT_DEFINITION");
  CfmlStubElementType<CfmlComponentStub, CfmlComponent> COMPONENT_TAG = new CfmlTagComponentElementTypeImpl("ComponentTag");
  IStubFileElementType CFML_FILE = new CfmlFileElementType("CFML_FILE", CfmlLanguage.INSTANCE);
}
