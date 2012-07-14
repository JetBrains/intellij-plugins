package com.intellij.javascript.flex.css;

import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.stubs.JSQualifiedElementIndex;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.css.CssPropertyValue;
import com.intellij.psi.css.impl.CssTermTypes;
import com.intellij.psi.css.impl.util.completion.LengthUserLookup;
import com.intellij.psi.css.impl.util.table.AbstractCssPropertyDescriptor;
import com.intellij.psi.css.impl.util.table.CssLookupValue;
import com.intellij.psi.css.impl.util.table.CssPropertyValueImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.containers.HashMap;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Eugene.Kudelevsky
 */
public class FlexCssPropertyDescriptor extends AbstractCssPropertyDescriptor {
  private final String myInherit;
  private final boolean myShorthand;
  private final String myPropertyName;
  private final CssPropertyValue myValue;
  private final Set<String> myClassNames;
  private final Set<String> myFileNames;
  private final FlexStyleIndexInfo myStyleInfo;

  public static final String COLOR_FORMAT = "Color";
  private static final String LENGTH_FORMAT = "Length";

  public FlexCssPropertyDescriptor(Collection<FlexStyleIndexInfo> infos) {
    FlexStyleIndexInfo firstInfo = infos.iterator().next();
    myStyleInfo = firstInfo;
    myPropertyName = firstInfo.getAttributeName();
    myInherit = firstInfo.getInherit();
    myShorthand = constainsShorthand(infos);
    myValue = createPropertyValue(infos, myShorthand);
    myClassNames = new HashSet<String>();
    myFileNames = new HashSet<String>();
    for (FlexStyleIndexInfo info : infos) {
      if (info.isInClass()) {
        myClassNames.add(info.getClassOrFileName());
      }
      else {
        myFileNames.add(info.getClassOrFileName());
      }
    }
  }

  private static boolean constainsShorthand(Collection<FlexStyleIndexInfo> infos) {
    for (FlexStyleIndexInfo info : infos) {
      if (isShorthand(info)) return true;
    }
    return false;
  }

  private static boolean isShorthand(FlexStyleIndexInfo info) {
    return JSCommonTypeNames.ARRAY_CLASS_NAME.equals(info.getType());
  }

  private static void addValuesFromEnumerations(Collection<FlexStyleIndexInfo> infos, Collection<CssPropertyValue> children) {
    Set<String> constantSet = new HashSet<String>();
    for (FlexStyleIndexInfo info : infos) {
      String enumeration = info.getEnumeration();
      if (enumeration != null) {
        String[] constants = enumeration.split(",");
        Collections.addAll(constantSet, constants);
      }
    }
    if (constantSet.size() > 0) {
      //CssPropertyValueImpl value = new FlexCssPropertyValue(false, true);
      for (String constant : constantSet) {
        children.add(new FlexCssPropertyValue(constant.trim()));
      }
      //children.add(value);
    }
  }

  @NotNull
  public FlexStyleIndexInfo getStyleInfo() {
    return myStyleInfo;
  }

  private static Set<String> addValuesFromFormats(Collection<FlexStyleIndexInfo> infos, List<CssPropertyValue> children) {
    Set<String> formats = new HashSet<String>();
    for (FlexStyleIndexInfo info : infos) {
      String format = info.getFormat();
      if (format != null) {
        formats.add(format);
      }
    }
    if (formats.contains(COLOR_FORMAT)) {
      children.add(new FlexCssColorValue());
    }
    if (formats.contains(LENGTH_FORMAT)) {
      children.add(new CssLookupValue(new LengthUserLookup(), CssTermTypes.LENGTH, CssTermTypes.NUMBER, CssTermTypes.NEGATIVE_NUMBER,
                                      CssTermTypes.NEGATIVE_LENGTH));
    }
    return formats;
  }

  private static void addValuesFromTypes(Collection<FlexStyleIndexInfo> infos, Set<String> formats, List<CssPropertyValue> children) {
    Set<String> types = new HashSet<String>();
    for (FlexStyleIndexInfo info : infos) {
      String type = info.getType();
      if (type != null) {
        types.add(type);
      }
    }
    if (types.contains(JSCommonTypeNames.NUMBER_CLASS_NAME) && !formats.contains(LENGTH_FORMAT)) {
      children.add(new CssLookupValue(CssPropertyValueImpl.Type.OR, CssTermTypes.NUMBER, CssTermTypes.NEGATIVE_NUMBER));
    }
  }

