package com.intellij.lang.javascript.flex.build;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.execution.configurations.CommandLineTokenizer;
import com.intellij.lang.javascript.JSConditionalCompilationDefinitionsProvider;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.projectStructure.FlexProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PlatformUtils;
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

      processConditionalCompilationDefinitions(module, new Processor<String>() {
        public boolean process(final String name) {
          if ((searchForPrefix && name.startsWith(searchedName)) ||
              (!searchForPrefix && name.equals(searchedName))
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

      processConditionalCompilationDefinitions(module, new Processor<String>() {
        public boolean process(final String name) {
          if (name.startsWith(beginning)) {
            result.add(name.substring(beginning.length()));
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
      processConditionalCompilationDefinitions(module, new Processor<String>() {
        public boolean process(final String name) {
          result.add(name);
          return true;
        }
      });
    }

    return result;
  }

  private void processConditionalCompilationDefinitions(final Module module, final Processor<String> processor) {
    if (PlatformUtils.isFlexIde()) {
      final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(module);

      String rawValue = manager.getActiveConfiguration().getCompilerOptions().getOption("compiler.define");
      if (rawValue == null) rawValue = manager.getModuleLevelCompilerOptions().getOption("compiler.define");
      if (rawValue == null) {
        rawValue = FlexProjectLevelCompilerOptionsHolder.getInstance(module.getProject()).getProjectLevelCompilerOptions()
          .getOption("compiler.define");
      }

      if (rawValue == null) return;

      int pos = 0;
      while (true) {
        int index = rawValue.indexOf(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR, pos);
        if (index == -1) break;
        String token = rawValue.substring(pos, index);

        final int tabIndex = token.indexOf(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR);
        if (tabIndex > 0 && !processor.process(token.substring(0, tabIndex))) {
          return;
        }

        pos = index + 1;
      }

      final int tabIndex = rawValue.indexOf(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR, pos);
      if (tabIndex > pos) {
        processor.process(rawValue.substring(pos, tabIndex));
      }
    }
    else {
      outer:
      for (FlexBuildConfiguration config : FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module)) {
        final Collection<ConditionalCompilationDefinition> definitions = config.USE_CUSTOM_CONFIG_FILE
                                                                         ? getDefinitionsFromConfigFile(config.CUSTOM_CONFIG_FILE)
                                                                         : config.CONDITIONAL_COMPILATION_DEFINITION_LIST;
        for (ConditionalCompilationDefinition definition : definitions) {
          if (!processor.process(definition.NAME)) break outer;
        }

        for (ConditionalCompilationDefinition definition : getDefinitionsFromCompilerOptions(config.ADDITIONAL_COMPILER_OPTIONS)) {
          if (!processor.process(definition.NAME)) break outer;
        }
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
        else if (token.equals("-" + option) && stringTokenizer.countTokens() >= 2) {
          final String name = stringTokenizer.peekNextToken();
          if (name.matches(FlexCompiler.CONDITIONAL_COMPILATION_VARIABLE_PATTERN)) {
            stringTokenizer.nextToken(); // advance tokenizer position
            final String value = stringTokenizer.peekNextToken();
            if (FlexUtils.canBeCompilerOptionValue(value)) {
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

    return result;
  }
}
