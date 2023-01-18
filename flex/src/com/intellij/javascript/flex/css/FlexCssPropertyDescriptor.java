// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.css;

import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.stubs.JSQualifiedElementIndex;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.css.CssBundle;
import com.intellij.psi.css.CssPropertyValue;
import com.intellij.psi.css.descriptor.BrowserVersion;
import com.intellij.psi.css.descriptor.CssContextType;
import com.intellij.psi.css.descriptor.value.CssValueDescriptor;
import com.intellij.psi.css.impl.CssTermTypes;
import com.intellij.psi.css.impl.descriptor.CssCommonDescriptorData;
import com.intellij.psi.css.impl.descriptor.value.*;
import com.intellij.psi.css.impl.util.completion.LengthUserLookup;
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorFactory2;
import com.intellij.psi.css.impl.util.scheme.CssValueDescriptorModificator;
import com.intellij.psi.css.impl.util.table.AbstractCssPropertyDescriptor;
import com.intellij.psi.css.impl.util.table.CssLookupValue;
import com.intellij.psi.css.impl.util.table.CssPropertyValueImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FlexCssPropertyDescriptor extends AbstractCssPropertyDescriptor {
  @NotNull private final String myInherit;
  private final boolean myShorthand;
  @NotNull private final String myPropertyName;
  @NotNull private final CssPropertyValue myValue;
  @NotNull private final Set<String> myClassNames;
  @NotNull private final Set<String> myFileNames;
  private final FlexStyleIndexInfo myStyleInfo;

  public static final String COLOR_FORMAT = "Color";
  private static final String LENGTH_FORMAT = "Length";
  @NotNull private final CssValueDescriptor myValueDescriptor;

  public FlexCssPropertyDescriptor(@NotNull Collection<FlexStyleIndexInfo> infos) {
    FlexStyleIndexInfo firstInfo = infos.iterator().next();
    myStyleInfo = firstInfo;
    myPropertyName = firstInfo.getAttributeName();
    myInherit = firstInfo.getInherit();
    myShorthand = containsShorthand(infos);
    myValue = createPropertyValue(infos, myShorthand);
    myValueDescriptor = createPropertyValueDescriptor(infos, myShorthand);
    myClassNames = new LinkedHashSet<>();
    myFileNames = new LinkedHashSet<>();
    for (FlexStyleIndexInfo info : infos) {
      if (info.isInClass()) {
        myClassNames.add(info.getClassOrFileName());
      }
      else {
        myFileNames.add(info.getClassOrFileName());
      }
    }
  }

  private static boolean containsShorthand(@NotNull Collection<FlexStyleIndexInfo> infos) {
    for (FlexStyleIndexInfo info : infos) {
      if (isShorthand(info)) return true;
    }
    return false;
  }

  private static boolean isShorthand(@NotNull FlexStyleIndexInfo info) {
    return JSCommonTypeNames.ARRAY_CLASS_NAME.equals(info.getType());
  }

  private static void addValuesFromEnumerations2(@NotNull Collection<FlexStyleIndexInfo> infos, @NotNull Collection<CssValueDescriptor> children) {
    Set<String> constantSet = new LinkedHashSet<>();
    for (FlexStyleIndexInfo info : infos) {
      String enumeration = info.getEnumeration();
      if (enumeration != null) {
        String[] constants = enumeration.split(",");
        Collections.addAll(constantSet, constants);
      }
    }
    if (constantSet.size() > 0) {
      for (String constant : constantSet) {
        String constantName = constant.trim();
        children.add(CssElementDescriptorFactory2.getInstance().createNameValueDescriptor(constantName, constantName, 1, 1, null));
      }
    }
  }


  private static void addValuesFromEnumerations(@NotNull Collection<FlexStyleIndexInfo> infos, @NotNull Collection<CssPropertyValue> children) {
    Set<String> constantSet = new LinkedHashSet<>();
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

  @NotNull
  private static Set<String> addValuesFromFormats(@NotNull List<CssValueDescriptor> children, @NotNull Collection<FlexStyleIndexInfo> infos) {
    Set<String> formats = new LinkedHashSet<>();
    for (FlexStyleIndexInfo info : infos) {
      ContainerUtil.addIfNotNull(formats, info.getFormat());
    }

    if (formats.contains(COLOR_FORMAT)) {
      children.add(createCssColorValue());
    }
    if (formats.contains(LENGTH_FORMAT)) {
      children.add(createCssLengthValue());
    }
    return formats;
  }

  @NotNull
  private static Set<String> addValuesFromFormats(@NotNull Collection<FlexStyleIndexInfo> infos, @NotNull List<CssPropertyValue> children) {
    Set<String> formats = new LinkedHashSet<>();
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

  private static void addValuesFromTypes2(@NotNull Collection<FlexStyleIndexInfo> infos, @NotNull Set<String> formats, @NotNull List<CssValueDescriptor> children) {
    Set<String> types = new HashSet<>();
    for (FlexStyleIndexInfo info : infos) {
      ContainerUtil.addIfNotNull(types, info.getType());
    }
    if (types.contains(JSCommonTypeNames.NUMBER_CLASS_NAME) && !formats.contains(LENGTH_FORMAT)) {
      children.add(createCssNumberValue());
    }
    if (types.contains("Class")) {
      children.add(CssElementDescriptorFactory2.getInstance().createFunctionInvocationValueDescriptor("ClassReference", 1, 1, null));
    }
  }

  private static void addValuesFromTypes(@NotNull Collection<FlexStyleIndexInfo> infos, @NotNull Set<String> formats, @NotNull List<CssPropertyValue> children) {
    Set<String> types = new LinkedHashSet<>();
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
  private static CssValueDescriptor createPropertyValueDescriptor(@NotNull Collection<FlexStyleIndexInfo> infos, boolean shorthand) {
    List<CssValueDescriptor> children = new ArrayList<>();
    Set<String> formats = addValuesFromFormats(children, infos);
    addValuesFromEnumerations2(infos, children);
    addValuesFromTypes2(infos, formats, children);

    CssGroupValue result = CssElementDescriptorFactory2.getInstance().createGroupValue(CssGroupValue.Type.OR, -1, 1, null, null);
    if (!children.isEmpty()) {
      for (CssValueDescriptor child : children) {
        result.addChild(CssValueDescriptorModificator.withParent(child, result));
      }
      if (!formats.contains(COLOR_FORMAT)) {
        result.addChild(CssElementDescriptorFactory2.getInstance().createStringValueDescriptor(null, 1, 1, result));
      }
    }
    else {
      result.addChild(CssElementDescriptorFactory2.getInstance().createAnyValueDescriptor(1, 1, result));
    }

    result.addChild(CssElementDescriptorFactory2.getInstance().createNameValueDescriptor("undefined", "undefined", 1, 1, result));
    result.addChild(CssElementDescriptorFactory2.getInstance().createFunctionInvocationValueDescriptor("PropertyReference", 1, 1, result));
    result.addChild(CssElementDescriptorFactory2.getInstance().createFunctionInvocationValueDescriptor("Embed", 1, 1, result));
    return result;
  }

  private static CssColorValue createCssColorValue() {
    String id = CssBundle.message("color.value.presentable.name");
    CssCommonDescriptorData commonDescriptorData = new CssCommonDescriptorData(id, id, CssContextType.EMPTY_ARRAY, BrowserVersion.EMPTY_ARRAY, CssVersion.UNKNOWN, null, "");
    CssValueDescriptorData valueDescriptorData = new CssValueDescriptorData(true, 1, 1, null, null, null, null, false);
    return new CssColorValue(commonDescriptorData, valueDescriptorData, false);
  }

  private static CssLengthValue createCssLengthValue() {
    String id = CssBundle.message("length.value.presentable.name");
    CssCommonDescriptorData commonDescriptorData = new CssCommonDescriptorData(id, id, CssContextType.EMPTY_ARRAY, BrowserVersion.EMPTY_ARRAY, CssVersion.UNKNOWN, null, "");
    CssValueDescriptorData valueDescriptorData = new CssValueDescriptorData(true, 1, 1, null, null, null, null, false);
    return new CssLengthValue(commonDescriptorData, valueDescriptorData);
  }

  private static CssNumberValue createCssNumberValue() {
    String id = CssBundle.message("number.value.presentable.name");
    CssCommonDescriptorData commonDescriptorData = new CssCommonDescriptorData(id, id, CssContextType.EMPTY_ARRAY, BrowserVersion.EMPTY_ARRAY, CssVersion.UNKNOWN, null, "");
    CssValueDescriptorData valueDescriptorData = new CssValueDescriptorData(true, 1, 1, null, null, null, null, false);
    return new CssNumberValue(commonDescriptorData, valueDescriptorData);
  }

  /**
   * @deprecated use this#createPropertyValueDescriptor
   */
  @Deprecated
  @NotNull
  private static CssPropertyValueImpl createPropertyValue(@NotNull Collection<FlexStyleIndexInfo> infos, boolean shorthand) {
    List<CssPropertyValue> children = new ArrayList<>();
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

  private static final class DocumentationElement {
    String header;
    String documentation;

    private DocumentationElement(@NotNull String header, @NotNull String documentation) {
      this.header = header;
      this.documentation = documentation;
    }
  }

  @Override
  @Nullable
  public String getDocumentationString(@Nullable PsiElement context) {
    if (context == null) return null;
    PsiElement[] declarations = getDeclarations(context);
    List<DocumentationElement> docElements = new ArrayList<>();
    for (PsiElement declaration : declarations) {
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
    docElements.sort((e1, e2) -> Comparing.compare(e1.header, e2.header));
    StringBuilder builder = new StringBuilder();
    for (int i = 0, n = docElements.size(); i < n; i++) {
      DocumentationElement docElement = docElements.get(i);
      String documentation = docElement.documentation;
      if (i > 0) {
        int definitionEnd = documentation.indexOf(DocumentationMarkup.DEFINITION_END);
        if (definitionEnd > 0) {
          documentation = documentation.substring(definitionEnd + DocumentationMarkup.DEFINITION_END.length());
        }
      }
      int sectionsStart = documentation.indexOf(DocumentationMarkup.SECTIONS_START);
      if (sectionsStart < 0) {
        builder.append(documentation);
        builder.append(DocumentationMarkup.SECTIONS_START);
        addDeclaredIn(builder, docElement);
        builder.append(DocumentationMarkup.SECTIONS_END);
      } else {
        sectionsStart += DocumentationMarkup.SECTIONS_START.length();
        builder.append(documentation, 0, sectionsStart);
        addDeclaredIn(builder, docElement);
        builder.append(documentation.substring(sectionsStart));
      }
    }
    return builder.toString();
  }

  private static void addDeclaredIn(StringBuilder builder, DocumentationElement docElement) {
    builder.append(DocumentationMarkup.SECTION_HEADER_START);
    builder.append("Declared in:");
    builder.append(DocumentationMarkup.SECTION_SEPARATOR);
    builder.append("<p>");
    builder.append(docElement.header);
    builder.append(DocumentationMarkup.SECTION_END);
  }

  @NotNull
  @Override
  public CssPropertyValue getValue() {
    return myValue;
  }

  private boolean checkIncludes(JSClass c) {
    Set<String> includes = new LinkedHashSet<>();
    FlexCssUtil.collectAllIncludes(c, includes);
    for (String name : myFileNames) {
      if (includes.contains(name)) {
        return true;
      }
    }
    return false;
  }

  private boolean findStyleAttributesInFile(@NotNull JSFile jsFile, @NotNull Set<JSFile> visited, @NotNull Map<PsiElement, PairInfo> navElement2pair) {
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

  private boolean findStyleAttributesInClassOrSuper(@NotNull JSClass c, @NotNull Set<JSClass> visited, @NotNull Map<PsiElement, PairInfo> navElement2pair) {
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

  private void findStyleAttributes(@NotNull Collection<JSQualifiedNamedElement> elements,
                                   @NotNull Set<JSClass> visited,
                                   @NotNull Map<PsiElement, PairInfo> navElement2pair) {
    for (JSQualifiedNamedElement element : elements) {
      if (element instanceof JSClass) {
        findStyleAttributesInClassOrSuper((JSClass)element, visited, navElement2pair);
      }
    }
  }

  private static final class PairInfo {
    final PsiElement myPair;
    final String myJsClassQName;

    private PairInfo(PsiElement pair, String jsClassQName) {
      myPair = pair;
      myJsClassQName = jsClassQName;
    }
  }

  @Override
  public PsiElement @NotNull [] getDeclarations(@NotNull PsiElement context) {
    Map<PsiElement, PairInfo> navElement2pairInfo = new HashMap<>();
    final Project project = context.getProject();

    GlobalSearchScope scope = FlexCssUtil.getResolveScope(context);
    Set<JSClass> visited = new LinkedHashSet<>();
    for (String className : myClassNames) {
      Collection<JSQualifiedNamedElement> candidates = StubIndex.getElements(JSQualifiedElementIndex.KEY, className, project,
                                                                             scope, JSQualifiedNamedElement.class);
      findStyleAttributes(candidates, visited, navElement2pairInfo);
      // search in MXML files
      PsiElement jsClass = ActionScriptClassResolver.findClassByQNameStatic(className, scope);
      if (jsClass instanceof JSClass) {
        findStyleAttributesInClassOrSuper((JSClass)jsClass, visited, navElement2pairInfo);
      }
    }

    Set<JSFile> visitedFiles = new LinkedHashSet<>();
    for (String fileName : myFileNames) {
      Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(fileName, scope);
      for (final VirtualFile file : files) {
        PsiFile psiFile = ReadAction.compute(() -> PsiManager.getInstance(project).findFile(file));
        if (psiFile instanceof JSFile) {
          findStyleAttributesInFile((JSFile)psiFile, visitedFiles, navElement2pairInfo);
        }
      }
    }

    Map<String, PsiElement> qName2ResultElement = new HashMap<>();
    for (Map.Entry<PsiElement, PairInfo> entry : navElement2pairInfo.entrySet()) {
      PsiElement navElement = entry.getKey();
      PairInfo pairInfo = entry.getValue();
      String jsClassQName = pairInfo.myJsClassQName;
      PsiElement navPairInOtherClassWithSameQName = jsClassQName != null ? qName2ResultElement.get(jsClassQName) : null;
      if (navPairInOtherClassWithSameQName == null ||
          navPairInOtherClassWithSameQName == navElement2pairInfo.get(navPairInOtherClassWithSameQName).myPair &&
          pairInfo.myPair != navElement) {
        qName2ResultElement.put(jsClassQName, navElement);
      }
    }
    Collection<PsiElement> result = qName2ResultElement.values();
    return PsiUtilCore.toPsiElementArray(result);
  }

  @Override
  public boolean isShorthandValue() {
    return myShorthand;
  }

  @Override
  @NotNull
  public String getPropertyName() {
    return myPropertyName;
  }

  @Override
  public boolean getInherited() {
    return "yes".equalsIgnoreCase(myInherit) || "true".equals(myInherit);
  }

  private class MyMetaDataProcessor implements ActionScriptResolveUtil.MetaDataProcessor {
    private JSAttributeNameValuePair myResult;

    @Override
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

    @Override
    public boolean handleOtherElement(PsiElement el, PsiElement context, @Nullable Ref<PsiElement> continuePassElement) {
      return true;
    }
  }

  @NotNull
  @Override
  public CssValueDescriptor getValueDescriptor() {
    return myValueDescriptor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FlexCssPropertyDescriptor)) return false;

    FlexCssPropertyDescriptor that = (FlexCssPropertyDescriptor)o;

    if (myShorthand != that.myShorthand) return false;
    if (!myClassNames.equals(that.myClassNames)) return false;
    if (!myFileNames.equals(that.myFileNames)) return false;
    if (!myInherit.equals(that.myInherit)) return false;
    if (!myPropertyName.equals(that.myPropertyName)) return false;
    if (!myStyleInfo.equals(that.myStyleInfo)) return false;
    if (!myValue.equals(that.myValue)) return false;
    if (!myValueDescriptor.equals(that.myValueDescriptor)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myInherit.hashCode();
    result = 31 * result + (myShorthand ? 1 : 0);
    result = 31 * result + (myPropertyName.hashCode());
    result = 31 * result + (myValue.hashCode());
    result = 31 * result + (myValueDescriptor.hashCode());
    result = 31 * result + (myClassNames.hashCode());
    result = 31 * result + (myFileNames.hashCode());
    result = 31 * result + (myStyleInfo.hashCode());
    return result;
  }
}
