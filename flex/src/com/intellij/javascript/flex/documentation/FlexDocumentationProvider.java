// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.documentation;

import com.intellij.codeInsight.documentation.AbstractExternalFilter;
import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.codeInsight.documentation.DocumentationManagerProtocol;
import com.intellij.codeInsight.documentation.PlatformDocumentationUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.lang.javascript.documentation.JSDocumentationBuilder;
import com.intellij.lang.javascript.documentation.JSDocumentationProvider;
import com.intellij.lang.javascript.documentation.JSQuickNavigateBuilder;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.jsdoc.impl.JSDocReferenceSet;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.types.primitives.JSVoidType;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FlexDocumentationProvider extends JSDocumentationProvider {

  private static final Pattern ourMainContentDiv = Pattern.compile("<div.*?class=\"MainContent\">", Pattern.CASE_INSENSITIVE);
  private static final Pattern ourDetailBodyDiv = Pattern.compile("<div.*?class=\"detailBody\">", Pattern.CASE_INSENSITIVE);
  private static final Pattern ourEventDetailTd = Pattern.compile("<td.*?class=\"summaryTableDescription\">", Pattern.CASE_INSENSITIVE);
  private static final Pattern ourOpeningDiv = Pattern.compile("<div.*?>", Pattern.CASE_INSENSITIVE);
  private static final Pattern ourClosingDiv = Pattern.compile("</div>", Pattern.CASE_INSENSITIVE);
  private static final Pattern ourOpeningTd = Pattern.compile("<td.*?>", Pattern.CASE_INSENSITIVE);
  private static final Pattern ourClosingTd = Pattern.compile("</td>", Pattern.CASE_INSENSITIVE);
  private static final Pattern ourSeeAlsoDiv = Pattern
    .compile("<p>[ \r\n\t]*?<span .*?>See also</span>[ \r\n\t]*?</p>[ \r\n\t]*?<div .*?class=\"seeAlso\".*?>", Pattern.CASE_INSENSITIVE);
  private static final Pattern ourLabelSpan =
    Pattern.compile("<span .*?class=\"label\".*?>(.*?)</span>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern[] ourStrip =
    new Pattern[]{Pattern.compile("<span[^>]*style=\"display:none\"[^>]*>.*?</span>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
      Pattern.compile("<p></p>", Pattern.CASE_INSENSITIVE), Pattern.compile("<hr>", Pattern.CASE_INSENSITIVE),
      Pattern.compile("[ ]?class=\".*?\"", Pattern.CASE_INSENSITIVE), Pattern.compile("<!--.*?-->"),
      Pattern.compile("<script.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
      Pattern.compile("<a[^>]*onclick=[^>]*>.*?</a>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)};
  private static final Pattern ourLinebreakInText = Pattern.compile("([^ \t\r\n>][ \t]*)([\r\n]+[ \t]*[^ \t\r\n<])");
  private static final Pattern ourLinebreakBeforeCode =
    Pattern.compile("(</code>[ \t]*?)([\r\n]+[ \t]*?[^ \t\r\n<])", Pattern.CASE_INSENSITIVE);
  private static final Pattern ourLinebreakAfterCode =
    Pattern.compile("([^ \t\r\n>][ \t]*)([\r\n]+[ \t]*?<code>)", Pattern.CASE_INSENSITIVE);
  private static final String ourInsertBr = "$1<br>$2";
  private static final String DISPLAY_NAME_MARKER = "$$$$DISPLAY_NAME$$$$";
  private final Pattern ourDetailHeaderTable =
    Pattern.compile("<table .*?class=\"classHeaderTable\".*?>.*?</table>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern ourClassHeaderTable =
    Pattern.compile("<table .*?class=\"classHeaderTable\".*?>.*?</table>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern ourLinkPattern = Pattern.compile("<a.*?href=\"(.*?)\".*?>(.*?)</a>", Pattern.CASE_INSENSITIVE);
  private static final @NonNls Pattern ourHREFselector = Pattern.compile("<a.*?href=\"([^>\"]*)\"", Pattern.CASE_INSENSITIVE);
  private static final @NonNls Pattern ourIMGselector =
    Pattern.compile("<img.*?src=\"([^>\"]*)\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  private static final @NonNls String PACKAGE = "package";
  private static final @NonNls String HTML_EXTENSION = ".html";
  private static final @NonNls String PACKAGE_FILE = PACKAGE + HTML_EXTENSION;

  private static final Map<String, String> DOCUMENTED_ATTRIBUTES;
  static {
    DOCUMENTED_ATTRIBUTES = new HashMap<>();
    DOCUMENTED_ATTRIBUTES.put("Event", "event:");
    DOCUMENTED_ATTRIBUTES.put("Style", "style:");
    DOCUMENTED_ATTRIBUTES.put("Effect", "effect:");
  }

  @Override
  protected @NotNull JSQuickNavigateBuilder createQuickNavigateBuilder() {
    return new FlexQuickNavigateBuilder();
  }

  @Override
  protected @Nullable @Nls String generateDoc(PsiElement _element, @Nullable PsiElement originalElement, @Nullable Ref<String> definitionDetails) {
    String doc = super.generateDoc(_element, originalElement, null);
    if (doc != null && doc.contains(DocumentationMarkup.CONTENT_START)) {
      return doc;
    }

    if (_element instanceof JSOffsetBasedImplicitElement) _element = ((JSOffsetBasedImplicitElement)_element).getElementAtOffset();

    XmlTag parent = null;
    if (_element instanceof XmlBackedJSClassImpl) {
      parent = ((XmlBackedJSClassImpl)_element).getParent();
    } else if (_element instanceof XmlToken) {
      parent = PsiTreeUtil.getParentOfType(_element, XmlTag.class);
    }

    if (parent != null) {
      PsiElement prev = PsiTreeUtil.prevLeaf(parent);
      while(prev instanceof PsiWhiteSpace || (prev instanceof XmlComment && !prev.getText().startsWith("<!---"))) {
        prev = PsiTreeUtil.prevLeaf(prev);
        if (prev instanceof XmlToken) prev = prev.getParent();
      }

      if (prev instanceof XmlComment) {
        final String textFromComment = doGetCommentTextFromComment((PsiComment)prev, originalElement);
        return DocumentationMarkup.DEFINITION_START +
               (_element instanceof PsiNamedElement ?  ((PsiNamedElement)_element).getName() : _element.getText()) +
               DocumentationMarkup.DEFINITION_END +
               textFromComment;
      }
    }

    final PsiElement elementToShowDoc = findElementToShowDoc(_element);
    AbstractExternalFilter docFilter = new AbstractExternalFilter() {

      private final RefConvertor[] myReferenceConvertors = new RefConvertor[]{new RefConvertor(ourHREFselector) {
        @Override
        protected String convertReference(String origin, String link) {
          if (BrowserUtil.isAbsoluteURL(link)) {
            return link;
          }

          String resolved = getSeeAlsoLinkResolved(elementToShowDoc, link);
          if (resolved != null) {
            return DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL + resolved;
          }

          String originFile = ourAnchorSuffix.matcher(origin).replaceAll("");
          if (StringUtil.startsWithChar(link, '#')) {
            return originFile + link;
          }
          else {
            String originPath = originFile.contains("/") ? originFile.substring(0, originFile.lastIndexOf("/")) : originFile;
            return doAnnihilate(originPath + "/" + link);
          }
        }
      }, new RefConvertor(ourIMGselector) {

        @Override
        protected String convertReference(String root, String href) {
          if (StringUtil.startsWithChar(href, '#')) {
            return root + href;
          }

          if (root.startsWith("file://") && SystemInfo.isWindows) {
            root = "file:///" + root.substring("file://".length());
          }
          return doAnnihilate(ourHtmlFileSuffix.matcher(root).replaceAll("/") + href);
        }
      }
      };

      @Override
      protected AbstractExternalFilter.RefConvertor[] getRefConverters() {
        return myReferenceConvertors;
      }

      @Override
      public String getExternalDocInfoForElement(final String docURL, final PsiElement element) throws Exception {
        String result = super.getExternalDocInfoForElement(docURL, element);
        if (StringUtil.isNotEmpty(result)) {
          result = result.replace(DISPLAY_NAME_MARKER, ReadAction.compute(() -> getDisplayName(element)));
        }
        return result;
      }

      @Override
      protected void doBuildFromStream(String url, Reader reader, StringBuilder result) throws IOException {
        String input = StreamUtil.readText(reader);

        Matcher anchorMatcher = ourAnchorSuffix.matcher(url);

        final int startOffset;
        Pair<Pattern, Pattern> mainContentPatterns = Pair.create(ourOpeningDiv, ourClosingDiv);
        if (anchorMatcher.find()) {
          String name = anchorMatcher.group(1);
          Pattern detailPattern = ourDetailBodyDiv;
          for (Map.Entry<String, String> e : DOCUMENTED_ATTRIBUTES.entrySet()) {
            if (name.startsWith(e.getValue())) {
              if (!"Event".equals(e.getKey())) {
                detailPattern = ourEventDetailTd;
                mainContentPatterns = Pair.create(ourOpeningTd, ourClosingTd);
              }
              break;
            }
          }
          name = name.replaceAll("\\)", "\\\\)").replaceAll("\\(", "\\\\(");
          Matcher m = Pattern.compile("<a name=\"" + name + "\"").matcher(input);
          if (!m.find()) {
            return;
          }
          int offset = m.end();
          m = detailPattern.matcher(input);
          if (!m.find(offset)) {
            return;
          }
          startOffset = m.start();
        }
        else {
          Matcher m = ourMainContentDiv.matcher(input);
          if (!m.find()) {
            return;
          }
          startOffset = m.start();
        }

        TextRange description =
          getRangeBetweenNested(input, new TextRange(startOffset, input.length()), mainContentPatterns.first, mainContentPatterns.second);
        if (description == null) {
          return;
        }

        Matcher m = ourSeeAlsoDiv.matcher(input);
        final TextRange seeAlso;
        if (findIn(m, description)) {
          seeAlso = getRangeBetweenNested(input, new TextRange(m.start(), description.getEndOffset()), ourOpeningDiv, ourClosingDiv);
          description = new TextRange(description.getStartOffset(), m.start());
        }
        else {
          seeAlso = null;
        }

        String text = description.substring(input);
        text = ourDetailHeaderTable.matcher(text).replaceAll("");
        text = ourClassHeaderTable.matcher(text).replaceAll("");

        result.append(HTML).append("<PRE><b>").append(DISPLAY_NAME_MARKER);
        result.append("</b></PRE>");
        result.append(prettyPrint(text));

        if (seeAlso != null) {
          result.append("<DL><DT><b>See also:</b></DT>");
          int pos = seeAlso.getStartOffset();
          Matcher br = Pattern.compile("<br/?>", Pattern.CASE_INSENSITIVE).matcher(input);
          while (findIn(br, new TextRange(pos, seeAlso.getEndOffset()))) {
            TextRange item = new TextRange(pos, br.start());
            result.append("<DD>").append(makeLink(item.substring(input))).append("</DD>");
            pos = br.end();
          }
          result.append("<DD>").append(makeLink(input.substring(pos, seeAlso.getEndOffset()))).append("</DD></DL>");
        }
        result.append(HTML_CLOSE);
      }
    };

    for (String docURL : findUrls(elementToShowDoc)) {
      try {
        String javadoc = docFilter.getExternalDocInfoForElement(docURL, elementToShowDoc);
        if (StringUtil.isNotEmpty(javadoc)) {
          return javadoc;
        }
      }
      catch (Exception e) {
        //try next url
      }
    }
    return doc;
  }

  @Override
  public @Nullable List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
    List<String> url = super.getUrlFor(element, originalElement);
    if (url != null) {
      return url;
    }

    final PsiElement elementToShowDoc = findElementToShowDoc(element);
    final List<String> urls = findUrls(elementToShowDoc);
    return urls != null && !urls.isEmpty() ? urls : null;
  }

  @Override
  public @Nullable PsiElement getDocumentationElementForLink(@NotNull PsiManager psiManager, @NotNull String link, @Nullable PsiElement context) {
    return getDocumentationElementForLinkStatic(psiManager, link, context);
  }

  @Override
  protected @NotNull JSDocumentationBuilder createDocumentationBuilder(@NotNull PsiElement element, PsiElement contextElement) {
    return new FlexDocumentationBuilder(element, null, this);
  }

  private static @NotNull PsiElement findElementToShowDoc(PsiElement _element) {
    PsiElement navigationElement = findNavigationElement(_element);
    if (navigationElement == null) {
      if (_element instanceof JSQualifiedNamedElement) {
        navigationElement = findTopLevelNavigationElement((JSQualifiedNamedElement)_element);
      }
      else {
        navigationElement = _element.getNavigationElement();
      }
    }
    return navigationElement;
  }

  private static String makeLink(String input) {
    input = input.trim();
    final Matcher link = ourLinkPattern.matcher(input);
    if (link.matches()) {
      String href = link.group(1);
      return "<a href=\"" + href + "\">" + link.group(2) + "</a>";
    }
    else {
      if (StringUtil.containsAnyChar(input, SEE_PLAIN_TEXT_CHARS)) {
        if (input.indexOf("\"") == input.length() - 1) {
          input = input.substring(0, input.length() - 1); // asdoc may generate trailing quote
        }
        return input;
      }
      else {
        input = StringUtil.escapeXmlEntities(input);
        return "<a href=\"" + input + "\">" + input + "</a>";
      }
    }
  }

  private static String prettyPrint(String text) {
    text = ourLabelSpan.matcher(text).replaceAll("<br><b>$1</b>");
    for (Pattern pattern : ourStrip) {
      text = pattern.matcher(text).replaceAll("");
    }
    text = replaceInPlainText(text, ourInsertBr, ourLinebreakInText);
    text = replaceInPlainText(text, ourInsertBr, ourLinebreakAfterCode);
    text = replaceInPlainText(text, ourInsertBr, ourLinebreakBeforeCode);
    return text;
  }

  private static String replaceInPlainText(String input, String replacement, Pattern p) {
    Matcher m = p.matcher(input);
    boolean result = m.find();
    int prevpos = 0;
    boolean isInTag = false;
    boolean isInPre = false;
    if (result) {
      StringBuilder sb = new StringBuilder();
      do {
        String s = input.substring(prevpos, m.start());
        isInTag = endIsBetween(s, "<", ">", isInTag);
        isInPre = endIsBetween(s, "<pre>", "</pre>", isInPre);

        prevpos = m.end();
        m.appendReplacement(sb, isInTag || isInPre ? "$0" : replacement);
        result = m.find();
      }
      while (result);
      m.appendTail(sb);
      return sb.toString();
    }
    return input;
  }

  private static boolean endIsBetween(String s, String open, String close, boolean defaultValue) {
    int openPos = s.lastIndexOf(open);
    int closePos = s.lastIndexOf(close);

    if (openPos == -1) {
      return closePos == -1 && defaultValue;
    }
    else {
      return closePos == -1 || closePos < openPos;
    }
  }

  private static String getAsDocRelativePath(JSQualifiedNamedElement element) {
    String qName = element.getQualifiedName();
    if (qName == null) qName = "";
    final String shortName = element instanceof JSClass ? StringUtil.getShortName(qName) : PACKAGE;
    String packageName = StringUtil.getPackageName(qName);
    packageName = packageName.replace('.', '/');
    return !packageName.isEmpty() ? packageName + "/" + shortName + HTML_EXTENSION : shortName + HTML_EXTENSION;
  }

  private static List<String> findUrlsForClass(JSQualifiedNamedElement aClass) {
    String qName = aClass.getQualifiedName();
    if (qName == null) {
      return Collections.emptyList();
    }

    PsiFile file = aClass.getContainingFile();
    if (!(file instanceof JSFile)) {
      return Collections.emptyList();
    }

    final PsiFile containingFile = aClass.getContainingFile();
    final VirtualFile virtualFile = containingFile.getVirtualFile();
    if (virtualFile == null) {
      return Collections.emptyList();
    }

    return findUrlForVirtualFile(containingFile.getProject(), virtualFile, getAsDocRelativePath(aClass));
  }

  private static List<String> findUrls(PsiElement element) {
    JSQualifiedNamedElement clazz = findParentQualifiedElement(element);
    if (clazz == null) {
      return Collections.emptyList();
    }

    String anchor = null;
    if (element instanceof JSFunction || element instanceof JSVariable) {
      anchor = ((JSNamedElement)element).getName();
      if (element instanceof JSFunction && !((JSFunction)element).isGetProperty() && !((JSFunction)element).isSetProperty()) {
        anchor += "()";
      }
    }
    else if (element instanceof JSAttributeNameValuePair) {
      String type = ((JSAttribute)element.getParent()).getName();
      final JSAttributeNameValuePair namePair = ((JSAttribute)element.getParent()).getValueByName("name");
      if (namePair != null) {
        anchor = DOCUMENTED_ATTRIBUTES.get(type) + namePair.getSimpleValue();
      }
    }
    else if (element instanceof JSAttribute) {
      final JSAttributeNameValuePair namePair = ((JSAttribute)element).getValueByName("name");
      if (namePair != null) {
        String type = ((JSAttribute)element).getName();
        anchor = DOCUMENTED_ATTRIBUTES.get(type) + namePair.getSimpleValue();
      }
    }

    List<String> urls = findUrlsForClass(clazz);
    if (anchor != null) {
      List<String> anchored = new ArrayList<>(urls.size());
      for (String url : urls) {
        anchored.add(url + "#" + anchor);
      }
      return anchored;
    }
    else {
      return urls;
    }
  }

  private static TextRange getRangeBetweenNested(String input, @Nullable TextRange within, Pattern openPattern, Pattern closePattern) {
    Matcher open = openPattern.matcher(input);
    Matcher close = closePattern.matcher(input);

    if (!open.find(within != null ? within.getStartOffset() : 0)) {
      return null;
    }

    final int contentsStart = open.end();

    int nesting = 0;
    int pos = contentsStart;
    while (true) {
      if ((within != null && !within.contains(pos)) || !close.find(pos)) {
        return null;
      }
      int closePos = close.start();

      if (open.find(pos) && open.start() < closePos) {
        nesting++;
        pos = open.end();
      }
      else {
        if (nesting > 0) {
          nesting--;
          pos = close.end();
        }
        else {
          return new TextRange(contentsStart, closePos);
        }
      }
    }
  }

  private static boolean findIn(Matcher m, TextRange within) {
    return m.find(within.getStartOffset()) && within.contains(m.start());
  }

  private static String getDisplayName(PsiElement element) {
    if (element instanceof JSClass) {
      return ((JSClass)element).getQualifiedName();
    }
    if (element instanceof JSFunction || element instanceof JSVariable) {
      final PsiElement parent = JSResolveUtil.findParent(element);
      String result;
      if (parent instanceof JSClass) {
        result = ((JSClass)parent).getQualifiedName() + "." + ((JSNamedElement)element).getName();
      }
      else {
        result = ((JSQualifiedNamedElement)element).getQualifiedName();
      }
      if (element instanceof JSFunction && !((JSFunction)element).isGetProperty() && !((JSFunction)element).isSetProperty()) {
        result += "()";
      }
      return result;
    }

    JSAttribute attribute = null;
    if (element instanceof JSAttributeNameValuePair) {
      attribute = (JSAttribute)element.getParent();
    }

    if (element instanceof JSAttribute) {
      attribute = (JSAttribute)element;
    }

    if (attribute != null && DOCUMENTED_ATTRIBUTES.containsKey(attribute.getName())) {
      final JSClass jsClass = PsiTreeUtil.getParentOfType(element, JSClass.class);
      JSAttributeNameValuePair namePair = attribute.getValueByName("name");
      if (jsClass != null && namePair != null) {
        return attribute.getName() + " " + jsClass.getQualifiedName() + "." + namePair.getSimpleValue();
      }
    }

    return null;
  }

  private static @Nullable PsiElement findNavigationElement(PsiElement element) {
    JSQualifiedNamedElement parentQualifiedElement = findParentQualifiedElement(element);
    if (parentQualifiedElement == null) {
      return null;
    }

    PsiElement navElement = findTopLevelNavigationElement(parentQualifiedElement);
    if (element instanceof JSClass) {
      return navElement;
    }
    if (element instanceof JSFunction && JSResolveUtil.findParent(element) instanceof JSClass && navElement instanceof JSClass) {
      return ((JSClass)navElement).findFunctionByNameAndKind(((JSFunction)element).getName(), ((JSFunction)element).getKind());
    }
    if (element instanceof JSVariable && JSResolveUtil.findParent(element) instanceof JSClass && navElement instanceof JSClass) {
      return ((JSClass)navElement).findFieldByName(((JSVariable)element).getName());
    }

    JSAttribute attribute = null;
    if (element instanceof JSAttributeNameValuePair) {
      attribute = (JSAttribute)element.getParent();
    }
    if (element instanceof JSAttribute) {
      attribute = (JSAttribute)element;
    }

    if (attribute != null && navElement instanceof JSClass) {
      final String type = attribute.getName();
      if (DOCUMENTED_ATTRIBUTES.containsKey(type)) {
        JSAttributeNameValuePair namePair = attribute.getValueByName("name");
        if (namePair != null) {
          return findNamedAttribute((JSClass)navElement, type, namePair.getSimpleValue());
        }
      }
    }

    if (element.getClass() == navElement.getClass()) {
      return navElement;
    }
    return null;
  }

  private static String[] correctHttpRoots(String [] roots) {
    String[] result = roots.clone();
    for (int i = 0; i < result.length; i++) {
      if (result[i].startsWith("http://")) {
        result[i] = StringUtil.trimEnd(StringUtil.trimEnd(result[i], "index.html"), "index.htm");
      }
    }
    return result;
  }

  private static String getLinkToResolve(JSQualifiedNamedElement origin, String link) {
    String originQname = origin.getQualifiedName();
    if (link.isEmpty()) {
      return originQname;
    }
    else if (StringUtil.startsWithChar(link, '#')) {
      if (origin instanceof JSClass) {
        return originQname + "." + link.substring(1);
      }
      else {
        String aPackage = StringUtil.getPackageName(originQname);
        return aPackage + "." + link.substring(1);
      }
    }
    else {
      String linkFile = link.contains("#") ? link.substring(0, link.lastIndexOf('#')) : link;
      String linkAnchor = link.contains("#") ? link.substring(link.lastIndexOf('#') + 1) : null;

      final String qname;
      if (StringUtil.endsWithIgnoreCase(linkFile, HTML_EXTENSION)) {
        String prefix = StringUtil.getPackageName(originQname);
        while (linkFile.startsWith("../")) {
          linkFile = linkFile.substring("../".length());
          prefix = StringUtil.getPackageName(prefix);
        }

        String linkFilename;
        if (linkFile.endsWith(PACKAGE_FILE)) {
          linkFilename = StringUtil.trimEnd(linkFile, PACKAGE_FILE);
          linkFilename = StringUtil.trimEnd(linkFilename, "/");
        }
        else {
          linkFilename = linkFile.substring(0, linkFile.lastIndexOf("."));
        }
        if (!linkFilename.isEmpty()) {
          qname = (!prefix.isEmpty() ? prefix + "." : prefix) + linkFilename.replaceAll("/", ".");
        }
        else {
          qname = prefix;
        }
      }
      else {
        qname = linkFile;
      }

      return linkAnchor != null ? (!qname.isEmpty() ? qname + "." : qname) + linkAnchor : qname;
    }
  }

  public static @NotNull PsiElement findTopLevelNavigationElement(JSQualifiedNamedElement element) {
    if (element.getName() == null) return element;

    final Ref<JSQualifiedNamedElement> withAsdoc = new Ref<>();
    final PsiElement sourceElement =
      JSPsiImplUtils.findTopLevelNavigatableElementWithSource(element, candidate -> {
        if (withAsdoc.isNull()) {
          ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(candidate.getProject()).getFileIndex();
          String relPath = getAsDocRelativePath(candidate);
          final PsiFile file = candidate.getContainingFile();
          if (file == null) {
            return;
          }

          VirtualFile containingFile = file.getVirtualFile();
          if (containingFile == null || projectFileIndex.getClassRootForFile(containingFile) == null) {
            return;
          }

          final List<OrderEntry> orderEntries = projectFileIndex.getOrderEntriesForFile(containingFile);
          for (OrderEntry orderEntry : orderEntries) {
            String[] roots = JavadocOrderRootType.getUrls(orderEntry);
            if (PlatformDocumentationUtil.getHttpRoots(correctHttpRoots(roots), relPath) != null) {
              withAsdoc.set(candidate);
            }
          }
        }
      });
    if (sourceElement != null) {
      return sourceElement;
    }
    if (!withAsdoc.isNull()) {
      return withAsdoc.get();
    }
    return element;
  }

  private static @NotNull List<String> findUrlForVirtualFile(final Project project, final VirtualFile virtualFile, final String relPath) {
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    Module module = fileIndex.getModuleForFile(virtualFile);
    if (module == null) {
      final VirtualFileSystem fs = virtualFile.getFileSystem();
      if (fs instanceof JarFileSystem) {
        final VirtualFile jar = ((JarFileSystem)fs).getVirtualFileForJar(virtualFile);
        if (jar != null) {
          module = fileIndex.getModuleForFile(jar);
        }
      }
    }
    if (module != null) {
      String[] javadocPaths = JavaModuleExternalPaths.getInstance(module).getJavadocUrls();
      List<String> httpRoots = PlatformDocumentationUtil.getHttpRoots(correctHttpRoots(javadocPaths), relPath);
      if (httpRoots != null) return httpRoots;
    }

    final List<OrderEntry> orderEntries = fileIndex.getOrderEntriesForFile(virtualFile);
    for (OrderEntry orderEntry : orderEntries) {
      final String[] files = JavadocOrderRootType.getUrls(orderEntry);
      final List<String> httpRoot = PlatformDocumentationUtil.getHttpRoots(correctHttpRoots(files), relPath);
      if (httpRoot != null) return httpRoot;
    }
    return Collections.emptyList();
  }

  private static @Nullable JSQualifiedNamedElement findParentQualifiedElement(PsiElement element) {
    if (element instanceof JSClass) {
      return (JSClass)element;
    }
    if (element instanceof XmlComment) {
      PsiElement parent = element.getParent();
      if (parent instanceof XmlProlog) {
        PsiElement e = parent.getNextSibling();
        if (e instanceof XmlTag) {
          return XmlBackedJSClassImpl.getContainingComponent((XmlElement)e);
        }
      }
    }
    if (element instanceof JSFunction || element instanceof JSVariable) {
      final PsiElement parent = JSResolveUtil.findParent(element);
      if (parent instanceof JSClass) {
        return (JSClass)parent;
      }
      else if (parent instanceof JSFile || parent instanceof JSPackage) {
        return (JSQualifiedNamedElement)element;
      }
    }

    JSAttribute attribute = null;
    if (element instanceof JSAttribute) {
      attribute = (JSAttribute)element;
    } else if (element instanceof JSAttributeNameValuePair) {
      attribute = (JSAttribute)element.getParent();
    }

    if (attribute != null && DOCUMENTED_ATTRIBUTES.containsKey(attribute.getName())) {
      final JSClass jsClass = PsiTreeUtil.getParentOfType(element, JSClass.class);
      if (jsClass != null) {
        return jsClass;
      }
    }

    return null;
  }

  private static @Nullable String getSeeAlsoLinkResolved(PsiElement originElement, String link) {
    JSQualifiedNamedElement qualifiedElement = findParentQualifiedElement(originElement);
    if (qualifiedElement == null) {
      return null;
    }
    String linkToResolve = getLinkToResolve(qualifiedElement, link);
    final PsiElement resolvedElement = getDocumentationElementForLinkStatic(originElement.getManager(), linkToResolve, originElement);
    if (resolvedElement != null) {
      return linkToResolve;
    }
    return null;
  }

  private static @Nullable PsiElement getDocumentationElementForLinkStatic(final PsiManager psiManager, String link, final PsiElement context) {
    final int delimiterIndex = link.lastIndexOf(':');
    final JavaScriptIndex index = JavaScriptIndex.getInstance(psiManager.getProject());

    String attributeType = null;
    String attributeName = null;
    for (Map.Entry<String, String> e : DOCUMENTED_ATTRIBUTES.entrySet()) {
      final String pattern = "." + e.getValue();
      if (link.contains(pattern)) {
        attributeType = e.getKey();
        attributeName = StringUtil.substringAfter(link, pattern);
        link = link.substring(0, link.indexOf(pattern));
        break;
      }
    }
    if (delimiterIndex != -1 && attributeType == null) {
      return resolveDocumentLink(psiManager, link, delimiterIndex);
    }
    else if (attributeType != null) {
      PsiElement clazz = ActionScriptClassResolver.findClassByQName(link, index, context != null ? ModuleUtilCore.findModuleForPsiElement(context) : null);
      if (!(clazz instanceof JSClass)) {
        return null;
      }
      return findNamedAttribute((JSClass)clazz, attributeType, attributeName);
    }
    else {
      PsiElement clazz = ActionScriptClassResolver.findClassByQName(link, index, context != null ? ModuleUtilCore.findModuleForPsiElement(context) : null);
      if (clazz == null && link.contains(".")) {
        String qname = link.substring(0, link.lastIndexOf('.'));
        clazz = ActionScriptClassResolver.findClassByQName(qname, index, context != null ? ModuleUtilCore.findModuleForPsiElement(context) : null);
        if (clazz instanceof JSClass jsClass) {
          String member = link.substring(link.lastIndexOf('.') + 1);

          if (member.endsWith("()")) {
            member = member.substring(0, member.length() - 2);

            PsiElement result = findMethod(jsClass, member);
            if (result == null) {
              result = findProperty(jsClass, member); // user might refer to a property
            }
            return result;
          }
          else {
            PsiElement result = jsClass.findFieldByName(member);
            if (result == null) {
              result = findProperty(jsClass, member);
            }
            if (result == null) {
              result = findMethod(jsClass, member); // user might forget brackets
            }
            return result;
          }
        }
      }

      if (clazz instanceof JSVariable) {
        return clazz;
      }

      if (link.endsWith("()")) {
        link = link.substring(0, link.length() - 2);
        clazz = ActionScriptClassResolver.findClassByQName(link, index, context != null ? ModuleUtilCore.findModuleForPsiElement(context) : null);
        if (clazz instanceof JSFunction) {
          return clazz;
        }
      }

      if (clazz == null && context != null) {
        final PsiReference[] references = new JSDocReferenceSet(context, link, 0, false).getReferences();
        if (references.length > 0) {
          final PsiElement resolve = references[references.length - 1].resolve();
          if (resolve != null) return resolve;
        }
      }
      return clazz;
    }
  }

  private static PsiElement findProperty(JSClass jsClass, String name) {
    PsiElement result = jsClass.findFunctionByNameAndKind(name, JSFunction.FunctionKind.GETTER);
    if (result == null) {
      result = jsClass.findFunctionByNameAndKind(name, JSFunction.FunctionKind.SETTER);
    }
    return result;
  }

  private static PsiElement findMethod(JSClass jsClass, String name) {
    PsiElement result = jsClass.findFunctionByNameAndKind(name, JSFunction.FunctionKind.CONSTRUCTOR);
    if (result == null) {
      result = jsClass.findFunctionByNameAndKind(name, JSFunction.FunctionKind.SIMPLE);
    }
    return result;
  }


  @Override
  protected boolean appendSeeAlsoLink(@NotNull String linkPart,
                                      @NotNull String displayText,
                                      @NotNull String seeAlsoValue,
                                      @NotNull PsiElement element,
                                      @NotNull StringBuilder result) {
    String seeAlsoLink = tryGetSeeAlsoLink(linkPart, element);
    if (seeAlsoLink != null) {
      DocumentationManager.createHyperlink(result, seeAlsoLink, displayText, true);
    }
    return seeAlsoLink != null;
  }

  public String tryGetSeeAlsoLink(@NotNull String linkPart, @NotNull PsiElement element) {
    if (!linkPart.contains(".") && !linkPart.startsWith("#")) {
      // first try to find class in the same package, then in default one
      JSQualifiedNamedElement qualifiedElement = findParentQualifiedElement(element);
      if (qualifiedElement != null) {
        String qname = qualifiedElement.getQualifiedName();
        String aPackage = qname.contains(".") ? qname.substring(0, qname.lastIndexOf('.') + 1) : "";
        String resolvedLink = getSeeAlsoLinkResolved(element, aPackage + linkPart);
        if (resolvedLink != null) {
          return resolvedLink;
        }
      }
    }
    return getSeeAlsoLinkResolved(element, linkPart);
  }

  private static @Nullable JSAttributeNameValuePair findNamedAttribute(JSClass clazz, final String type, final String name) {
    final Ref<JSAttributeNameValuePair> attribute = new Ref<>();
    ActionScriptResolveUtil.processMetaAttributesForClass(clazz, new ActionScriptResolveUtil.MetaDataProcessor() {
      @Override
      public boolean process(@NotNull JSAttribute jsAttribute) {
        if (type.equals(jsAttribute.getName())) {
          final JSAttributeNameValuePair jsAttributeNameValuePair = jsAttribute.getValueByName("name");
          if (jsAttributeNameValuePair != null && name.equals(jsAttributeNameValuePair.getSimpleValue())) {
            attribute.set(jsAttributeNameValuePair);
            return false;
          }
        }
        return true;
      }

      @Override
      public boolean handleOtherElement(PsiElement el, PsiElement context, @Nullable Ref<PsiElement> continuePassElement) {
        return true;
      }
    });
    return attribute.get();
  }

  @Override
  protected void appendFunctionInfoDoc(@NotNull JSFunction function, @NotNull StringBuilder builder) {
    JSType type = JSPsiImplUtils.getTypeFromDeclaration(function);

    if (type != null && !(type instanceof JSVoidType) && !function.isGetProperty()) {
      builder.append("* @return ");

      //builder.append(s);
      builder.append("\n");
    }
  }
}
