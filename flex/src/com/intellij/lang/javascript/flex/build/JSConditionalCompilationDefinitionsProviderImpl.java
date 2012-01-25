package com.intellij.lang.javascript.flex.build;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.execution.configurations.CommandLineTokenizer;
import com.intellij.lang.javascript.JSConditionalCompilationDefinitionsProvider;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.projectStructure.FlexProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.ModuleOrProjectCompilerOptions;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
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

import static com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil.*;

public class JSConditionalCompilationDefinitionsProviderImpl implements JSConditionalCompilationDefinitionsProvider {

  private static String[] CONDITIONAL_COMPILATION_DEFINITION_OPTION_ALIASES = {"define", "compiler.define"};

  private Map<VirtualFile, Long> configFileToTimestamp = new THashMap<VirtualFile, Long>();
  private Map<VirtualFile, Collection<String>> configFileToConditionalCompilerDefinitions = new THashMap<VirtualFile, Collection<String>>();

  public boolean containsConstant(final Module module, final String namespace, final String constantName) {
    if (module != null && ModuleType.get(module) instanceof FlexModuleType
        && !StringUtil.isEmpty(namespace) && !StringUtil.isEmpty(constantName)) {
      final boolean searchForPrefix = constantName.contains(CompletionUtil.DUMMY_IDENTIFIER_TRIMMED);
      final String searchedName = namespace + "::" + (searchForPrefix ? "" : constantName);
      final Ref<Boolean> result = new Ref<Boolean>(false);

      processConditionalCompilationDefinitions(module, new Processor<String>() {
        public boolean process(final String name) {
          if ((searchForPrefix && name.startsWith(searchedName)) || (!searchForPrefix && name.equals(searchedName))) {
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

    if (module != null && ModuleType.get(module) instanceof FlexModuleType && !StringUtil.isEmpty(namespace)) {
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

    if (module != null && ModuleType.get(module) instanceof FlexModuleType) {
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
    final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(module);
    final FlexIdeBuildConfiguration bc = manager.getActiveConfiguration();

    final ModuleOrProjectCompilerOptions moduleLevelOptions = manager.getModuleLevelCompilerOptions();
    final ModuleOrProjectCompilerOptions projectLevelOptions =
      FlexProjectLevelCompilerOptionsHolder.getInstance(module.getProject()).getProjectLevelCompilerOptions();

    String rawValue = bc.getCompilerOptions().getOption("compiler.define");
    if (rawValue == null) rawValue = moduleLevelOptions.getOption("compiler.define");
    if (rawValue == null) rawValue = projectLevelOptions.getOption("compiler.define");

    if (rawValue != null) {

      int pos = 0;
      while (true) {
        int index = rawValue.indexOf(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR, pos);
        if (index == -1) break;

        String token = rawValue.substring(pos, index);
        final int tabIndex = token.indexOf(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR);

        if (tabIndex > 0 && !processor.process(token.substring(0, tabIndex))) return;

        pos = index + 1;
      }

      final int tabIndex = rawValue.indexOf(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR, pos);
      if (tabIndex > pos) {
        if (!processor.process(rawValue.substring(pos, tabIndex))) return;
      }
    }

    for (String definition : getDefinitionsFromCompilerOptions(projectLevelOptions.getAdditionalOptions())) {
      if (!processor.process(definition)) return;
    }
    for (String definition : getDefinitionsFromCompilerOptions(moduleLevelOptions.getAdditionalOptions())) {
      if (!processor.process(definition)) return;
    }
    for (String definition : getDefinitionsFromCompilerOptions(bc.getCompilerOptions().getAdditionalOptions())) {
      if (!processor.process(definition)) return;
    }
    for (String definition : getDefinitionsFromConfigFile(bc.getCompilerOptions().getAdditionalConfigFilePath())) {
      if (!processor.process(definition)) return;
    }
  }

  private Collection<String> getDefinitionsFromConfigFile(final String configFilePath) {
    if (StringUtil.isEmptyOrSpaces(configFilePath)) return Collections.emptyList();

    final VirtualFile configFile = LocalFileSystem.getInstance().findFileByPath(configFilePath);
    if (configFile == null || configFile.isDirectory()) return Collections.emptyList();

    final FileDocumentManager documentManager = FileDocumentManager.getInstance();
    final com.intellij.openapi.editor.Document cachedDocument = documentManager.getCachedDocument(configFile);
    final Long currentTimestamp = cachedDocument != null ? cachedDocument.getModificationStamp() : configFile.getModificationCount();
    final Long cachedTimestamp = configFileToTimestamp.get(configFile);

    if (cachedTimestamp == null || !cachedTimestamp.equals(currentTimestamp)) {
      configFileToTimestamp.remove(configFile);
      configFileToConditionalCompilerDefinitions.remove(configFile);

      try {
        final Document document = cachedDocument == null
                                  ? JDOMUtil.loadDocument(configFile.getInputStream())
                                  : JDOMUtil.loadDocument(cachedDocument.getCharsSequence());
        final Collection<String> result = new ArrayList<String>();

        final Element rootElement = document.getRootElement();
        if (rootElement.getName().equals(FLEX_CONFIG)) {
          // noinspection unchecked
          for (Element compilerElement : ((Iterable<Element>)rootElement.getChildren(COMPILER, rootElement.getNamespace()))) {
            // noinspection unchecked
            for (Element defineElement : ((Iterable<Element>)compilerElement.getChildren(DEFINE, rootElement.getNamespace()))) {
              final String name = defineElement.getChildText(NAME, rootElement.getNamespace());
              final String value = defineElement.getChildText(VALUE, rootElement.getNamespace());
              if (!StringUtil.isEmpty(name) && value != null) {
                result.add(name);
              }
            }
          }
        }

        configFileToTimestamp.put(configFile, currentTimestamp);
        configFileToConditionalCompilerDefinitions.put(configFile, result);

        return result;
      }
      catch (JDOMException e) {/*ignore*/}
      catch (IOException e) {/*ignore*/}

      return Collections.emptyList();
    }
    else {
      return configFileToConditionalCompilerDefinitions.get(configFile);
    }
  }

  private static Collection<String> getDefinitionsFromCompilerOptions(final String compilerOptions) {
    if (StringUtil.isEmpty(compilerOptions)) {
      return Collections.emptyList();
    }

    final Collection<String> result = new ArrayList<String>();

    for (CommandLineTokenizer stringTokenizer = new CommandLineTokenizer(compilerOptions); stringTokenizer.hasMoreTokens(); ) {
      final String token = stringTokenizer.nextToken();
      for (String option : CONDITIONAL_COMPILATION_DEFINITION_OPTION_ALIASES) {
        if (token.startsWith("-" + option + "=") || token.startsWith("-" + option + "+=")) {
          final String optionValue = token.substring(token.indexOf("=") + 1);
          final int commaIndex = optionValue.indexOf(',');
          if (commaIndex > 0) {
            result.add(optionValue.substring(0, commaIndex));
          }
        }
        else if (token.equals("-" + option) && stringTokenizer.countTokens() >= 2) {
          final String name = stringTokenizer.peekNextToken();
          stringTokenizer.nextToken(); // advance tokenizer position
          final String value = stringTokenizer.peekNextToken();
          if (FlexUtils.canBeCompilerOptionValue(value)) {
            stringTokenizer.nextToken(); // advance tokenizer position
            result.add(name);
          }
        }
      }
    }

    return result;
  }
}
