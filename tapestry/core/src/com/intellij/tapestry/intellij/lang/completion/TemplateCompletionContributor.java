package com.intellij.tapestry.intellij.lang.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.presentation.TapestryComponent;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ResolvedValue;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverChain;
import com.intellij.tapestry.core.util.ClassUtils;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.lang.descriptor.TapestryXmlExtension;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class TemplateCompletionContributor extends CompletionContributor {
  private static final Logger _logger = Logger.getInstance(TemplateCompletionContributor.class);

  public TemplateCompletionContributor() {
    extend(null, psiElement(), new CompletionProvider<>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    @NotNull ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        PsiElement psiElement = parameters.getPosition();

        if (!(psiElement instanceof LeafPsiElement)) return;
        PsiElement prev = psiElement.getPrevSibling();
        if (prev != null && ".".equals(prev.getText())) return;

        Module module = ModuleUtilCore.findModuleForPsiElement(psiElement);

        // if this isn't a Tapestry module don't do anything
        if (!TapestryUtils.isTapestryModule(module)) return;

        if (psiElement instanceof XmlToken && ((XmlToken)psiElement).getTokenType().toString().equals("XML_ATTRIBUTE_VALUE_TOKEN")) {
          // The selected attribute
          XmlAttribute attribute = PsiTreeUtil.getParentOfType(psiElement, XmlAttribute.class);

          // The selected tag
          XmlTag tag = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class);

          if (attribute == null) {
            return;
          }

          // Completion of type and id attributes is handled by ComponentNameContextGetter
          if (TapestryXmlExtension.isTapestryTemplateNamespace(attribute.getNamespace()) &&
              (attribute.getLocalName().equals("type") ||
               attribute.getLocalName().equals("id") ||
               attribute.getLocalName().equals("mixins"))) {
            return;
          }

          // Try to match the tag to a component
          TapestryComponent component = TapestryUtils.getTypeOfTag(tag);
          if (component == null) return;

          TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);
          if (tapestryProject == null) return;

          final PresentationLibraryElement element = tapestryProject.findElementByTemplate(parameters.getOriginalFile());
          if (element == null) return;

          IntellijJavaClassType elementClass = (IntellijJavaClassType)element.getElementClass();
          if (elementClass == null) return;

          for (TapestryParameter parameter : component.getParameters().values()) {
            String attributeValue = "";
            if (tag != null) {
              attributeValue = tag.getAttributeValue(attribute.getLocalName());
            }

            if (!parameter.getName().equalsIgnoreCase(attribute.getLocalName())) {
              continue;
            }

            if (attributeValue != null) {
              // Completion of all attribute values that starts with "prop:"
              if (attributeValue.equals("prop:IntellijIdeaRulezzz ")) {
                Set<String> properties = ClassUtils.getClassProperties(elementClass).keySet();
                ArrayList<String> returnedProperties = new ArrayList<>();

                for (String property : properties) {
                  returnedProperties.add("prop:" + property);
                }
                addVariants(result, returnedProperties);
                return;
              }

              // Completion of composed properties
              if (attributeValue.contains(".") &&
                  !attributeValue.contains("..") &&
                  (parameter.getDefaultPrefix().equals("prop") || attributeValue.startsWith("prop:"))) {

                Set<String> properties;

                if (attributeValue.contains("." + CompletionInitializationContext.DUMMY_IDENTIFIER)) {
                  String words = "";
                  try (Scanner scan = new Scanner(attributeValue)) {
                    String word = "";
                    while (scan.hasNext() && !word.contains(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED)) {
                      word = scan.next();
                      words += word;
                    }
                  }
                  attributeValue = words.replaceFirst(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED, "");
                }

                ResolvedValue resolvedValue;
                try {
                  resolvedValue =
                    ValueResolverChain.getInstance().resolve(tapestryProject, elementClass, attributeValue, parameter.getDefaultPrefix());
                }
                catch (Exception ex) {
                  _logger.error(ex);

                  return;
                }

                if (resolvedValue != null && resolvedValue.getType() != null && resolvedValue.getType() instanceof IJavaClassType) {
                  elementClass =
                    new IntellijJavaClassType(module, ((IntellijJavaClassType)resolvedValue.getType()).getPsiClass().getContainingFile());
                  properties = ClassUtils.getClassProperties(elementClass).keySet();

                  ArrayList<String> returnedProperties = new ArrayList<>();
                  for (String property : properties) {
                    returnedProperties.add(attributeValue + property);
                  }

                  addVariants(result, returnedProperties);
                  return;
                }
              }
            }

            // Completion of boolean parameter
            if (parameter.getParameterField().getType() != null) {
              if (parameter.getParameterField().getType().getName().toLowerCase(Locale.getDefault()).equals("boolean")) {
                Set<String> attributes = new HashSet<>(ClassUtils.getClassProperties(elementClass).keySet());
                attributes.add("literal:true");
                attributes.add("literal:false");

                addVariants(result, attributes);
                return;
              }
            }

            // Completion of all attributes whose default prefix is "prop"
            if (parameter.getDefaultPrefix().equals("prop")) {
              addVariants(result, ClassUtils.getClassProperties(elementClass).keySet());
            }
          }
        }
      }

      private void addVariants(@NotNull CompletionResultSet result, Collection<String> returnedProperties) {
        for (String property : returnedProperties) {
          result.addElement(LookupElementBuilder.create(property).withCaseSensitivity(false));
        }
      }
    });
  }
}
