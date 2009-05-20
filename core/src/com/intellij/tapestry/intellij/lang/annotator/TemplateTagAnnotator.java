package com.intellij.tapestry.intellij.lang.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotFoundException;
import com.intellij.tapestry.core.java.IJavaAnnotation;
import com.intellij.tapestry.core.java.IJavaField;
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
import com.intellij.tapestry.intellij.util.TapestryUtils;

import java.util.Map;

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
      _annotationHolder.createInfoAnnotation(tag.getChildren()[1], null)
          .setTextAttributes(TextAttributesKey.find(TemplateColorSettingsPage.TAPESTRY_COMPONENT_TAG_KEY));

      // only annotation closing tag if the tag isn't empty like <t:body/>
      if (!tag.isEmpty()) {
        _annotationHolder.createInfoAnnotation(tag.getChildren()[tag.getChildren().length - 2], null)
            .setTextAttributes(TextAttributesKey.find(TemplateColorSettingsPage.TAPESTRY_COMPONENT_TAG_KEY));
      }

      Module module = ProjectRootManager.getInstance(tag.getManager().getProject()).getFileIndex()
          .getModuleForFile(tag.getContainingFile().getVirtualFile());
      if (module == null) return;
      Component component;
      try {
        component = TapestryUtils.getComponentFromTag(module, tag);
      }
      catch (NotFoundException ex) {
        component = null;
      }

      if (component != null) {
        TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);

        IntellijJavaClassType elementClass;
        try {
          elementClass =
              (IntellijJavaClassType)ComponentUtils.findClassFromTemplate(new IntellijResource(tag.getContainingFile()), tapestryProject);
        }
        catch (NotFoundException ex) {
          elementClass = null;
        }

        // annotate the tag parameters
        if (elementClass != null) {
          String tapestryNamespacePrefix = TapestryUtils.getTapestryNamespacePrefix((XmlFile)tag.getContainingFile());
          for (TapestryParameter parameter : component.getParameters().values()) {
            final String qname = tapestryNamespacePrefix + ":" + parameter.getName();
            XmlAttribute attribute = tag.getAttribute(qname);

            if (attribute == null) attribute = tag.getAttribute(parameter.getName(), "");

            if (attribute == null) {
              if (parameter.isRequired() && !parameterDefineInClass(parameter, elementClass, tag)) {
                _annotationHolder.createErrorAnnotation(tag.getChildren()[1], "Missing required parameter \"" + parameter.getName() + "\"");
              }

              continue;
            }

            // annotate attribute name
            _annotationHolder.createInfoAnnotation(attribute.getChildren()[0], null)
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
        XmlElement identifierAttribute = TapestryUtils.getComponentIdentifier(tag);
        if (identifierAttribute != null) {
          if (identifierAttribute instanceof XmlAttribute) {
            _annotationHolder.createErrorAnnotation(identifierAttribute.getChildren()[0], "Invalid component name");
          }
          else {
            _annotationHolder.createErrorAnnotation(identifierAttribute.getNavigationElement(), "Invalid component name");
          }
        }
      }
    }

    tag.acceptChildren(this);
  }//visitXmlTag

  /**
   * Verify the existence of parameter declaration in elementClass
   *
   * @param parameter    the parameter to check
   * @param elementClass the class to get the fields
   * @param tag          the component to get the parameters
   * @return <code>true</code> if the parameter is define in the class, <code>false</code> otherwise.
   */
  public boolean parameterDefineInClass(TapestryParameter parameter, IntellijJavaClassType elementClass, XmlTag tag) {

    Map<String, IJavaField> fields = elementClass.getFields(false);
    boolean parameterDefineInClass = false;

    for (IJavaField field : fields.values()) {
      if (field.getAnnotations().get(TapestryConstants.COMPONENT_ANNOTATION) != null) {
        if (field.getName().equals(tag.getAttributeValue("t:id"))) {

          IJavaAnnotation fieldAnnotation = field.getAnnotations().get(TapestryConstants.COMPONENT_ANNOTATION);
          String[] fieldParameters = fieldAnnotation.getParameters().get("parameters");

          if (fieldParameters != null) {
            for (String fieldParameter : fieldParameters) {
              if (fieldParameter.split("=").length == 2) {
                String parameterName = fieldParameter.split("=")[0];
                if (parameterName.equals(parameter.getName())) parameterDefineInClass = true;
              }
            }
          }
        }
      }
    }
    return parameterDefineInClass;
  }//parameterDefineInClass

}//TemplateTagAnnotator
