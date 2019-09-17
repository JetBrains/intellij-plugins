// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.entities.Angular2Directive;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;

import one.util.streamex.StreamEx;

public class AngularDuplicateAttributeInspection extends AngularHtmlLikeTemplateLocalInspectionTool {
   @Override
   protected void visitXmlTag(
         @NotNull final ProblemsHolder problemsHolder,
         @NotNull final XmlTag xmlTag) {
      StreamEx.of(xmlTag.getAttributes())
              .mapToEntry(XmlAttribute::getDescriptor, Function.identity())
              .selectKeys(Angular2AttributeDescriptor.class)
              .filterKeys(AngularDuplicateAttributeInspection::isInspectable)
              .flatMapKeyValue(AngularDuplicateAttributeInspection::toDuplicateAttribute)
              .collect(Collectors.groupingBy(duplicateAttribute -> duplicateAttribute.targetName))
              .values()
              .forEach(duplicateAttributes -> {
                 if (duplicateAttributes.size() > 1) {
                    registerProblems(problemsHolder, duplicateAttributes);
                 }
              });
   }

   private static boolean isInspectable(@NotNull final Angular2AttributeDescriptor descriptor) {
      final Angular2AttributeType type = descriptor.getInfo().type;
      return type == Angular2AttributeType.REGULAR
            || type == Angular2AttributeType.PROPERTY_BINDING
            || type == Angular2AttributeType.BANANA_BOX_BINDING;
   }

   private static Stream<DuplicateAttribute> toDuplicateAttribute(
         @NotNull final Angular2AttributeDescriptor descriptor,
         @NotNull final XmlAttribute xmlAttribute) {
      final String attributeName = descriptor.getInfo().name;

      // Check if the attribute name represents a Directive selector.
      // If it represents a Directive (or more than one) then enhance the Stream
      // with an item per each Directive name.
      // If it matches none, then a single item will be returned, using the name of the attribute
      return StreamEx.of(descriptor.getSourceDirectives())
                     .filter(directive -> matchesAttributeName(directive, attributeName))
                     .map(Angular2Directive::getName)
                     .distinct()
                     .ifEmpty(attributeName)
                     .map(targetName -> {
                        final boolean isBinding =
                              descriptor.getDeclarations()
                                        .stream()
                                        .anyMatch(d -> d instanceof JSElement);
                        return new DuplicateAttribute(
                              isBinding,
                              targetName,
                              isBinding ? descriptor.getInfo().name : targetName,
                              xmlAttribute.getNameElement()
                        );
                     });
   }

   private static boolean matchesAttributeName(
         @NotNull final Angular2Directive directive,
         @NotNull final String attributeName) {
      return StreamEx.of(directive.getSelector().getSimpleSelectors())
                     .flatCollection(Angular2DirectiveSimpleSelector::getAttrNames)
                     .anyMatch(a -> a.equalsIgnoreCase(attributeName));
   }

   private static void registerProblems(
         @NotNull final ProblemsHolder problemsHolder,
         @NotNull final List<DuplicateAttribute> duplicateAttributes) {
      final int size = duplicateAttributes.size();
      final boolean uniform =
            duplicateAttributes.stream().map(a -> a.isBinding).distinct().count() == 1;

      // All bindings            -> problem on all of them
      // All selectors           -> problem only for last one
      // Selectors and bindings  -> problem on all but last
      if (uniform) {
         final DuplicateAttribute first = duplicateAttributes.get(0);
         doRegisterProblems(
               problemsHolder,
               first.isBinding
                     ? duplicateAttributes
                     : duplicateAttributes.subList(size - 1, size),
               null,
               first.isBinding
                     ? "angular.inspection.template.duplicate-binding"
                     : "angular.inspection.template.redundant-selector"
         );
      } else {
         final Collection<DuplicateAttribute> subList = duplicateAttributes.subList(0, size - 1);
         doRegisterProblems(
               problemsHolder,
               subList,
               getFirstDirectivePresentableName(subList),
               "angular.inspection.template.redundant-selector"
         );
      }
   }

   private static void doRegisterProblems(
         @NotNull final ProblemsHolder problemsHolder,
         @NotNull final Collection<DuplicateAttribute> duplicateAttributes,
         @Nullable final String presentableName,
         @NotNull final String messageKey) {
      for (final DuplicateAttribute a : duplicateAttributes) {
         final String message = Angular2Bundle.message(
               messageKey,
               presentableName != null ? presentableName : a.presentableName
         );
         problemsHolder.registerProblem(a.xmlElement, message);
      }
   }

   private static String getFirstDirectivePresentableName(
         @NotNull final Collection<DuplicateAttribute> duplicateAttributes) {
      for (final DuplicateAttribute a : duplicateAttributes) {
         if (!a.isBinding) {
            return a.presentableName;
         }
      }

      // This should never happen
      throw new RuntimeException("Expected a Directive selector but found none");
   }

   private static class DuplicateAttribute {
      final boolean isBinding;
      final String targetName;
      final String presentableName;
      final XmlElement xmlElement;

      DuplicateAttribute(
            final boolean isBinding,
            final String targetName,
            final String presentableName,
            final XmlElement xmlElement) {
         this.isBinding = isBinding;
         this.targetName = targetName;
         this.presentableName = presentableName;
         this.xmlElement = xmlElement;
      }
   }
}
