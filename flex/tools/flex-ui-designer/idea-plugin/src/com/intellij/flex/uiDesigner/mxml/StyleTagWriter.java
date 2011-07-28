package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.FlexUIDesignerBundle;
import com.intellij.flex.uiDesigner.InjectionUtil;
import com.intellij.flex.uiDesigner.ProblemsHolder;
import com.intellij.flex.uiDesigner.css.CssWriter;
import com.intellij.flex.uiDesigner.css.LocalCssWriter;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.css.CssFile;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StyleTagWriter {
  private final CssWriter cssWriter;
  private final ProblemsHolder problemsHolder;

  public StyleTagWriter(StringRegistry.StringWriter stringWriter, ProblemsHolder problemsHolder) {
    this.problemsHolder = problemsHolder;
    cssWriter = new LocalCssWriter(stringWriter);
  }

  public byte[] write(XmlTag tag, Module module) {
    CssFile cssFile = null;
    XmlAttribute source = tag.getAttribute("source");
    if (source != null) {
      XmlAttributeValue valueElement = source.getValueElement();
      if (valueElement != null) {
        final PsiFileSystemItem psiFile = InjectionUtil.getReferencedPsiFile(valueElement, problemsHolder, true);
        if (psiFile != null) {
          if (psiFile instanceof CssFile) {
            cssFile = (CssFile)psiFile;
          }
          else {
            problemsHolder.add(FlexUIDesignerBundle.message("error.embed.source.is.not.css.file", psiFile.getName()));
          }
        }
      }
    }
    else {
      PsiElement host = MxmlUtil.getInjectedHost(tag);
      if (host != null) {
        InjectedPsiVisitor visitor = new InjectedPsiVisitor(host);
        InjectedLanguageUtil.enumerate(host, visitor);
        cssFile = visitor.getCssFile();
      }
    }

    return cssFile == null ? null : cssWriter.write(cssFile, module, problemsHolder);
  }

  private static class InjectedPsiVisitor implements PsiLanguageInjectionHost.InjectedPsiVisitor {
    private final PsiElement host;
    private boolean visited;

    private CssFile cssFile;

    public InjectedPsiVisitor(PsiElement host) {
      this.host = host;
    }

    @Nullable
    public CssFile getCssFile() {
      return cssFile;
    }

    public void visit(@NotNull PsiFile injectedPsi, @NotNull List<PsiLanguageInjectionHost.Shred> places) {
      assert !visited;
      visited = true;

      assert places.size() == 1;
      assert places.get(0).host == host;
      cssFile = (CssFile)injectedPsi;
    }
  }
}
