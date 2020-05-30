package com.intellij.tapestry.intellij.lang.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaType;
import com.intellij.tapestry.core.java.coercion.TypeCoercionValidator;
import com.intellij.tapestry.core.model.presentation.TapestryComponent;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.core.model.presentation.valueresolvers.AbstractValueResolver;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ResolvedValue;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverChain;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.lang.TemplateColorSettingsPage;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Annotates a Tapestry template.
 */
public class TemplateTagAnnotator extends XmlRecursiveElementVisitor implements Annotator {

  private static final Logger _logger = Logger.getInstance(TemplateTagAnnotator.class);

  private AnnotationHolder annotationHolder;

  public TemplateTagAnnotator() {
    super();

    annotationHolder = null;
  }//Constructor

  @Override
  public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
    this.annotationHolder = annotationHolder;

    try {
      psiElement.accept(this);
    }
    finally {
      this.annotationHolder = null;
    }
  }//annotate

  @Override
  public void visitXmlTag(XmlTag tag) {

    if (TapestryUtils.getComponentIdentifier(tag) != null) {

      // annotate the tag
      annotateTapestryTag(tag);
      XmlAttribute attr = TapestryUtils.getIdentifyingAttribute(tag);
      if (attr != null) {
        annotateTapestryAttribute(attr);
      }

      TapestryComponent component = TapestryUtils.getTypeOfTag(tag);

      if (component != null) {
        TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(tag);
        final PresentationLibraryElement element =
          tapestryProject == null ? null : tapestryProject.findElementByTemplate(tag.getContainingFile());

        IntellijJavaClassType elementClass = element != null ? (IntellijJavaClassType)element.getElementClass() : null;
        // annotate the tag parameters
        for (TapestryParameter parameter : component.getParameters().values()) {
          final String paramName = parameter.getName();
          XmlAttribute attribute = TapestryUtils.getTapestryAttribute(tag, paramName);
          if (attribute == null) continue;
          annotateTapestryAttribute(attribute);
          if (elementClass != null) {
            annotateAttributeValue(tapestryProject, elementClass, parameter, attribute);
          }
        }
      }
    }

    tag.acceptChildren(this);
  }//visitXmlTag

  private void annotateAttributeValue(TapestryProject tapestryProject,
                                      IntellijJavaClassType elementClass,
                                      TapestryParameter parameter,
                                      XmlAttribute attribute) {
    ResolvedValue resolvedValue;
    final XmlAttributeValue value = attribute.getValueElement();
    if (value == null) return;
    try {
      resolvedValue =
        ValueResolverChain.getInstance().resolve(tapestryProject, elementClass, attribute.getValue(), parameter.getDefaultPrefix());
    } catch (ProcessCanceledException pce) {
      throw pce;
    }
    catch (Exception ex) {
      _logger.error(ex);
      //annotationHolder.createErrorAnnotation(value, "Invalid value");
      return;
    }

    if (resolvedValue == null) {
      //annotationHolder.createErrorAnnotation(value, "Invalid value");
      return;
    }

    IJavaType parameterType = parameter.getParameterField().getType();
    final AnnotationHolder holder = annotationHolder;
    if (holder != null &&
        !TypeCoercionValidator
          .canCoerce(tapestryProject, resolvedValue.getType(), AbstractValueResolver.getCleanValue(attribute.getValue()), parameterType)) {
      holder.newAnnotation(HighlightSeverity.ERROR, "Can't coerce a " +
                                                    resolvedValue.getType().getName() +
                                                    " to a " +
                                                    (parameterType != null ? parameterType.getName() : "undefined")).range(value).create();
    }
  }

  private void annotateTapestryTag(XmlTag tag) {
    final AnnotationHolder holder = annotationHolder;
    if (holder == null) return;
    holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(IdeaUtils.getNameElement(tag)).textAttributes(TemplateColorSettingsPage.TAG_NAME).create();
    if (!tag.isEmpty()) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(IdeaUtils.getNameElementClosing(tag)).textAttributes(TemplateColorSettingsPage.TAG_NAME).create();
    }
  }

  private void annotateTapestryAttribute(XmlAttribute attribute) {
    final AnnotationHolder holder = annotationHolder;
    if (holder != null) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(attribute.getFirstChild()).textAttributes(TemplateColorSettingsPage.ATTR_NAME).create();
    }
  }

}//TemplateTagAnnotator
