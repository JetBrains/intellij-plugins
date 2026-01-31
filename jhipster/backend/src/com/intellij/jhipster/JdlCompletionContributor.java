// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.codeInsight.TailTypes;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.jhipster.model.JdlDeclarationsModel;
import com.intellij.jhipster.model.JdlEnumListType;
import com.intellij.jhipster.model.JdlEnumType;
import com.intellij.jhipster.model.JdlModelEnum;
import com.intellij.jhipster.model.JdlOptionMapping;
import com.intellij.jhipster.model.JdlOptionModel;
import com.intellij.jhipster.model.JdlPrimitiveType;
import com.intellij.jhipster.psi.JdlConfigBlock;
import com.intellij.jhipster.psi.JdlConfigurationOption;
import com.intellij.jhipster.psi.JdlDeployment;
import com.intellij.jhipster.psi.JdlEntitiesList;
import com.intellij.jhipster.psi.JdlEnum;
import com.intellij.jhipster.psi.JdlFieldConstraint;
import com.intellij.jhipster.psi.JdlFieldType;
import com.intellij.jhipster.psi.JdlOptionName;
import com.intellij.jhipster.psi.JdlOptionNameValue;
import com.intellij.jhipster.psi.JdlRelationshipMapping;
import com.intellij.jhipster.psi.JdlRelationshipOption;
import com.intellij.jhipster.psi.JdlRelationshipOptionId;
import com.intellij.jhipster.psi.JdlRelationshipType;
import com.intellij.jhipster.psi.JdlStringLiteral;
import com.intellij.jhipster.psi.JdlValue;
import com.intellij.jhipster.psi.JdlWithOptionValue;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static com.intellij.jhipster.JdlConstants.APPLICATION_NESTED_KEYWORDS;
import static com.intellij.jhipster.JdlConstants.CONFIGURATION_OPTION_NESTED_KEYWORDS;
import static com.intellij.jhipster.JdlConstants.FALSE;
import static com.intellij.jhipster.JdlConstants.FIELD_TYPES;
import static com.intellij.jhipster.JdlConstants.FIELD_VALIDATIONS;
import static com.intellij.jhipster.JdlConstants.RELATIONSHIP_NESTED_KEYWORDS;
import static com.intellij.jhipster.JdlConstants.RELATIONSHIP_OPTIONS;
import static com.intellij.jhipster.JdlConstants.RELATIONSHIP_OPTION_VALUES;
import static com.intellij.jhipster.JdlConstants.RELATIONSHIP_TYPES;
import static com.intellij.jhipster.JdlConstants.TOP_LEVEL_KEYWORDS;
import static com.intellij.jhipster.JdlConstants.TRUE;
import static com.intellij.jhipster.JdlPatterns.jdlApplicationBlock;
import static com.intellij.jhipster.JdlPatterns.jdlIdentifier;
import static com.intellij.jhipster.JdlPatterns.jdlTopLevelBlock;
import static com.intellij.patterns.PlatformPatterns.psiElement;

