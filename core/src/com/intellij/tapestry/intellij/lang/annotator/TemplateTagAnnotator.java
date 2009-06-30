package com.intellij.tapestry.intellij.lang.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaType;
import com.intellij.tapestry.core.java.coercion.TypeCoercionValidator;
import com.intellij.tapestry.core.log.Logger;
import com.intellij.tapestry.core.log.LoggerFactory;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.model.presentation.valueresolvers.AbstractValueResolver;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ResolvedValue;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverChain;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.lang.TemplateColorSettingsPage;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.util.TapestryUtils;

/**
 * Annotates a Tapestry template.
 */
public class TemplateTagAnnotator extends XmlRecursiveElementVisitor implements Annotator {

  private static final Logger _logger = LoggerFactory.getInstance().getLogger(TemplateTagAnnotator.class);

  private AnnotationHolder _annotationHolder;

  public TemplateTagAnnotator() {
    super();

    _annotationHolder = null;
  }//Constructor

  public synchronized void annotate(PsiElement psiElement, AnnotationHolder annotationHolder) {
    _annotationHolder = annotationHolder;

    psiElement.accept(this);
  }//annotate


  /**
   * {@inheritDoc}
   */
  @Override
  public void visitXmlTag(XmlTag tag) {

    if (TapestryUtils.getComponentIdentifier(tag) != null) {
      // don't valid these tags
      /*tag.putUserData(XmlHighlightVisitor.DO_NOT_VALIDATE_KEY, "true");
     tag.getParent().putUserData(XmlHighlightVisitor.DO_NOT_VALIDATE_KEY, "true");*/

      // annotate the tag start
      annotateTapestryTag(tag);

      Module module = IdeaUtils.getModule(tag);
      if (module == null) return;
      Component component = TapestryUtils.getTypeOfTag(tag);

      if (component != null) {
        TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);

        final PresentationLibraryElement element = tapestryProject.findElementByTemplate(tag.getContainingFile());

        IntellijJavaClassType elementClass = element != null ? (IntellijJavaClassType)element.getElementClass() : null;
        // annotate the tag parameters
        if (elementClass != null) {
          XmlAttribute attr = TapestryUtils.getIdentifyingAttribute(tag);
          if (attr != null) {
            annotateTapestryAttribute(attr);
          }
          for (TapestryParameter parameter : component.getParameters().values()) {
            final String paramName = parameter.getName();
            XmlAttribute attribute = TapestryUtils.getTapestryAttribute(tag, paramName);
            if (attribute == null) {
              if (parameter.isRequired() && !TapestryUtils.parameterDefinedInClass(paramName, elementClass, tag)) {
                annotateTapestryTag(tag, "Missing required parameter \"" + paramName + "\"");
              }
              continue;
            }
            annotateTapestryAttribute(attribute);

            ResolvedValue resolvedValue;
            try {
              resolvedValue = ValueResolverChain.getInstance()
                  .resolve(tapestryProject, elementClass, attribute.getValue(), parameter.getDefaultPrefix());
            }
            catch (Exception ex) {
              _logger.error(ex);
              _annotationHolder.createErrorAnnotation(attribute.getValueElement(), "Invalid value");
              continue;
            }

            if (resolvedValue == null) {
              _annotationHolder.createErrorAnnotation(attribute.getValueElement(), "Invalid value");
              continue;
            }

            IJavaType parameterType = parameter.getParameterField().getType();
            if (!TypeCoercionValidator
                .canCoerce(tapestryProject, resolvedValue.getType(), AbstractValueResolver.getCleanValue(attribute.getValue()),
                           parameterType)) {
              _annotationHolder.createErrorAnnotation(attribute.getValueElement(), "Can't coerce a " +
                                                                                   resolvedValue.getType().getName() +
                                                                                   " to a " +
                                                                                   (parameterType != null
                                                                                    ? parameterType.getName()
                                                                                    : "undefined"));
            }
          }
        }
      }
      else {
        XmlElement componentIdentifier = TapestryUtils.getComponentIdentifier(tag);
        if (componentIdentifier != null) {
          if (componentIdentifier instanceof XmlAttribute) {
            String attrName = ((XmlAttribute)componentIdentifier).getLocalName();
            final String msg = attrName.equals("id") ? "Unknown child component id" : "Unknown component type";
            _annotationHolder.createErrorAnnotation(componentIdentifier.getLastChild(), msg);
          }
          else {
            annotateTapestryTag(tag, "Unknown component type");
          }
        }
      }
    }

    tag.acceptChildren(this);
  }//visitXmlTag

  private void annotateTapestryTag(XmlTag tag, String errorMsg) {
    _annotationHolder.createErrorAnnotation(IdeaUtils.getNameElement(tag), errorMsg);
    if (!tag.isEmpty()) {
      _annotationHolder.createErrorAnnotation(IdeaUtils.getNameElementClosing(tag), errorMsg);
    }
  }

  private void annotateTapestryTag(XmlTag tag) {
    _annotationHolder.createInfoAnnotation(IdeaUtils.getNameElement(tag), null).setTextAttributes(TemplateColorSettingsPage.TAG_NAME);
    if (!tag.isEmpty()) {
      _annotationHolder.createInfoAnnotation(IdeaUtils.getNameElementClosing(tag), null).setTextAttributes(TemplateColorSettingsPage.TAG_NAME);
    }
  }

  private void annotateTapestryAttribute(XmlAttribute attribute) {
    _annotationHolder.createInfoAnnotation(attribute.getFirstChild(), null).setTextAttributes(TemplateColorSettingsPage.ATTR_NAME);
  }

}//TemplateTagAnnotator
