package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.EmbedSwfManager;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

class ClassValueWriter extends AbstractPrimitiveValueWriter {
  private final JSClass jsClass;

  ClassValueWriter(JSClass jsClass) {
    this.jsClass = jsClass;
  }
  
  public JSClass getJsClass() {
    return jsClass;
  }

  @Override
  protected void doWrite(PrimitiveAmfOutputStream out, BaseWriter writer, boolean isStyle) {
    PsiFile psiFile = jsClass.getContainingFile();
    VirtualFile virtualFile = psiFile.getVirtualFile();
    if (virtualFile != null && JavaScriptSupportLoader.isFxgFile(virtualFile)) {
      out.write(AmfExtendedTypes.SWF);
      out.writeUInt29(EmbedSwfManager.getInstance().add(virtualFile, EmbedSwfManager.FXG_MARKER, writer.getAssetCounter()));
    }
    else {
      writer.classReference(jsClass.getQualifiedName());
    }
  }
}