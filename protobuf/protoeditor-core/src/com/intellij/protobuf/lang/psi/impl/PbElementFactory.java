/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.DummyHolder;
import com.intellij.psi.impl.source.DummyHolderFactory;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.lang.PbParserDefinition;
import com.intellij.protobuf.lang.psi.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Builders that help generate synthetic proto PSI elements. */
public class PbElementFactory {

  public static PbElementFactory getInstance(PsiFile file) {
    return new PbElementFactory(file);
  }

  private final PsiFile file;
  private final Project project;
  private final PsiManager manager;

  private PbElementFactory(PsiFile file) {
    this.file = file;
    this.project = file.getProject();
    this.manager = file.getManager();
  }

  public TypeNameBuilder typeNameBuilder() {
    return new TypeNameBuilder();
  }

  FieldLabelBuilder fieldLabelBuilder() {
    return new FieldLabelBuilder();
  }

  OptionBuilder optionBuilder() {
    return new OptionBuilder();
  }

  MessageBuilder messageBuilder() {
    return new MessageBuilder();
  }

  FieldBuilder fieldBuilder() {
    return new FieldBuilder();
  }

  abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
    private PsiElement parent = null;
    private PsiElement navigationElement = null;
    private TextRange textRange = null;

    abstract T getThis();

    public T setParent(PsiElement parent) {
      this.parent = parent;
      return getThis();
    }

    public T setNavigationElement(PsiElement navigationElement) {
      this.navigationElement = navigationElement;
      return getThis();
    }

    public T setTextRange(TextRange textRange) {
      this.textRange = textRange;
      return getThis();
    }

    void updateCommonFields(PbElement element) {
      // PbOverridableElement is a package-private interface and not extended by PbCompositeElement.
      // However, all implementations of PbCompositeElement should also implement
      // PbOverridableElement.
      if (!(element instanceof PbOverridableElement)) {
        throw new IllegalArgumentException("element must extend PbOverridableElement");
      }
      updateCommonFields((PbOverridableElement) element);
    }

