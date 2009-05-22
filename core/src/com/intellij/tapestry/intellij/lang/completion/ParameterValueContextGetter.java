package com.intellij.tapestry.intellij.lang.completion;

import com.intellij.codeInsight.completion.CompletionContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.filters.ContextGetter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotFoundException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.log.Logger;
import com.intellij.tapestry.core.log.LoggerFactory;
import com.intellij.tapestry.core.model.Library;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ResolvedValue;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverChain;
import com.intellij.tapestry.core.util.ClassUtils;
import com.intellij.tapestry.core.util.ComponentUtils;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.util.TapestryUtils;

import java.util.*;

/**
 * Provides all parameters context values
 */
public class ParameterValueContextGetter implements ContextGetter {

    private static final Logger _logger = LoggerFactory.getInstance().getLogger(ParameterValueContextGetter.class);

    /**
     * {@inheritDoc}
     */
    public Object[] get(PsiElement psiElement, CompletionContext completionContext) {
        Module module = ProjectRootManager.getInstance(completionContext.project).getFileIndex().getModuleForFile(psiElement.getContainingFile().getOriginalFile().getVirtualFile());

        // if this isn't a Tapestry module don't do anything
        if (!TapestryUtils.isTapestryModule(module))
            return new Object[0];

        if (psiElement instanceof XmlToken && ((XmlToken) psiElement).getTokenType().toString().equals("XML_ATTRIBUTE_VALUE_TOKEN")) {
            // The selected attribute
            XmlAttribute attribute = PsiTreeUtil.getParentOfType(psiElement, XmlAttribute.class);

            // The selected tag
            XmlTag tag = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class);

            if (attribute == null) {
                return new Object[0];
            }

            // Completion of type and id attributes is handled by ComponentNameContextGetter
            if (attribute.getNamespace().equals(TapestryConstants.TEMPLATE_NAMESPACE) && (attribute.getLocalName().equals("type") || attribute.getLocalName().equals("id"))) {
                return new Object[0];
            }

            // Try to match the tag to a component
          Component component = TapestryUtils.getComponentFromTag(module, tag);
          if(component == null) return new Object[0];

            TapestryProject tapestryProject = TapestryModuleSupportLoader
                    .getTapestryProject(ProjectRootManager.getInstance(completionContext.project).getFileIndex().getModuleForFile(psiElement.getContainingFile().getOriginalFile().getVirtualFile()));

            // Completion of parameter page parameter in pagelink
            if (component.getName().toLowerCase(Locale.getDefault()).equals("pagelink") && attribute.getLocalName().equals("page")) {
                return completePageLinkPageParameter(tapestryProject);
            }

          IntellijJavaClassType elementClass = (IntellijJavaClassType) ComponentUtils.findClassFromTemplate(new IntellijResource(completionContext.file), tapestryProject);
          if(elementClass == null) return new Object[0];

            for (TapestryParameter parameter : component.getParameters().values()) {
                String attributeValue = "";
                if (attribute.getLocalName() != null && tag != null) {
                    attributeValue = tag.getAttributeValue(attribute.getLocalName());
                }

                if (!parameter.getName().toLowerCase(Locale.getDefault()).equals(attribute.getLocalName().toLowerCase(Locale.getDefault()))) {
                    continue;
                }

                if (attributeValue != null) {
                    // Completion of all attribute values that starts with "prop:"
                    if (attributeValue.equals("prop:IntellijIdeaRulezzz ")) {
                        Set<String> properties = ClassUtils.getClassProperties(elementClass).keySet();
                        ArrayList<String> returnedProperties = new ArrayList<String>();

                        for (String property : properties)
                            returnedProperties.add("prop:" + property);

                        return returnedProperties.toArray();
                    }

                    // Completion of composed properties
                    if (attributeValue.contains(".")
                            && !attributeValue.contains("..")
                            && (parameter.getDefaultPrefix().equals("prop") || attributeValue.startsWith("prop:"))) {

                        Set<String> properties;

                        if (attributeValue.contains(".IntellijIdeaRulezzz ")) {
                            Scanner scan = new Scanner(attributeValue);
                            String word = "", words = "";
                            while (scan.hasNext() && !word.contains("IntellijIdeaRulezzz")) {
                                word = scan.next();
                                words = words + word;
                            }
                            attributeValue = words.replaceFirst("IntellijIdeaRulezzz", "");
                        }

                        ResolvedValue resolvedValue;
                        try {
                            resolvedValue = ValueResolverChain.getInstance().resolve(tapestryProject, elementClass, attributeValue, parameter.getDefaultPrefix());
                        } catch (Exception ex) {
                            _logger.error(ex);

                            return new Object[0];
                        }

                        if (resolvedValue != null && resolvedValue.getType() != null && resolvedValue.getType() instanceof IJavaClassType && module != null) {
                            elementClass = new IntellijJavaClassType(module, ((IntellijJavaClassType) resolvedValue.getType()).getPsiClass().getContainingFile());
                            properties = ClassUtils.getClassProperties(elementClass).keySet();

                            ArrayList<String> returnedProperties = new ArrayList<String>();
                            for (String property : properties) {
                                returnedProperties.add(attributeValue + "" + property);
                            }

                            return returnedProperties.toArray();
                        }
                    }
                }

                // Completion of boolean parameter
                if (parameter.getParameterField().getType() != null) {
                    if (parameter.getParameterField().getType().getName().toLowerCase(Locale.getDefault()).equals("boolean")) {
                        Set<String> attributes = ClassUtils.getClassProperties(elementClass).keySet();
                        attributes.add("literal:true");
                        attributes.add("literal:false");

                        return attributes.toArray();
                    }
                }

                // Completion of all attributes whose default prefix is "prop"
                if (parameter.getDefaultPrefix().equals("prop")) {
                    return ClassUtils.getClassProperties(elementClass).keySet().toArray();
                }

                return new Object[0];
            }
        }
        return new Object[0];
    }

    /**
     * Returns list of all page names in this tapestry project
     *
     * @param tapestryProject the tapestry project
     * @return list with all the page names
     */
    private static String[] completePageLinkPageParameter(TapestryProject tapestryProject) {
        List<String> names = new ArrayList<String>();

        for (Library library : tapestryProject.getLibraries())
            for (PresentationLibraryElement page : library.getPages().values())
                names.add(page.getName());

        return names.toArray(new String[names.size()]);
    }
}