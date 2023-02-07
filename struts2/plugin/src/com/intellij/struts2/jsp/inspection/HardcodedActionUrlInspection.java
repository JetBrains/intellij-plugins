/*
 * Copyright 2015 The authors
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
package com.intellij.struts2.jsp.inspection;

import com.intellij.codeInsight.completion.ExtendedTagInsertHandler;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.struts2.model.constant.StrutsConstantHelper;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlNamespaceHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Collections;

/*
 * @author max
 * @author Yann C&eacute;bron
 */
public class HardcodedActionUrlInspection extends XmlSuppressableInspectionTool {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    final boolean isJspFileWithStrutsSupport =
      JspPsiUtil.getJspFile(holder.getFile()) != null &&
      StrutsFacet.getInstance(holder.getFile()) != null;

    @Nullable final String actionExtension;
    if (isJspFileWithStrutsSupport) {
      actionExtension = ContainerUtil.getFirstItem(StrutsConstantHelper.getActionExtensions(holder.getFile()));
    }
    else {
      actionExtension = null;
    }

    return new XmlElementVisitor() {

      @Override
      public void visitXmlAttributeValue(@NotNull XmlAttributeValue value) {
        if (!isJspFileWithStrutsSupport ||
            actionExtension == null) {
          return;
        }

        XmlTag tag = PsiTreeUtil.getParentOfType(value, XmlTag.class);
        if (tag == null) return;

        URL parsedURL = parseURL(value, actionExtension);
        if (parsedURL == null) return;

        if (buildTag("", parsedURL, "", false, actionExtension) == null) return;

        TextRange range = ElementManipulators.getValueTextRange(value);
        holder.registerProblem(value, range, "Use Struts <url> tag instead of hardcoded URL", new WrapWithSUrl(actionExtension));
      }
    };
  }

  @Override
  public String @NotNull [] getGroupPath() {
    return new String[]{StrutsBundle.message("inspections.group.path.name"), getGroupDisplayName()};
  }


  private static final class WrapWithSUrl implements LocalQuickFix {

    private final String myActionExtension;

    private WrapWithSUrl(String actionExtension) {
      myActionExtension = actionExtension;
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return "Wrap with Struts <url> tag";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      PsiElement element = descriptor.getPsiElement();
      if (element instanceof XmlAttributeValue value) {
        XmlTag tag = PsiTreeUtil.getParentOfType(value, XmlTag.class, false);

        final boolean inline = tag instanceof HtmlTag;

        final URL url = parseURL(value, myActionExtension);
        if (url == null) {
          return;
        }

        final JspFile jspFile = JspPsiUtil.getJspFile(value);
        assert jspFile != null;

        XmlTag rootTag = jspFile.getRootTag();
        String prefix = rootTag.getPrefixByNamespace(StrutsConstants.TAGLIB_STRUTS_UI_URI);

        if (StringUtil.isEmpty(prefix)) {
          XmlNamespaceHelper extension = XmlNamespaceHelper.getHelper(jspFile);
          prefix = ExtendedTagInsertHandler.suggestPrefix(jspFile, StrutsConstants.TAGLIB_STRUTS_UI_URI);
          XmlNamespaceHelper.Runner<String, IncorrectOperationException> after =
            new XmlNamespaceHelper.Runner<>() {
              @Override
              public void run(String param) throws IncorrectOperationException {
                wrapValue(param, value, url, inline);
              }
            };
          extension.insertNamespaceDeclaration(jspFile, null, Collections.singleton(StrutsConstants.TAGLIB_STRUTS_UI_URI), prefix, after);
        }
        else {
          wrapValue(prefix, value, url, inline);
        }
      }
    }

    private void wrapValue(String prefix, XmlAttributeValue value, URL url, boolean inline) {
      final JspFile jspFile = JspPsiUtil.getJspFile(value);
      assert jspFile != null;

      Project project = jspFile.getProject();
      TextRange range = value.getValueTextRange();
      Document document = PsiDocumentManager.getInstance(project).getDocument(jspFile);
      assert document != null;
      PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);

      int start = range.getStartOffset();
      int lineStart = document.getLineStartOffset(document.getLineNumber(start));
      String linePrefix = document.getCharsSequence().subSequence(lineStart, start).toString();
      linePrefix = linePrefix.substring(0, linePrefix.length() - linePrefix.trim().length());

      String indent = linePrefix;
      while (indent.length() < start - lineStart) indent += " ";

      Pair<String, String> tag_var = buildTag(prefix, url, indent, inline, myActionExtension);
      String tag = tag_var.getFirst();
      String var = tag_var.getSecond();

      int end = range.getEndOffset();

      int formattingStart;
      int formattingEnd;

      if (inline) {
        document.replaceString(start, end, tag);
        formattingStart = start;
        formattingEnd = start + tag.length();
      }
      else {
        document.replaceString(start, end, "${" + var + "}");
        XmlTag containingTag = PsiTreeUtil.getParentOfType(value, XmlTag.class, false);
        assert containingTag != null;
        int startOffset = containingTag.getTextRange().getStartOffset();
        document.insertString(startOffset, "\n");
        document.insertString(startOffset, tag);

        formattingStart = startOffset;
        formattingEnd = startOffset + tag.length() + 2;
      }

      PsiDocumentManager.getInstance(project).commitDocument(document);

      CodeStyleManager.getInstance(project).reformatText(jspFile, formattingStart, formattingEnd);
    }
  }

  private static Pair<String, String> buildTag(String prefix, URL url, String indent, boolean inline, String actionExtension) {
    String path = url.getPath();
    int slash = path.lastIndexOf('/');
    String namespace = slash > 0 ? path.substring(0, slash) : null;
    String action = slash != -1 ? path.substring(slash + 1) : path;

    action = StringUtil.trimEnd(action, actionExtension);

    int exclamationIdx = action.indexOf('!');
    String method = null;
    if (exclamationIdx > 0) {
      method = action.substring(exclamationIdx + 1);
      action = action.substring(0, exclamationIdx);
    }

    StringBuilder sb = new StringBuilder();
    sb.append('<').append(prefix).append(":url");

    String var;
    if (inline) {
      var = null;
    }
    else {
      var = action + "_url";
      sb.append(" var=\"").append(var).append("\"");
    }

    if (namespace != null) {
      sb.append(" namespace=\"").append(namespace).append("\"");
    }

    sb.append(" action=\"").append(action).append("\"");
    if (method != null) {
      sb.append(" method=\"").append(method).append("\"");
    }

    String query = url.getQuery();
    if (StringUtil.isEmpty(query)) {
      sb.append("/>");
    }
    else {
      sb.append(">");

      for (String escapedArg : StringUtil.split(query, "&amp;")) {
        for (String arg : StringUtil.split(escapedArg, "&")) {
          int eq = arg.indexOf('=');
          String name = eq > 0 ? arg.substring(0, eq) : arg;
          String value = eq > 0 ? arg.substring(eq + 1) : "";

          if (name.contains("[") || name.contains("$")) return null; // This will not work if arg name is actually an expression

          sb.append("\n").append(indent).append("  <")
            .append(prefix)
            .append(":param name=\"")
            .append(name).append("\">")
            .append(value)
            .append("</")
            .append(prefix)
            .append(":param>");
        }
      }
      sb.append('\n').append(indent);
      sb.append("</").append(prefix).append(":url>");
    }

    return Pair.create(sb.toString(), var);
  }

  @Nullable
  private static URL parseURL(XmlAttributeValue value, String actionExtension) {
    String rawUrl = value.getValue();
    if (rawUrl.startsWith("http://") ||
        rawUrl.startsWith("https://")) {
      return null;
    }

    URL parsedURL;
    try {
      parsedURL = new URL("http://" + rawUrl);
    }
    catch (Exception e) {
      return null;
    }

    String host = parsedURL.getHost();
    if (!StringUtil.isEmpty(host) &&
        !(host.startsWith("${") && host.endsWith("}"))) {
      return null;
    }

    String path = parsedURL.getPath();
    if (!path.endsWith(actionExtension)) {
      return null;
    }

    if (path.contains("${")) {
      return null; // Dynamic action paths cannot be converted.
    }

    return parsedURL;
  }
}