    void updateCommonFields(PbOverridableElement element) {
      if (parent != null) {
        element.setParentOverride(parent);
      }
      if (navigationElement != null) {
        element.setNavigationElementOverride(navigationElement);
      }
      if (textRange != null) {
        element.setTextRangeOverride(textRange);
      }
    }
  }

  /** A builder that constructs a typename. E.g., ".com.foo.bar.Message". */
  public class TypeNameBuilder extends AbstractBuilder<TypeNameBuilder> {
    private String name = null;

    @Override
    TypeNameBuilder getThis() {
      return this;
    }

    public TypeNameBuilder setName(String name) {
      this.name = name;
      return this;
    }

    public TypeNameBuilder setName(QualifiedName name) {
      this.name = name.toString();
      return this;
    }

    public PbTypeName build() {
      if (name == null) {
        throw new IllegalStateException("name is missing");
      }
      PbTypeName element = (PbTypeName) parseLight(PbTypes.TYPE_NAME, name);
      updateCommonFields(element);
      return element;
    }
  }

  class FieldLabelBuilder extends AbstractBuilder<FieldLabelBuilder> {
    private String label = null;

    @Override
    FieldLabelBuilder getThis() {
      return this;
    }

    public FieldLabelBuilder setLabel(String label) {
      this.label = label;
      return this;
    }

    public PbFieldLabel build() {
      if (label == null) {
        throw new IllegalStateException("label is missing");
      }
      PbFieldLabel element = (PbFieldLabel) parseLight(PbTypes.FIELD_LABEL, label);
      updateCommonFields(element);
      return element;
    }
  }

  class OptionBuilder extends AbstractBuilder<OptionBuilder> {
    String name;
    String value;

    @Override
    public OptionBuilder getThis() {
      return this;
    }

    public OptionBuilder setName(String name) {
      this.name = name;
      return this;
    }

    public OptionBuilder setRawValue(String value) {
      this.value = value;
      return this;
    }

    public OptionBuilder setValue(String value) {
      this.value = "'" + value + "'";
      return this;
    }

    public OptionBuilder setValue(boolean value) {
      this.value = Boolean.toString(value);
      return this;
    }

    public OptionBuilder setValue(long value) {
      this.value = Long.toString(value);
      return this;
    }

    public OptionBuilder setValue(double value) {
      this.value = Double.toString(value);
      return this;
    }

    public PbOptionExpression buildExpression() {
      validate();
      String text = String.format("%s = %s", name, value);
      PbOptionExpression element = (PbOptionExpression) parseLight(PbTypes.OPTION_EXPRESSION, text);
      updateCommonFields(element);
      return element;
    }

    public PbOptionStatement buildStatement() {
      validate();
      String text = String.format("option %s = %s;", name, value);
      PbOptionStatement element = (PbOptionStatement) parseLight(PbTypes.OPTION_STATEMENT, text);
      updateCommonFields(element);
      return element;
    }

    private void validate() {
      if (name == null) {
        throw new IllegalStateException("name is missing");
      }
      if (value == null) {
        throw new IllegalStateException("value is missing");
      }
    }
  }

  class MessageBuilder extends AbstractBuilder<MessageBuilder> {
    private String name;
    private final List<PbStatement> members = new ArrayList<>();

    @Override
    public MessageBuilder getThis() {
      return this;
    }

    public MessageBuilder setName(String name) {
      this.name = name;
      return this;
    }

    public MessageBuilder addStatement(PbStatement statement) {
      members.add(statement);
      return this;
    }

    public PbMessageDefinition build() {
      if (name == null) {
        throw new IllegalStateException("name is missing");
      }

      String text = String.format("message %s {}", name);
      PbMessageDefinition element =
          (PbMessageDefinition) parseLight(PbTypes.MESSAGE_DEFINITION, text);
      updateCommonFields(element);
      PbMessageBody body = element.getBody();
      if (body == null) {
        throw new IllegalStateException("generated message has no body");
      }
      members.forEach(body::add);
      return element;
    }
  }

  class FieldBuilder extends AbstractBuilder<FieldBuilder> {
    private String label;
    private String type;
    private String name;
    private int number = 0;
    private String optionText;
    private final List<PbOptionExpression> options = new ArrayList<>();

    @Override
    public FieldBuilder getThis() {
      return this;
    }

    public FieldBuilder setLabel(PbFieldLabel label) {
      this.label = label != null ? label.getText() : null;
      return this;
    }

    public FieldBuilder setLabel(String label) {
      this.label = label;
      return this;
    }

    public FieldBuilder setType(String type) {
      this.type = type;
      return this;
    }

    public FieldBuilder setName(String name) {
      this.name = name;
      return this;
    }

    public FieldBuilder setNumber(int number) {
      this.number = number;
      return this;
    }

    public FieldBuilder setOptionText(String optionText) {
      this.optionText = optionText;
      return this;
    }

    public FieldBuilder addOption(PbOptionExpression option) {
      options.add(option);
      return this;
    }

    public PbSimpleField build() {
      if (type == null) {
        throw new IllegalStateException("type is missing");
      }
      if (name == null) {
        throw new IllegalStateException("name is missing");
      }
      if (number <= 0) {
        throw new IllegalStateException("number is missing");
      }

      if (optionText != null && !options.isEmpty()) {
        throw new IllegalArgumentException("setOptionText and addOption cannot be used together");
      }

      String labelPrefix = label != null ? label + " " : "";
      String optionSuffix = "";
      if (optionText != null) {
        optionSuffix = " " + optionText;
      } else if (!options.isEmpty()) {
        optionSuffix =
            " [" + String.join(", ", Collections.nCopies(options.size(), "key=val")) + "]";
      }
      String text = String.format("%s%s %s = %d%s;", labelPrefix, type, name, number, optionSuffix);
      PbSimpleField element = (PbSimpleField) parseLight(PbTypes.SIMPLE_FIELD, text);
      updateCommonFields(element);
      if (!options.isEmpty()) {
        replacePlaceholders(element.getOptions(), options);
      }
      return element;
    }
  }

  private PsiElement parseLight(IElementType type, CharSequence text) {
    PbParserDefinition parserDefinition = new PbParserDefinition();
    PsiBuilder builder =
        PsiBuilderFactory.getInstance()
            .createBuilder(parserDefinition, parserDefinition.createLexer(project), text);
    LightPsiParser parser = (LightPsiParser) parserDefinition.createParser(project);
    parser.parseLight(type, builder);

    ASTNode node = builder.getTreeBuilt();
    DummyHolder holder = DummyHolderFactory.createHolder(manager, null);
    holder.setOriginalFile(file);
    holder.getTreeElement().rawAddChildren((TreeElement) node);
    return node.getPsi();
  }

  private static void replacePlaceholders(
      List<? extends PsiElement> placeholders, List<? extends PsiElement> defined) {
    if (placeholders.size() != defined.size()) {
      throw new IllegalArgumentException(
          String.format(
              "placeholder count does not match defined count: %d != %d",
              placeholders.size(), defined.size()));
    }
    for (int i = 0; i < defined.size(); i++) {
      placeholders.get(i).replace(defined.get(i));
    }
  }
}
