package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import icons.CucumberIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman.Chernyatchik
 */
public class GherkinColorsPage implements ColorSettingsPage {

 private static final String DEMO_TEXT =
   """
     # language: en
     Feature: Cucumber Colors Settings Page
       In order to customize Gherkin language (*.feature files) highlighting
       Our users can use this settings preview pane

       @wip
       Scenario Outline: Different Gherkin language structures
         Given Some feature file with content
         ""\"
         Feature: Some feature
           Scenario: Some scenario
         ""\"
         And I want to add new cucumber step
         And Also a step with "<regexp_param>regexp</regexp_param>" parameter
         When I open <<outline_param>ruby_ide</outline_param>>
         Then Steps autocompletion feature will help me with all these tasks

       Examples:
         | <th>ruby_ide</th> |
         | RubyMine |""";

  private static final AttributesDescriptor[] ATTRS = new AttributesDescriptor[]{
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.text"), GherkinHighlighter.TEXT),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.comment"), GherkinHighlighter.COMMENT),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.keyword"), GherkinHighlighter.KEYWORD),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.tag"), GherkinHighlighter.TAG),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.pystring"), GherkinHighlighter.PYSTRING),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.table.header.cell"), GherkinHighlighter.TABLE_HEADER_CELL),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.table.cell"), GherkinHighlighter.TABLE_CELL),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.table.pipe"), GherkinHighlighter.PIPE),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.outline.param.substitution"), GherkinHighlighter.OUTLINE_PARAMETER_SUBSTITUTION),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.regexp.param"), GherkinHighlighter.REGEXP_PARAMETER),
  };

  // Empty still
  private static final Map<String, TextAttributesKey> ADDITIONAL_HIGHLIGHT_DESCRIPTORS = new HashMap<>();
  static {
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("th", GherkinHighlighter.TABLE_HEADER_CELL);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("outline_param", GherkinHighlighter.OUTLINE_PARAMETER_SUBSTITUTION);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("regexp_param", GherkinHighlighter.REGEXP_PARAMETER);
  }

  @Override
  @Nullable
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ADDITIONAL_HIGHLIGHT_DESCRIPTORS;
  }

  @Override
  @NotNull
  public String getDisplayName() {
    return CucumberBundle.message("color.settings.gherkin.name");
  }

  @Override
  @NotNull
  public Icon getIcon() {
    return CucumberIcons.Cucumber;
  }

  @Override
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return ATTRS;
  }

  @Override
  public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override
  @NotNull
  public SyntaxHighlighter getHighlighter() {
    return new GherkinSyntaxHighlighter(new PlainGherkinKeywordProvider());
  }

  @Override
  @NotNull
  public String getDemoText() {
    return DEMO_TEXT;
  }
}
