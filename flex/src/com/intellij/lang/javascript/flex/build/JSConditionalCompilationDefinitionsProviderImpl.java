package com.intellij.lang.javascript.flex.build;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.execution.configurations.CommandLineTokenizer;
import com.intellij.lang.javascript.JSConditionalCompilationDefinitionsProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Processor;
import gnu.trove.THashMap;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static com.intellij.lang.javascript.flex.build.FlexBuildConfiguration.ConditionalCompilationDefinition;
import static com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil.*;

public class JSConditionalCompilationDefinitionsProviderImpl implements JSConditionalCompilationDefinitionsProvider {

  private static String[] CONDITIONAL_COMPILATION_DEFINITION_OPTION_ALIASES = {"define", "compiler.define"};

  private Map<VirtualFile, Long> configFileToTimestamp = new THashMap<VirtualFile, Long>();
  private Map<VirtualFile, Collection<ConditionalCompilationDefinition>> configFileToConditionalCompilerDefinitions =
    new THashMap<VirtualFile, Collection<ConditionalCompilationDefinition>>();

  public boolean containsConstant(final Module module, final String namespace, final String constantName) {
    if (module != null && !StringUtil.isEmpty(namespace) && !StringUtil.isEmpty(constantName)) {
      final boolean searchForPrefix = constantName.indexOf(CompletionUtil.DUMMY_IDENTIFIER_TRIMMED) != -1;
      final String searchedName = namespace + "::" + (searchForPrefix ? "" : constantName);
      final Ref<Boolean> result = new Ref<Boolean>(false);

      processConditionalCompilationDefinitions(module, new Processor<ConditionalCompilationDefinition>() {
        public boolean process(final ConditionalCompilationDefinition definition) {
          if ((searchForPrefix && definition.NAME.startsWith(searchedName)) ||
              (!searchForPrefix && definition.NAME.equals(searchedName))
            ) {
            result.set(true);
            return false;
          }
          return true;
        }
      });

      return result.get();
    }
    return false;
  }

  public Collection<String> getConstantNamesForNamespace(final Module module, final String namespace) {
    final Collection<String> result = new ArrayList<String>();

    if (module != null && !StringUtil.isEmpty(namespace)) {
      final String beginning = namespace + "::";

      processConditionalCompilationDefinitions(module, new Processor<ConditionalCompilationDefinition>() {
        public boolean process(final ConditionalCompilationDefinition definition) {
          if (definition.NAME.startsWith(beginning)) {
            result.add(definition.NAME.substring(beginning.length()));
          }
          return true;
        }
      });
    }

    return result;
  }

  public Collection<String> getAllConstants(final Module module) {
    final Collection<String> result = new ArrayList<String>();

    if (module != null) {
      processConditionalCompilationDefinitions(module, new Processor<ConditionalCompilationDefinition>() {
        public boolean process(final ConditionalCompilationDefinition definition) {
          result.add(definition.NAME);
          return true;
        }
      });
    }

    return result;
  }

