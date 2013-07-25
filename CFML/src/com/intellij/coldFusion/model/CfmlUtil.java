/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.model;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.coldFusion.model.info.CfmlAttributeDescription;
import com.intellij.coldFusion.model.info.CfmlLangInfo;
import com.intellij.coldFusion.model.info.CfmlTagDescription;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.psi.CfmlImport;
import com.intellij.coldFusion.model.psi.CfmlReferenceExpression;
import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl;
import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Created by Lera Nikolaenko
 * Date: 20.10.2008
 */
public class CfmlUtil {
  @Nullable
  public static VirtualFile findFileByLibTag(PsiFile originalFile, @NotNull String libtag) {
    VirtualFile base = getRealVirtualFile(originalFile);
    final Module module = base == null ? null : ModuleUtilCore.findModuleForFile(base, originalFile.getProject());
    base = module == null ? null : module.getModuleFile();
    base = base == null ? null : base.getParent();

    if (libtag != null && libtag.startsWith("/")) {
      libtag = libtag.substring("/".length());
    }

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      final VirtualFile virtualFile = getRealVirtualFile(originalFile);
      assert virtualFile != null;
      base = virtualFile.getParent();
    }

    return VfsUtil.findRelativeFile(base, libtag.split("/"));
  }

  @Nullable
  public static CfmlImport getImportByPrefix(@Nullable PsiElement context, @Nullable final String prefix) {
    if (prefix == null || context == null) {
      return null;
    }
    final CfmlImport[] cfmlImports = PsiTreeUtil.getChildrenOfType(context.getContainingFile(), CfmlImport.class);
    if (cfmlImports == null) {
      return null;
    }
    return ContainerUtil.find(cfmlImports, new Condition<CfmlImport>() {
      @Override
      public boolean value(CfmlImport anImport) {
        return prefix.equalsIgnoreCase(anImport.getPrefix());
      }
    });
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
    return CfmlLangInfo.getInstance(project).getTagAttributes().keySet();
  }

  @NotNull
  private static Project anyProject(Project project) {
    if (project != null) return project;
    Project[] projects = ProjectManager.getInstance().getOpenProjects();
    if (projects.length != 0) return projects[0];
    return ProjectManager.getInstance().getDefaultProject();
  }

  public static boolean hasAnyAttributes(String tagName, Project project) {
    if (isUserDefined(tagName)) {
      return true;
    }
    if (CfmlLangInfo.getInstance(anyProject(project)).getTagAttributes().get(tagName) != null &&
        CfmlLangInfo.getInstance(anyProject(project)).getTagAttributes().get(tagName).getAttributes() != null) {
      return CfmlLangInfo.getInstance(anyProject(project)).getTagAttributes().get(tagName).getAttributes().size() != 0;
    }
    return false;
  }

  public static Collection<CfmlAttributeDescription> getAttributes(String tagName, Project project) {
    if (CfmlLangInfo.getInstance(anyProject(project)).getTagAttributes().get(tagName) != null &&
        CfmlLangInfo.getInstance(anyProject(project)).getTagAttributes().get(tagName).getAttributes() != null) {
      return Collections
        .unmodifiableCollection(CfmlLangInfo.getInstance(anyProject(project)).getTagAttributes().get(tagName).getAttributes());
    }
    return Collections.emptyList();
  }

  public static boolean isStandardTag(String tagName, Project project) {
    return CfmlLangInfo.getInstance(anyProject(project)).getTagAttributes().containsKey(tagName);
  }

  public static boolean isUserDefined(String tagName) {
    return tagName != null && (tagName.toLowerCase().startsWith("cf_") || tagName.contains(":"));
  }

  public static boolean isSingleCfmlTag(String tagName, Project project) {
    if (isUserDefined(tagName)) {
      return false;
    }
    if (!CfmlLangInfo.getInstance(anyProject(project)).getTagAttributes().containsKey(tagName)) {
      return false;
    }
    return !CfmlLangInfo.getInstance(anyProject(project)).getTagAttributes().get(tagName).isEndTagRequired() &&
           CfmlLangInfo.getInstance(anyProject(project)).getTagAttributes().get(tagName).isSingle();
  }

  public static boolean isEndTagRequired(String tagName, Project project) {
    if (!CfmlLangInfo.getInstance(anyProject(project)).getTagAttributes().containsKey(tagName)) {
      return true;
    }
    return CfmlLangInfo.getInstance(anyProject(project)).getTagAttributes().get(tagName).isEndTagRequired();
  }

  public static String getTagDescription(String tagName, Project project) {
    if (!CfmlLangInfo.getInstance(anyProject(project)).getTagAttributes().containsKey(tagName)) {
      return null;
    }
    CfmlTagDescription a = CfmlLangInfo.getInstance(anyProject(project)).getTagAttributes().get(tagName);
    return "<div>Name: " +
           tagName +
           "</div>" +
           "<div>IsEndTagRequired: " +
           a.isEndTagRequired() +
           "</div>" +
           "<div>Descriprion: " +
           a.getDescription() +
           "</div>" +
           "<div>For more information visit <a href = \"http://livedocs.adobe.com/coldfusion/8/htmldocs/Tags-pt0_01.html\">" +
           "\"http://livedocs.adobe.com/coldfusion/8/htmldocs/Tags-pt0_01.html\"</div>";
  }

  public static String getAttributeDescription(String tagName, String attributeName, Project project) {
    CfmlAttributeDescription af = getAttribute(tagName, attributeName, project);
    if (af == null) {
      return "";
    }
    return af.toString();
  }

  @Nullable
  public static CfmlAttributeDescription getAttribute(String tagName, String attributeName, Project project) {
    CfmlTagDescription tagDescription = CfmlLangInfo.getInstance(anyProject(project)).getTagAttributes().get(tagName);
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

    final String name = tokenText.toLowerCase();
    final boolean keyword = name.equals("param") ||
                            name.equals("lock") ||
                            name.equals("transaction") ||
                            name.equals("writelog") ||
                            name.equals("savecontent");
    return keyword && checkAheadActionTokens(builder.lookAhead(1), builder.lookAhead(2));
  }

  private static boolean checkAheadActionTokens(@Nullable IElementType second, @Nullable IElementType third) {
    return second == CfscriptTokenTypes.IDENTIFIER || second == CfscriptTokenTypes.L_CURLYBRACKET
           || (second == CfmlTokenTypes.ASSIGN && third == CfscriptTokenTypes.L_CURLYBRACKET);
  }

  public static String[] getPredifinedFunctions(Project project) {
    return CfmlLangInfo.getInstance(anyProject(project)).getPredefinedFunctions();
  }

  public static boolean isPredefinedFunction(String functionName, Project project) {
    return ArrayUtil.find(CfmlLangInfo.getInstance(anyProject(project)).getPredefinedFunctionsInLowCase(), functionName.toLowerCase()) !=
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
      CfmlLangInfo.getInstance(anyProject(project)).getPredefinedVariables().keySet()
        .contains(tagNameWithoutCf.toLowerCase() + "." + predefVarText
          .toLowerCase());
  }

  private static String[] EMPTY_STRING_ARRAY = ArrayUtil.EMPTY_STRING_ARRAY;

  @NotNull
  public static String[] getAttributeValues(String tagName, String attributeName, Project project) {
    CfmlAttributeDescription attribute = getAttribute(tagName, attributeName, project);
    if (attribute != null) {
      String[] values = attribute.getValues();
      return values != null ? values : EMPTY_STRING_ARRAY;
    }
    return EMPTY_STRING_ARRAY;
  }

  @NotNull
  public static String[] getCreateObjectArgumentValues() {
    return new String[]{"component", "java", "com", "corba",
      "webservice"}; //http://livedocs.adobe.com/coldfusion/8/htmldocs/help.html?content=functions_c-d_15.html
  }

  public static String[] getVariableScopes(Project project) {
    return CfmlLangInfo.getInstance(anyProject(project)).getVariableScopes();
  }

  @NotNull
  public static String getFileName(PsiElement element) {
    final String fileName = element.getContainingFile().getName();
    if (fileName.indexOf('.') == -1) {
      return fileName;
    }
    return fileName.substring(0, fileName.indexOf('.'));
  }

  public static void showCompletion(Editor editor) {
    AutoPopupController.getInstance(editor.getProject()).autoPopupMemberLookup(editor, null);
  }

  @Nullable
  public static VirtualFile getRealVirtualFile(PsiFile psiFile) {
    return psiFile.getOriginalFile().getVirtualFile();
  }

  public static Pair<String, String> getPrefixAndName(String name) {
    if (name == null) {
      return Pair.empty();
    }
    final int index = name.indexOf(':');
    if (index == -1) {
      return Pair.create(null, name);
    }
    return Pair.create(name.substring(0, index), name.substring(index + 1));
  }
}
