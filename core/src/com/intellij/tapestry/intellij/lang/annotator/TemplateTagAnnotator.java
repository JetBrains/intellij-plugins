package com.intellij.tapestry.intellij.lang.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaType;
import com.intellij.tapestry.core.java.coercion.TypeCoercionValidator;
import com.intellij.tapestry.core.log.Logger;
import com.intellij.tapestry.core.log.LoggerFactory;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.core.model.presentation.valueresolvers.AbstractValueResolver;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ResolvedValue;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverChain;
import com.intellij.tapestry.core.util.ComponentUtils;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.core.resource.xml.IntellijXmlTag;
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

    if (ComponentUtils.isComponentTag(new IntellijXmlTag(tag))) {
      // don't valid these tags
      /*tag.putUserData(XmlHighlightVisitor.DO_NOT_VALIDATE_KEY, "true");
     tag.getParent().putUserData(XmlHighlightVisitor.DO_NOT_VALIDATE_KEY, "true");*/

      // annotate the tag start
      _annotationHolder.createInfoAnnotation(IdeaUtils.getNameElement(tag), null)
          .setTextAttributes(TextAttributesKey.find(TemplateColorSettingsPage.TAPESTRY_COMPONENT_TAG_KEY));

      // only annotation closing tag if the tag isn't empty like <t:body/>
      if (!tag.isEmpty()) {
        _annotationHolder.createInfoAnnotation(IdeaUtils.getNameElementClosing(tag), null)
            .setTextAttributes(TextAttributesKey.find(TemplateColorSettingsPage.TAPESTRY_COMPONENT_TAG_KEY));
      }

      Module module = IdeaUtils.getModule(tag);
      if (module == null) return;
      Component component = TapestryUtils.getComponentFromTag(module, tag);

      if (component != null) {
        TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);

        IntellijJavaClassType elementClass =
            (IntellijJavaClassType)ComponentUtils.findClassFromTemplate(new IntellijResource(tag.getContainingFile()), tapestryProject);

        // annotate the tag parameters
        if (elementClass != null) {
          for (TapestryParameter parameter : component.getParameters().values()) {
            XmlAttribute attribute = tag.getAttribute(parameter.getName(), TapestryConstants.TEMPLATE_NAMESPACE);
            if (attribute == null) attribute = tag.getAttribute(parameter.getName(), "");
            if (attribute == null) {
              if (parameter.isRequired() && !TapestryUtils.parameterDefinedInClass(parameter, elementClass, tag)) {
                _annotationHolder.createErrorAnnotation(IdeaUtils.getNameElement(tag), "Missing required parameter \"" + parameter.getName() + "\"");
              }
              continue;
            }

            // annotate attribute name
            _annotationHolder.createInfoAnnotation(attribute.getFirstChild(), null)
                .setTextAttributes(TextAttributesKey.find(TemplateColorSettingsPage.TAPESTRY_COMPONENT_PARAMETER_KEY));

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
            _annotationHolder.createErrorAnnotation(componentIdentifier.getNavigationElement(), "Unknown component type");
          }
        }
      }
    }

    tag.acceptChildren(this);
  }//visitXmlTag

}//TemplateTagAnnotator