final class JdlCompletionContributor extends CompletionContributor implements DumbAware {
  JdlCompletionContributor() {
    extend(CompletionType.BASIC, jdlIdentifier().inside(jdlApplicationBlock()).andNot(psiElement().inside(JdlConfigBlock.class)),
           new KeywordsCompletionProvider(APPLICATION_NESTED_KEYWORDS));

    extend(CompletionType.BASIC, jdlIdentifier()
             .andNot(jdlIdentifier().inside(jdlTopLevelBlock()))
             .andNot(jdlIdentifier().inside(JdlWithOptionValue.class))
             .andNot(jdlIdentifier().inside(JdlEntitiesList.class)),
           new KeywordsCompletionProvider(TOP_LEVEL_KEYWORDS));

    extend(CompletionType.BASIC,
           jdlIdentifier().inside(JdlRelationshipMapping.class).andNot(psiElement().inside(JdlRelationshipOption.class)),
           new KeywordsCompletionProvider(RELATIONSHIP_NESTED_KEYWORDS));

    extend(CompletionType.BASIC, jdlIdentifier().inside(JdlRelationshipOptionId.class),
           new OptionCompletionProvider(RELATIONSHIP_OPTIONS));

    extend(CompletionType.BASIC, psiElement().inside(JdlStringLiteral.class).inside(psiElement(JdlRelationshipOption.class)),
           new OptionCompletionProvider(RELATIONSHIP_OPTION_VALUES));

    extend(CompletionType.BASIC, jdlIdentifier().inside(JdlConfigurationOption.class),
           new KeywordsCompletionProvider(CONFIGURATION_OPTION_NESTED_KEYWORDS));

    extend(CompletionType.BASIC, jdlIdentifier().inside(JdlRelationshipType.class), new CompletionProvider<>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        for (var relationshipType : RELATIONSHIP_TYPES) {
          result.addElement(LookupElementBuilder.create(relationshipType)
                              .withIcon(JdlIconsMapping.getRelationshipIcon()));
        }
      }
    });

    extend(CompletionType.BASIC, jdlIdentifier().inside(JdlFieldType.class), new CompletionProvider<>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        for (var fieldType : FIELD_TYPES.keySet()) {
          result.addElement(LookupElementBuilder.create(fieldType)
                              .withIcon(JdlIconsMapping.getFieldTypeIcon()));
        }

        var allEnums = JdlDeclarationsModel.findAllEnums(parameters.getOriginalFile());
        for (JdlEnum enumBlock : allEnums) {
          String enumId = enumBlock.getName();

          if (enumId != null && !enumId.isBlank()) {
            result.addElement(LookupElementBuilder.create(enumBlock)
                                .withIcon(JdlIconsMapping.getEnumIcon()));
          }
        }
      }
    });

    extend(CompletionType.BASIC, jdlIdentifier().withParent(JdlOptionName.class).inside(JdlConfigBlock.class),
           new CompletionProvider<>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               addOptions(result, JdlOptionModel.INSTANCE.getApplicationConfigOptions());
             }
           });

    extend(CompletionType.BASIC, jdlIdentifier().withParent(JdlOptionName.class).inside(JdlDeployment.class),
           new CompletionProvider<>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               addOptions(result, JdlOptionModel.INSTANCE.getDeploymentOptions());
             }
           });

    extend(CompletionType.BASIC, jdlIdentifier().inside(JdlValue.class).inside(JdlOptionNameValue.class).inside(JdlConfigBlock.class),
           new CompletionProvider<>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               PsiElement position = parameters.getPosition();
               PsiElement optionNameValue = PsiTreeUtil.findFirstParent(position, p -> p instanceof JdlOptionNameValue);

               if (optionNameValue instanceof JdlOptionNameValue) {
                 String key = ((JdlOptionNameValue)optionNameValue).getName();

                 addOptionValues(result, JdlOptionModel.INSTANCE.getApplicationConfigOptions().get(key));
               }
             }
           });

    extend(CompletionType.BASIC, jdlIdentifier().inside(JdlValue.class).inside(JdlOptionNameValue.class).inside(JdlDeployment.class),
           new CompletionProvider<>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               PsiElement position = parameters.getPosition();
               PsiElement optionNameValue = PsiTreeUtil.findFirstParent(position, p -> p instanceof JdlOptionNameValue);

               if (optionNameValue instanceof JdlOptionNameValue) {
                 String key = ((JdlOptionNameValue)optionNameValue).getName();

                 addOptionValues(result, JdlOptionModel.INSTANCE.getDeploymentOptions().get(key));
               }
             }
           });

    extend(CompletionType.BASIC, jdlIdentifier().inside(JdlFieldConstraint.class), new CompletionProvider<>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        for (String fieldValidation : FIELD_VALIDATIONS) {
          result.addElement(LookupElementBuilder.create(fieldValidation)
                              .withTailText(fieldValidation.endsWith("()") ? "()" : null, true)
                              .withPresentableText(fieldValidation.replace("()", ""))
                              .withIcon(JdlIconsMapping.getFieldConstraintIcon()));
        }
      }
    });
  }

  private static void addOptionValues(@NotNull CompletionResultSet result, @Nullable JdlOptionMapping optionMapping) {
    if (optionMapping != null) {
      if (optionMapping.getPropertyType() instanceof JdlEnumType) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        List<JdlModelEnum> values = ((JdlEnumType)optionMapping.getPropertyType()).getValues();

        for (JdlModelEnum value : values) {
          result.addElement(LookupElementBuilder.create(value.getId()).withIcon(JdlIconsMapping.getEnumIcon()));
        }
      }
      else if (optionMapping.getPropertyType() instanceof JdlEnumListType) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        List<JdlModelEnum> values = ((JdlEnumListType)optionMapping.getPropertyType()).getValues();

        for (JdlModelEnum value : values) {
          result.addElement(LookupElementBuilder.create(value.getId()).withIcon(JdlIconsMapping.getEnumIcon()));
        }
      }
      else if (optionMapping.getPropertyType() == JdlPrimitiveType.BOOLEAN_TYPE) {
        result.addElement(LookupElementBuilder.create(TRUE).withBoldness(true));
        result.addElement(LookupElementBuilder.create(FALSE).withBoldness(true));
      }
    }
  }

  private static void addOptions(@NotNull CompletionResultSet result, Map<String, JdlOptionMapping> options) {
    for (var optionMapping : options.values()) {
      var element = LookupElementBuilder.create(optionMapping.getName());
      if (optionMapping.getDefaultValue() != null) {
        element = element.withTailText("=" + optionMapping.getDefaultValue(), true);
      }

      result.addElement(element
                          .withTypeText(optionMapping.getPropertyType().getName())
                          .withIcon(JdlIconsMapping.getPropertyIcon()));
    }
  }

  private static final class KeywordsCompletionProvider extends CompletionProvider<CompletionParameters> {
    private final List<String> keywords;

    private KeywordsCompletionProvider(List<String> keywords) {
      this.keywords = keywords;
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
      addKeywords(result, keywords);
    }

    private static void addKeywords(@NotNull CompletionResultSet result, List<String> applicationNestedKeywords) {
      for (var topLevelKeyword : applicationNestedKeywords) {
        var element = LookupElementBuilder.create(topLevelKeyword).withBoldness(true);
        result.addElement(TailTypeDecorator.withTail(element, TailTypes.insertSpaceType()));
      }
    }
  }

  private static final class OptionCompletionProvider extends CompletionProvider<CompletionParameters> {
    private final List<String> keywords;

    private OptionCompletionProvider(List<String> keywords) {
      this.keywords = keywords;
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
      for (var k : keywords) {
        result.addElement(LookupElementBuilder.create(k));
      }
    }
  }
}