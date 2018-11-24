package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.*;
import org.jetbrains.plugins.cucumber.psi.i18n.JsonGherkinKeywordProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
public class GherkinFileImpl extends PsiFileBase implements GherkinFile {
  public GherkinFileImpl(FileViewProvider viewProvider) {
    super(viewProvider, GherkinLanguage.INSTANCE);
  }

  @NotNull
  public FileType getFileType() {
    return GherkinFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "GherkinFile:" + getName();
  }

  public List<String> getStepKeywords() {
    final GherkinKeywordProvider provider = JsonGherkinKeywordProvider.getKeywordProvider();
    List<String> result = new ArrayList<>();

    // find language comment
    final String language = getLocaleLanguage();

    // step keywords
    final GherkinKeywordTable table = provider.getKeywordsTable(language);
    result.addAll(table.getStepKeywords());

    return result;
  }

  public String getLocaleLanguage() {
    final ASTNode node = getNode();

    ASTNode child = node.getFirstChildNode();
    while (child != null) {
      if (child.getElementType() == GherkinTokenTypes.COMMENT) {
        final String text = child.getText().substring(1).trim();

        final String lang = GherkinLexer.fetchLocationLanguage(text);
        if (lang != null) {
          return lang;
        }
      } else {
        if (child.getElementType() != TokenType.WHITE_SPACE) {
          break;
        }
      }
      child = child.getTreeNext();
    }
    return getDefaultLocale();
  }

  @Override
  public GherkinFeature[] getFeatures() {
    return findChildrenByClass(GherkinFeature.class);
  }

  public static String getDefaultLocale() {
    return "en";
  }

  @Override
  public PsiElement findElementAt(int offset) {
    PsiElement result = super.findElementAt(offset);
    if (result == null && offset == getTextLength()) {
      final PsiElement last = getLastChild();
      result = last != null ? last.getLastChild() : last;
    }
    return result;
  }
}
