package com.intellij.javascript.flex.mxml.schema;

import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.daemon.Validator;
import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @author Maxim.Mossienko
*/
public class FlexMxmlNSDescriptor implements XmlNSDescriptor, Validator<XmlDocument> {
  private XmlFile myFile;
  private String namespace;
  private Module module;
  private GlobalSearchScope scope;

  static final Key<String> NS_KEY = Key.create("flex.ns.key");
  static final Key<Module> MODULE_KEY = Key.create("flex.module.key");
  static final Key<GlobalSearchScope> SCOPE_KEY = Key.create("flex.scope.key");
  private static boolean reportedAboutStackOverflow;

  // TODO seems that need to fix ECMAScript.js2 and E4X.js2
  // classes Boolean, Date, Number, String and XML are final in playerglobal.swc, but not final in our predefined files.
  // if changed to final there then we can remove these classes from this list.
  private static final String[] ILLEGAL_LANGUAGE_ROOT_TAGS =
    {"Array", "Boolean", "Component", "Class", "Date", "DesignLayer", "Function", "Number", "String", "XML", "int", "uint"};

  @Override
  @Nullable
  public XmlElementDescriptor getElementDescriptor(@NotNull final XmlTag tag) {
    if (MxmlJSClass.isInsideTagThatAllowsAnyXmlContent(tag)) {
      return new AnyXmlElementWithAnyChildrenDescriptor();
    }

    final String namespace = tag.getNamespace();
    final CodeContext context = getCodeContext(namespace);
    final String localName = tag.getLocalName();
    XmlElementDescriptor descriptor = context.getElementDescriptor(localName, tag);

    if (descriptor == null) {
      final XmlTag parentTag = tag.getParentTag();

      if (parentTag != null && namespace.equals(parentTag.getNamespace())) {
        final XmlElementDescriptor parentDescriptor = parentTag.getDescriptor();

        if (parentDescriptor != null &&
            // FIXME: prevent stackoverflow due to parent delegation due to different context namespace
            (!(parentDescriptor instanceof ClassBackedElementDescriptor) ||
             ClassBackedElementDescriptor.sameNs(namespace,(((ClassBackedElementDescriptor)parentDescriptor).context.namespace))
            )) {
          descriptor = parentDescriptor.getElementDescriptor(tag, parentTag);
        } else if (parentDescriptor != null && !reportedAboutStackOverflow) { // TODO: remove diagnostic
          Logger.getInstance(getClass().getName()).error(
            new AssertionError("avoided SOE:\n"+tag.getContainingFile().getText())
          );
          reportedAboutStackOverflow = true;
        }
      }
    }
    else if (tag.getParent() instanceof XmlDocument && !isLegalRootElementDescriptor(descriptor)) {
      return null;
    }

    if (descriptor == null && JavaScriptSupportLoader.MXML_URI3.equals(tag.getNamespace())) {
      return FxDefinitionBackedDescriptor.getFxDefinitionBackedDescriptor(module, tag);
    }

    return descriptor;
  }

  private CodeContext getCodeContext(String namespace) {
    return CodeContext.getContext(namespace, module, scope);
  }

  private static boolean isLegalRootElementDescriptor(final @NotNull XmlElementDescriptor _descriptor) {
    if (_descriptor instanceof ClassBackedElementDescriptor descriptor) {
      final PsiElement element = descriptor.getDeclaration();
      if (element instanceof JSClass) {
        final JSAttributeList attributeList = ((JSClass)element).getAttributeList();
        if (attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.FINAL)){
          return false;
        }
      }

      if (JavaScriptSupportLoader.isLanguageNamespace(descriptor.context.namespace)){
        final String tagName = _descriptor.getName();
        return !descriptor.isPredefined() && !ArrayUtil.contains(tagName, ILLEGAL_LANGUAGE_ROOT_TAGS);
      }
    }
    return true;
  }

  @Override
  public XmlElementDescriptor @NotNull [] getRootElementsDescriptors(@Nullable final XmlDocument document) {
    XmlElementDescriptor[] elementDescriptors = getCodeContext(namespace).getDescriptorsWithAllowedDeclaration();
    ArrayList<XmlElementDescriptor> results = new ArrayList<>(elementDescriptors.length);

    final XmlTag rootTag = document == null ? null : document.getRootTag();
    // sorry for this hacky way to determine if this is root tag name completion or not
    final boolean isRootTagCompletion = rootTag != null && rootTag.getName().endsWith(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED);

    for (XmlElementDescriptor elementDescriptor : elementDescriptors) {
      if (isRootTagCompletion) {
        if (!isLegalRootElementDescriptor(elementDescriptor)) {
          continue;
        }

        if (elementDescriptor instanceof MxmlBackedElementDescriptor) {
          final PsiElement declaration = elementDescriptor.getDeclaration();
          final PsiFile containingFile = document.getContainingFile();
          if (declaration != null && containingFile != null && declaration.equals(containingFile.getOriginalFile())) {
            // do not suggest root tag referencing to this mxml file itself
            continue;
          }
        }
      }

      String name = elementDescriptor.getName();
      if (name.length() > 0 /*&& Character.isUpperCase(name.charAt(0))*/) results.add(elementDescriptor);
    }
    return results.toArray(XmlElementDescriptor.EMPTY_ARRAY);
  }

  @Override
  @Nullable
  public XmlFile getDescriptorFile() {
    return myFile;
  }

  @Override
  public PsiElement getDeclaration() {
    return myFile;
  }

  @Override
  @NonNls
  public String getName(final PsiElement context) {
    return null;
  }

  @Override
  @NonNls
  public String getName() {
    return null;
  }

  @Override
  public void init(final PsiElement element) {
    XmlDocument document = (XmlDocument) element;
    myFile = ((XmlFile)document.getContainingFile());
    namespace = myFile.getUserData(NS_KEY);
    module = myFile.getUserData(MODULE_KEY);
    scope = myFile.getUserData(SCOPE_KEY);
    assert namespace != null;
    assert module != null;
    assert scope != null;

    CodeContextHolder.getInstance(module.getProject()).clearCodeContext(namespace, module, scope);
  }

  @Override
  public Object @NotNull [] getDependencies() {
    return getCodeContext(namespace).getDependencies();
  }

  @Override
  public void validate(@NotNull final XmlDocument context, @NotNull final ValidationHost host) {}

  public boolean hasElementDescriptorWithName(String name, String className) {
    CodeContext context = getCodeContext(namespace);
    XmlElementDescriptor descriptor = context.getElementDescriptor(name, (XmlTag)null);
    if (descriptor instanceof ClassBackedElementDescriptor) {
      return ((ClassBackedElementDescriptor)descriptor).className.equals(className);
    }
    return false;
  }

}