  @NotNull
  private static CssPropertyValueImpl createPropertyValue(Collection<FlexStyleIndexInfo> infos, boolean shorthand) {
    List<CssPropertyValue> children = new ArrayList<CssPropertyValue>();
    Set<String> formats = addValuesFromFormats(infos, children);
    addValuesFromEnumerations(infos, children);
    addValuesFromTypes(infos, formats, children);
    CssPropertyValueImpl value = null;
    if (children.size() >= 1) {
      value = new FlexCssPropertyValue(shorthand, false);
      for (CssPropertyValue child : children) {
        value.addChild(child);
      }
    }
    if (value == null) {
      value = new FlexCssPropertyValue(shorthand, true);
    }
    else if (!formats.contains(COLOR_FORMAT)) {
      value.addChild(new FlexStringPropertyValue());
    }
    value.addChild(new FlexCssPropertyValue("undefined"));
    return value;
  }

  private static class DocumentationElement {
    String header;
    String documentation;

    private DocumentationElement(@NotNull String header, @NotNull String documentation) {
      this.header = header;
      this.documentation = documentation;
    }
  }

  public String generateDoc(PsiElement context) {
    if (context == null) return null;
    PsiElement[] declarations = getDeclarations(context);
    List<DocumentationElement> docElements = new ArrayList<DocumentationElement>();
    for (int i = 0, n = declarations.length; i < n; i++) {
      PsiElement declaration = declarations[i];
      PsiFile file = declaration.getContainingFile();
      if (file != null) {
        DocumentationProvider provider = DocumentationManager.getProviderFromElement(declaration);
        String docForDeclaration = provider.generateDoc(declaration, declaration);
        if (docForDeclaration != null) {
          JSClass jsClass = PsiTreeUtil.getParentOfType(declaration, JSClass.class);
          String header = jsClass != null ? jsClass.getQualifiedName() : file.getName();
          docElements.add(new DocumentationElement(header, docForDeclaration));
        }
      }
    }
    Collections.sort(docElements, new Comparator<DocumentationElement>() {
      public int compare(DocumentationElement e1, DocumentationElement e2) {
        return Comparing.compare(e1.header, e2.header);
      }
    });
    StringBuilder builder = new StringBuilder();
    for (int i = 0, n = docElements.size(); i < n; i++) {
      DocumentationElement docElement = docElements.get(i);
      builder.append("<b>").append(docElement.header).append("</b>").append("<br>\n");
      builder.append(docElement.documentation);
      if (i != n - 1) {
        builder.append("<br><br>\n\n");
      }
    }
    return builder.toString();
  }

  @NotNull
  @Override
  public CssPropertyValue getValue() {
    return myValue;
  }

  private boolean checkIncludes(JSClass c) {
    Set<String> includes = new HashSet<String>();
    FlexCssUtil.collectAllIncludes(c, includes);
    for (String name : myFileNames) {
      if (includes.contains(name)) {
        return true;
      }
    }
    return false;
  }

  private boolean findStyleAttributesInFile(@NotNull JSFile jsFile, Set<JSFile> visited, Map<PsiElement, PairInfo> navElement2pair) {
    if (!visited.add(jsFile)) return false;
    JSAttributeNameValuePair result = null;
    String fileName = jsFile.getName();
    if (myFileNames.contains(fileName)) {
      MyMetaDataProcessor processor = new MyMetaDataProcessor();
      FlexUtils.processMetaAttributesForClass(jsFile, processor);
      result = processor.myResult;
    }
    PsiElement navElement = result != null ? result.getNavigationElement() : null;
    if (result != null) {
      navElement2pair.put(navElement != null ? navElement : result, new PairInfo(result, fileName));
      return true;
    }
    return false;
  }

  private boolean findStyleAttributesInClassOrSuper(@NotNull JSClass c, Set<JSClass> visited, Map<PsiElement, PairInfo> navElement2pair) {
    if (!visited.add(c)) return false;
    JSAttributeNameValuePair result = null;
    String qName = c.getQualifiedName();
    if (myClassNames.contains(qName) || checkIncludes(c)) {
      MyMetaDataProcessor processor = new MyMetaDataProcessor();
      FlexUtils.processMetaAttributesForClass(c, processor);
      result = processor.myResult;
    }
    PsiElement navElement = result != null ? result.getNavigationElement() : null;
    if (result == null || navElement == null) {
      boolean found = false;
      for (JSClass superClass : c.getSupers()) {
        found = found || findStyleAttributesInClassOrSuper(superClass, visited, navElement2pair);
      }
      if (found) return true;
    }
    if (result != null) {
      navElement2pair.put(navElement != null ? navElement : result, new PairInfo(result, qName));
      return true;
    }
    return false;
  }

