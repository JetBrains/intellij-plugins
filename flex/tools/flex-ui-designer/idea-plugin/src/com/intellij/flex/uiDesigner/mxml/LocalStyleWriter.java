package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.CssWriter;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.css.CssFile;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LocalStyleWriter {
  private byte[] data;
  private final CssWriter cssWriter;

  public LocalStyleWriter(StringRegistry.StringWriter stringWriter) {
    cssWriter = new CssWriter(stringWriter);
  }

  public @NotNull byte[] getData() {
    return data;
  }
          
  public boolean write(XmlTag tag, Module module) {
    data = null;
    
    PsiElement host = XmlTagValueProvider.getInjectedHost(tag);
    if (host == null) {
      return false;
    }
    
    InjectedPsiVisitor visitor = new InjectedPsiVisitor(host);
    InjectedLanguageUtil.enumerate(host, visitor);
    if (visitor.getCssFile() == null) {
      return false;
    }

    data = cssWriter.write(visitor.getCssFile(), module);
    return true;
  }

  private static class InjectedPsiVisitor implements PsiLanguageInjectionHost.InjectedPsiVisitor {
    private final PsiElement host;
    private boolean visited;

    private CssFile cssFile;

    public InjectedPsiVisitor(PsiElement host) {
      this.host = host;
    }

    public @Nullable CssFile getCssFile() {
      return cssFile;
    }

    public void visit(@NotNull PsiFile injectedPsi, @NotNull List<PsiLanguageInjectionHost.Shred> places) {
      assert !visited;
      visited = true;

      assert places.size() == 1;
      assert places.get(0).host == host;
      cssFile = (CssFile) injectedPsi;
    }
  }
}