  private void processConditionalCompilationDefinitions(final Module module, final Processor<ConditionalCompilationDefinition> processor) {
    outer:
    for (FlexBuildConfiguration config : FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module)) {
      final Collection<ConditionalCompilationDefinition> definitions = config.USE_CUSTOM_CONFIG_FILE
                                                                       ? getDefinitionsFromConfigFile(config.CUSTOM_CONFIG_FILE)
                                                                       : config.CONDITIONAL_COMPILATION_DEFINITION_LIST;
      for (ConditionalCompilationDefinition definition : definitions) {
        if (!processor.process(definition)) break outer;
      }

      for (ConditionalCompilationDefinition definition : getDefinitionsFromCompilerOptions(config.ADDITIONAL_COMPILER_OPTIONS)) {
        if (!processor.process(definition)) break outer;
      }
    }
  }

  private Collection<ConditionalCompilationDefinition> getDefinitionsFromConfigFile(final String configFilePath) {
    if (!StringUtil.isEmpty(configFilePath)) {
      final VirtualFile customConfigFile = LocalFileSystem.getInstance().findFileByPath(configFilePath);
      if (customConfigFile != null && !customConfigFile.isDirectory()) {
        final Long currentTimestamp = customConfigFile.getModificationCount();
        final Long cachedTimestamp = configFileToTimestamp.get(customConfigFile);

        if (cachedTimestamp == null || !cachedTimestamp.equals(currentTimestamp)) {
          configFileToTimestamp.remove(customConfigFile);
          configFileToConditionalCompilerDefinitions.remove(customConfigFile);

          try {
            final Document document = JDOMUtil.loadDocument(customConfigFile.getInputStream());
            final Collection<ConditionalCompilationDefinition> result = new ArrayList<ConditionalCompilationDefinition>();

            final Element rootElement = document.getRootElement();
            if (rootElement.getName().equals(FLEX_CONFIG)) {
              // noinspection unchecked
              for (Element compilerElement : ((Iterable<Element>)rootElement
                .getChildren(COMPILER, rootElement.getNamespace()))) {
                // noinspection unchecked
                for (Element defineElement : ((Iterable<Element>)compilerElement
                  .getChildren(DEFINE, rootElement.getNamespace()))) {
                  final String name = defineElement.getChildText(NAME, rootElement.getNamespace());
                  final String value = defineElement.getChildText(VALUE, rootElement.getNamespace());
                  if (!StringUtil.isEmpty(name) && value != null) {
                    final ConditionalCompilationDefinition definition = new ConditionalCompilationDefinition();
                    definition.NAME = name;
                    definition.VALUE = value;
                    result.add(definition);
                  }
                }
              }
            }

            configFileToTimestamp.put(customConfigFile, currentTimestamp);
            configFileToConditionalCompilerDefinitions.put(customConfigFile, result);
            return result;
          }
          catch (JDOMException e) {/*ignore*/}
          catch (IOException e) {/*ignore*/}
        }
        else {
          return configFileToConditionalCompilerDefinitions.get(customConfigFile);
        }
      }
    }
    return Collections.emptyList();
  }

  public static Collection<ConditionalCompilationDefinition> getDefinitionsFromCompilerOptions(final String compilerOptions) {
    if (StringUtil.isEmpty(compilerOptions)) {
      return Collections.emptyList();
    }

    final Collection<ConditionalCompilationDefinition> result = new ArrayList<ConditionalCompilationDefinition>();

    for (CommandLineTokenizer stringTokenizer = new CommandLineTokenizer(compilerOptions); stringTokenizer.hasMoreTokens(); ) {
      final String token = stringTokenizer.nextToken();
      for (String option : CONDITIONAL_COMPILATION_DEFINITION_OPTION_ALIASES) {
        if (token.startsWith("-" + option + "=") || token.startsWith("-" + option + "+=")) {
          final String optionValue = token.substring(token.indexOf("=") + 1);
          final int commaIndex = optionValue.indexOf(',');
          if (commaIndex > 0) {
            final String name = optionValue.substring(0, commaIndex);
            if (name.matches(FlexCompiler.CONDITIONAL_COMPILATION_VARIABLE_PATTERN)) {
              final ConditionalCompilationDefinition definition = new ConditionalCompilationDefinition();
              definition.NAME = name;
              definition.VALUE = optionValue.substring(commaIndex + 1);
              result.add(definition);
            }
          }
        }
        else if (token.equals("-" + option)) {
          if (stringTokenizer.countTokens() >= 2) {
            final String name = stringTokenizer.peekNextToken();
            if (name.matches(FlexCompiler.CONDITIONAL_COMPILATION_VARIABLE_PATTERN)) {
              stringTokenizer.nextToken(); // advance tokenizer position
              final String value = stringTokenizer.peekNextToken();
              if (isOptionValue(value)) {
                stringTokenizer.nextToken(); // advance tokenizer position

                final ConditionalCompilationDefinition definition = new ConditionalCompilationDefinition();
                definition.NAME = name;
                definition.VALUE = value;
                result.add(definition);
              }
            }
          }
        }
      }
    }

    return result;
  }

  private static boolean isOptionValue(final String text) {
    if (text.startsWith("-")) {  // option or negative number
      return text.length() > 1 && Character.isDigit(text.charAt(1));
    }
    return !text.startsWith("+");
  }
}