  private void findStyleAttributes(Collection<JSQualifiedNamedElement> elements,
                                   Set<JSClass> visited,
                                   Map<PsiElement, PairInfo> navElement2pair) {
    for (JSQualifiedNamedElement element : elements) {
      if (element instanceof JSClass) {
        findStyleAttributesInClassOrSuper((JSClass)element, visited, navElement2pair);
      }
    }
  }

  private static class PairInfo {
    final PsiElement myPair;
    final String myJsClassQName;

    private PairInfo(PsiElement pair, String jsClassQName) {
      myPair = pair;
      myJsClassQName = jsClassQName;
    }
  }

  @NotNull
  @Override
  public PsiElement[] getDeclarations(PsiElement context) {
    Map<PsiElement, PairInfo> navElement2pairInfo = new HashMap<PsiElement, PairInfo>();
    final Project project = context.getProject();

    GlobalSearchScope scope = FlexCssUtil.getResolveScope(context);

    StubIndex stubIndex = StubIndex.getInstance();

    Set<JSClass> visited = new HashSet<JSClass>();
    for (String className : myClassNames) {
      Collection<JSQualifiedNamedElement> candidates = stubIndex.get(JSQualifiedElementIndex.KEY, className.hashCode(), project, scope);
      findStyleAttributes(candidates, visited, navElement2pairInfo);
      // search in MXML files
      PsiElement jsClass = JSResolveUtil.findClassByQName(className, scope);
      if (jsClass instanceof JSClass) {
        findStyleAttributesInClassOrSuper((JSClass)jsClass, visited, navElement2pairInfo);
      }
    }

    Set<JSFile> visitedFiles = new HashSet<JSFile>();
    for (String fileName : myFileNames) {
      Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(project, fileName, scope);
      for (final VirtualFile file : files) {
        PsiFile psiFile = ApplicationManager.getApplication().runReadAction(new Computable<PsiFile>() {
          @Nullable
          @Override
          public PsiFile compute() {
            return PsiManager.getInstance(project).findFile(file);
          }
        });
        if (psiFile instanceof JSFile) {
          findStyleAttributesInFile((JSFile)psiFile, visitedFiles, navElement2pairInfo);
        }
      }
    }

    Set<PsiElement> navPairs = navElement2pairInfo.keySet();
    Map<String, PsiElement> qName2ResultElement = new HashMap<String, PsiElement>();
    for (PsiElement navPair : navPairs) {
      PairInfo pairInfo = navElement2pairInfo.get(navPair);
      String jsClassQName = pairInfo.myJsClassQName;
      PsiElement navPairInOtherClassWithSameQName = jsClassQName != null ? qName2ResultElement.get(jsClassQName) : null;
      if (navPairInOtherClassWithSameQName == null) {
        qName2ResultElement.put(jsClassQName, navPair);
      }
      else if (navPairInOtherClassWithSameQName == navElement2pairInfo.get(navPairInOtherClassWithSameQName) &&
               pairInfo.myPair != navPair) {
        qName2ResultElement.put(jsClassQName, navPair);
      }
    }
    Collection<PsiElement> result = qName2ResultElement.values();
    return PsiUtilBase.toPsiElementArray(result);
  }

  public boolean isShorthandValue() {
    return myShorthand;
  }

  @NotNull
  public String getPropertyName() {
    return myPropertyName;
  }

  public boolean getInherited() {
    return "yes".equalsIgnoreCase(myInherit) || "true".equals(myInherit);
  }

  private class MyMetaDataProcessor implements JSResolveUtil.MetaDataProcessor {
    private JSAttributeNameValuePair myResult;

    public boolean process(@NotNull JSAttribute jsAttribute) {
      if (FlexAnnotationNames.STYLE.equals(jsAttribute.getName())) {
        JSAttributeNameValuePair pair = jsAttribute.getValueByName("name");
        if (pair != null && myPropertyName.equals(pair.getSimpleValue())) {
          myResult = pair;
          return false;
        }
      }
      return true;
    }

    public boolean handleOtherElement(PsiElement el, PsiElement context, @Nullable Ref<PsiElement> continuePassElement) {
      return true;
    }
  }
}
