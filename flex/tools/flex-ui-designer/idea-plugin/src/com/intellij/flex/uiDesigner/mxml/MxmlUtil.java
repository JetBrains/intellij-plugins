package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.DocumentFactoryManager;
import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.xml.*;
import com.intellij.util.concurrency.Semaphore;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.regex.Pattern;

import static com.intellij.flex.uiDesigner.mxml.MxmlWriter.LOG;

public final class MxmlUtil {
  private static final Pattern FLEX_SDK_ABSTRACT_CLASSES = Pattern.compile("^(mx|spark)\\.(.*)?Base$");
  private static final Trinity<Integer, String, Condition<AnnotationBackedDescriptor>> NON_PROJECT_CLASS = new Trinity<>(-1, null, null);

  static final String UNKNOWN_COMPONENT_CLASS_NAME = "com.intellij.flex.uiDesigner.flex.UnknownComponent";
  static final String UNKNOWN_ITEM_RENDERER_CLASS_NAME = "com.intellij.flex.uiDesigner.flex.UnknownItemRenderer";

  public static Document getDocumentAndWaitIfNotCommitted(PsiFile psiFile) {
    Application application = ApplicationManager.getApplication();
    LOG.assertTrue(application.isUnitTestMode() || !application.isReadAccessAllowed());
    PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(psiFile.getProject());
    Document document = psiDocumentManager.getDocument(psiFile);
    LOG.assertTrue(document != null);
    if (!psiDocumentManager.isCommitted(document)) {
      final Semaphore semaphore = new Semaphore();
      semaphore.down();
      psiDocumentManager.performForCommittedDocument(document, () -> semaphore.up());
      semaphore.waitFor();
    }

    return document;
  }

  // about id http://opensource.adobe.com/wiki/display/flexsdk/id+property+in+MXML+2009
  public static boolean isIdLanguageAttribute(XmlAttribute attribute, AnnotationBackedDescriptor descriptor) {
    if (!descriptor.hasIdType()) {
      return false;
    }

    String ns = attribute.getNamespace();
    return ns.isEmpty() || ns.equals(JavaScriptSupportLoader.MXML_URI3);
  }

  static boolean isComponentLanguageTag(XmlTag tag) {
    return tag.getNamespace().equals(JavaScriptSupportLoader.MXML_URI3) && tag.getLocalName().equals("Component");
  }

  static boolean containsOnlyWhitespace(XmlTagChild child) {
    PsiElement firstChild = child.getFirstChild();
    return firstChild == child.getLastChild() && (firstChild == null || firstChild instanceof PsiWhiteSpace);
  }

  @Nullable
  public static PsiLanguageInjectionHost getInjectedHost(XmlTag tag) {
    // support <tag>{v}...</tag> or <tag>__PsiWhiteSpace__{v}...</tag>
    // <tag><span>text</span> {v}...</tag> is not supported
    for (XmlTagChild child : tag.getValue().getChildren()) {
      if (child instanceof XmlText) {
        //noinspection CastConflictsWithInstanceof
        return (PsiLanguageInjectionHost)child;
      }
      else if (!(child instanceof PsiWhiteSpace)) {
        return null;
      }
    }

    return null;
  }

  static boolean isAbstract(ClassBackedElementDescriptor classBackedDescriptor) {
    return FLEX_SDK_ABSTRACT_CLASSES.matcher(classBackedDescriptor.getQualifiedName()).matches();
  }

  static Trinity<Integer, String, Condition<AnnotationBackedDescriptor>> computeEffectiveClass(final PsiElement element,
                                                                                               final PsiElement declaration,
                                                                                               final ProjectComponentReferenceCounter projectComponentReferenceCounter,
                                                                                               final boolean computePropertyFilter)
    throws InvalidPropertyException {
    PsiFile psiFile = declaration.getContainingFile();
    VirtualFile virtualFile = psiFile.getVirtualFile();
    ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(psiFile.getProject()).getFileIndex();
    LOG.assertTrue(virtualFile != null);
    if (!projectFileIndex.isInSourceContent(virtualFile)) {
      return NON_PROJECT_CLASS;
    }

    if (psiFile instanceof XmlFile) {
      return new Trinity<>(
        DocumentFactoryManager.getInstance().getId(virtualFile, (XmlFile)psiFile, projectComponentReferenceCounter), null, null);
    }

    final Set<PsiFile> filteredFiles;
    if (computePropertyFilter) {
      filteredFiles = new THashSet<>();
      filteredFiles.add(psiFile);
    }
    else {
      filteredFiles = null;
    }

    final JSClass aClass = (JSClass)declaration;
    JSClass[] classes;
    while ((classes = aClass.getSuperClasses()).length > 0) {
      JSClass parentClass = classes[0];
      PsiFile containingFile = parentClass.getContainingFile();
      //noinspection ConstantConditions
      if (!projectFileIndex.isInSourceContent(containingFile.getVirtualFile())) {
        return new Trinity<>(-1, parentClass.getQualifiedName(),
                             computePropertyFilter
                             ? new CustomComponentPropertyFilter(filteredFiles)
                             : null);
      }
      else if (computePropertyFilter) {
        filteredFiles.add(containingFile);
      }
    }

    // well, it must be at least mx.core.UIComponent or spark.primitives.supportClasses.GraphicElement
    throw new InvalidPropertyException(element, "unresolved.class", aClass.getQualifiedName());
  }

  public static boolean isObjectLanguageTag(XmlTag tag) {
    return tag.getNamespace().equals(JavaScriptSupportLoader.MXML_URI3) &&
        tag.getLocalName().equals(JSCommonTypeNames.OBJECT_CLASS_NAME);
  }

  private static class CustomComponentPropertyFilter implements Condition<AnnotationBackedDescriptor> {
    private final Set<PsiFile> filteredFiles;

    public CustomComponentPropertyFilter(Set<PsiFile> filteredFiles) {
      this.filteredFiles = filteredFiles;
    }

    @Override
    public boolean value(AnnotationBackedDescriptor descriptor) {
      return !filteredFiles.contains(descriptor.getDeclaration().getContainingFile());
    }
  }

  static boolean isPropertyOfSparkDataGroup(AnnotationBackedDescriptor descriptor) {
    PsiElement parent = descriptor.getDeclaration().getParent();
    if (parent instanceof JSClass) {
      String name = ((JSClass)parent).getQualifiedName();
      return name.equals("spark.components.DataGroup") || name.equals("spark.components.SkinnableDataContainer");
    }

    return false;
  }
}
