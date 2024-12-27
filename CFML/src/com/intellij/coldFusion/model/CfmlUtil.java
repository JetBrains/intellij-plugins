// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.coldFusion.model.info.CfmlAttributeDescription;
import com.intellij.coldFusion.model.info.CfmlLangInfo;
import com.intellij.coldFusion.model.info.CfmlTagDescription;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlKeywords;
import com.intellij.coldFusion.model.psi.CfmlImport;
import com.intellij.coldFusion.model.psi.CfmlReferenceExpression;
import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl;
import com.intellij.lang.Language;
import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Created by Lera Nikolaenko
 */
public final class CfmlUtil {

  public static @Nullable Language getSqlLanguage() {
    return Language.findLanguageByID("SQL");
  }

  public static @Nullable VirtualFile findFileByLibTag(PsiFile originalFile, @NotNull String libtag) {
    VirtualFile base = getRealVirtualFile(originalFile);
    final Module module = base == null ? null : ModuleUtilCore.findModuleForFile(base, originalFile.getProject());
    base = module == null ? null : module.getModuleFile();
    base = base == null ? null : base.getParent();

    libtag = StringUtil.trimStart(libtag, "/");

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      final VirtualFile virtualFile = getRealVirtualFile(originalFile);
      assert virtualFile != null;
      base = virtualFile.getParent();
    }

