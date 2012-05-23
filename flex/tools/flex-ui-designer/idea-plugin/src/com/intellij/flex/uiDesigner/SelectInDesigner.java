package com.intellij.flex.uiDesigner;

import com.intellij.ide.SelectInContext;
import com.intellij.ide.SelectInTarget;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nullable;

import static com.intellij.flex.uiDesigner.DocumentFactoryManager.DocumentInfo;

public class SelectInDesigner implements SelectInTarget {
  @Override
  public boolean canSelect(SelectInContext context) {
    PsiElement element = getPsiElement(context);
    return element != null && DesignerApplicationManager.isApplicable(context.getProject(), element.getContainingFile());
  }

  @Nullable
  private static PsiElement getPsiElement(SelectInContext context) {
    Object selectorInFile = context.getSelectorInFile();
    if (selectorInFile instanceof PsiElement) {
      PsiElement element = (PsiElement)selectorInFile;
      return element.getContainingFile() instanceof XmlFile ? element : null;
    }

    return null;
  }

  @Override
  public void selectIn(SelectInContext context, boolean requestFocus) {
    UsageTrigger.trigger("FlashUIDesigner.selectIn");

    final PsiElement element = getPsiElement(context);
    if (element == null) {
      return;
    }

    DesignerApplicationManager.getInstance().renderIfNeed((XmlFile)element.getContainingFile(), new AsyncResult.Handler<DocumentInfo>() {
      @Override
      public void run(DocumentInfo info) {
        int componentId = 0;
        if (!(element instanceof XmlFile)) {
          PsiElement effectiveElement = element;
          do {
            if (effectiveElement instanceof XmlTag && ((XmlTag)effectiveElement).getDescriptor() instanceof ClassBackedElementDescriptor) {
              componentId = info.rangeMarkerIndexOf(effectiveElement);
              if (componentId != -1) {
                break;
              }
            }

            effectiveElement = effectiveElement.getContext();
          }
          while (effectiveElement != null);

          if (componentId == -1) {
            componentId = 0; // select root
            LogMessageUtil.LOG.warn("Can't find target component");
          }
        }

        Client.getInstance().selectComponent(info.getId(), componentId);
      }
    });
  }

  @Override
  public String getToolWindowId() {
    return null;
  }

  @Override
  public String getMinorViewId() {
    return null;
  }

  @Override
  public float getWeight() {
    return 0;
  }

  @Override
  public String toString() {
    return FlashUIDesignerBundle.message("select.in.designer");
  }
}