    return VfsUtil.findRelativeFile(base, libtag.split("/"));
  }

  public static @Nullable CfmlImport getImportByPrefix(@Nullable PsiElement context, final @Nullable String prefix) {
    if (prefix == null || context == null) {
      return null;
    }
    final CfmlImport[] cfmlImports = PsiTreeUtil.getChildrenOfType(context.getContainingFile(), CfmlImport.class);
    if (cfmlImports == null) {
      return null;
    }
    return ContainerUtil.find(cfmlImports, anImport -> prefix.equalsIgnoreCase(anImport.getPrefix()));
  }

  public static boolean isSearchedScope(String scopeText) {
    return scopeText.equalsIgnoreCase("variables") ||
           scopeText.equalsIgnoreCase("arguments") ||
           scopeText.equalsIgnoreCase("url") ||
           scopeText.equalsIgnoreCase("form");
  }

  public static boolean hasEqualScope(CfmlReferenceExpression ref1, CfmlReferenceExpression ref2) {
    if (ref1.getScope() == null && ref2.getScope() == null) {
      return true;
    }
    else if (ref2.getScope() == null) {
      return CfmlUtil.isSearchedScope(ref1.getScope().getText());
    }
    else if (ref1.getScope() == null) {
      return CfmlUtil.isSearchedScope(ref2.getScope().getText());
    }
    else if (ref2.getScope().getText().equalsIgnoreCase(ref1.getScope().getText())) {
      return true;
    }
    return false;
  }

  public static Set<String> getTagList(@NotNull Project project) {
    return getCfmlLangInfo(project).getTagAttributes().keySet();
  }

  /**
   * Use only if {@code ApplicationManager.getApplication() != null}
   */
  private static @NotNull Project anyProject(Project project) {
    if (project != null) return project;
    Project[] projects = ProjectManager.getInstance().getOpenProjects();
    if (projects.length != 0) return projects[0];
    return ProjectManager.getInstance().getDefaultProject();
  }

  public static boolean hasAnyAttributes(String tagName, Project project) {
    if (isUserDefined(tagName)) {
      return true;
    }
    if (getCfmlLangInfo(project).getTagAttributes().get(tagName) != null &&
        getCfmlLangInfo(project).getTagAttributes().get(tagName).getAttributes() != null) {
      return !getCfmlLangInfo(project).getTagAttributes().get(tagName).getAttributes().isEmpty();
    }
    return false;
  }

  public static CfmlLangInfo getCfmlLangInfo(Project project) {
    if (ApplicationManager.getApplication() == null) return CfmlLangInfo.getInstance(null);
    return CfmlLangInfo.getInstance(anyProject(project));
  }

  public static Collection<CfmlAttributeDescription> getAttributes(String tagName, Project project) {
    if (getCfmlLangInfo(project).getTagAttributes().get(tagName) != null &&
        getCfmlLangInfo(project).getTagAttributes().get(tagName).getAttributes() != null) {
      return Collections
        .unmodifiableCollection(getCfmlLangInfo(project).getTagAttributes().get(tagName).getAttributes());
    }
    return Collections.emptyList();
  }

  public static boolean isStandardTag(String tagName, Project project) {
    return getCfmlLangInfo(project).getTagAttributes().containsKey(tagName);
  }

  public static boolean isUserDefined(String tagName) {
    return tagName != null && (StringUtil.toLowerCase(tagName).startsWith("cf_") || tagName.contains(":"));
  }

  public static boolean isSingleCfmlTag(String tagName, Project project) {
    if (isUserDefined(tagName)) {
      return false;
    }
    if (!getCfmlLangInfo(project).getTagAttributes().containsKey(tagName)) {
      return false;
    }
    return !getCfmlLangInfo(project).getTagAttributes().get(tagName).isEndTagRequired() &&
           getCfmlLangInfo(project).getTagAttributes().get(tagName).isSingle();
  }

  public static boolean isEndTagRequired(String tagName, Project project) {
    if (!getCfmlLangInfo(project).getTagAttributes().containsKey(tagName)) {
      return true;
    }
    return getCfmlLangInfo(project).getTagAttributes().get(tagName).isEndTagRequired();
  }

  public static String getTagDescription(String tagName, Project project) {
    return CfmlDocUtil.tagDescription(tagName, project);
  }

  public static String getAttributeDescription(String tagName, String attributeName, Project project) {
    CfmlAttributeDescription cfmlAttributeDescription = getAttribute(tagName, attributeName, project);
    return CfmlDocUtil.attributeDescription(tagName, cfmlAttributeDescription, project);
  }

  public static @Nullable CfmlAttributeDescription getAttribute(String tagName, String attributeName, Project project) {
    CfmlTagDescription tagDescription = getCfmlLangInfo(project).getTagAttributes().get(tagName);
    if (tagDescription == null) return null;
    final Collection<CfmlAttributeDescription> attributesCollection = tagDescription.getAttributes();
    for (CfmlAttributeDescription af : attributesCollection) {
      if (af.acceptName(attributeName)) {
        return af;
      }
    }
    return null;
  }

  public static boolean isControlToken(IElementType type) {
    return type == CfmlTokenTypes.OPENER ||
           type == CfmlTokenTypes.CLOSER ||
           type == CfmlTokenTypes.LSLASH_ANGLEBRACKET ||
           type == CfmlTokenTypes.R_ANGLEBRACKET ||
           type == CfscriptTokenTypes.L_CURLYBRACKET ||
           type == CfscriptTokenTypes.SEMICOLON;
  }

  public static boolean isActionName(PsiBuilder builder) {
    final String tokenText = builder.getTokenText();
    if (tokenText == null) {
      return false;
    }

    final String name = StringUtil.toLowerCase(tokenText);
    boolean isKeyword = Arrays
      .stream(CfmlKeywords.values())
      .anyMatch(cfmlKeyword -> cfmlKeyword.getKeyword().equals(name));
    return isKeyword && checkAheadActionTokens(builder.lookAhead(1), builder.lookAhead(2));
  }

  private static boolean checkAheadActionTokens(@Nullable IElementType second, @Nullable IElementType third) {
    return second == CfscriptTokenTypes.IDENTIFIER || second == CfscriptTokenTypes.L_CURLYBRACKET
           || (second == CfmlTokenTypes.ASSIGN && third == CfscriptTokenTypes.L_CURLYBRACKET);
  }

  public static String[] getPredifinedFunctions(Project project) {
    return getCfmlLangInfo(project).getPredefinedFunctions();
  }

  public static boolean isPredefinedFunction(String functionName, Project project) {
    return ArrayUtil.find(getCfmlLangInfo(project).getPredefinedFunctionsInLowCase(), StringUtil.toLowerCase(functionName)) !=
           -1;
  }

  public static boolean isPredefinedTagVariables(CfmlReferenceExpression cfmlRef, Project project) {
    String predefVarText = cfmlRef.getLastChild() != null ? cfmlRef.getLastChild().getText() : null;
    //try to find tag type by name//
    PsiElement referenceName = cfmlRef.getFirstChild();
    if (!(referenceName instanceof CfmlReferenceExpression) || predefVarText == null) {
      return false;
    }
    referenceName = ((CfmlReferenceExpression)referenceName).resolve();
    referenceName = referenceName != null ? referenceName.getParent() : null;
    if (!(referenceName instanceof CfmlTagImpl)) {
      return false;
    }
    String tagName = ((CfmlTagImpl)referenceName).getTagName();
    String tagNameWithoutCf = tagName.startsWith("cf") ? tagName.substring(2) : tagName;
    return
      getCfmlLangInfo(project).getPredefinedVariables()
        .containsKey(StringUtil.toLowerCase(tagNameWithoutCf) + "." + StringUtil.toLowerCase(predefVarText));
  }

  private static final String[] EMPTY_STRING_ARRAY = ArrayUtilRt.EMPTY_STRING_ARRAY;

  public static String @NotNull [] getAttributeValues(String tagName, String attributeName, Project project) {
    CfmlAttributeDescription attribute = getAttribute(tagName, attributeName, project);
    if (attribute != null) {
      String[] values = attribute.getValues();
      return values != null ? values : EMPTY_STRING_ARRAY;
    }
    return EMPTY_STRING_ARRAY;
  }

  public static String @NotNull [] getCreateObjectArgumentValues() {
    return new String[]{"component", "java", "com", "corba",
      "webservice"}; //http://livedocs.adobe.com/coldfusion/8/htmldocs/help.html?content=functions_c-d_15.html
  }

  public static String[] getVariableScopes(Project project) {
    return getCfmlLangInfo(project).getVariableScopes();
  }

  public static @NotNull String getFileName(PsiElement element) {
    final String fileName = element.getContainingFile().getName();
    if (fileName.indexOf('.') == -1) {
      return fileName;
    }
    return fileName.substring(0, fileName.indexOf('.'));
  }

  public static void showCompletion(Editor editor) {
    AutoPopupController.getInstance(editor.getProject()).autoPopupMemberLookup(editor, null);
  }

  public static @Nullable VirtualFile getRealVirtualFile(PsiFile psiFile) {
    return psiFile.getOriginalFile().getVirtualFile();
  }

  public static Couple<String> getPrefixAndName(String name) {
    if (name == null) {
      return Couple.getEmpty();
    }
    final int index = name.indexOf(':');
    if (index == -1) {
      return Couple.of(null, name);
    }
    return Couple.of(name.substring(0, index), name.substring(index + 1));
  }
}
