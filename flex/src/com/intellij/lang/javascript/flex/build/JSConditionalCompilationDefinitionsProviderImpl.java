package com.intellij.lang.javascript.flex.build;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.execution.configurations.CommandLineTokenizer;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.build.FlexCompilerConfigFileUtilBase;
import com.intellij.lang.javascript.JSConditionalCompilationDefinitionsProvider;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.FlexProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.ModuleOrProjectCompilerOptions;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Pair;
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

import static com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil.DEFINE;
import static com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil.NAME;
import static com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil.VALUE;

public class JSConditionalCompilationDefinitionsProviderImpl implements JSConditionalCompilationDefinitionsProvider {

  private static String[] CONDITIONAL_COMPILATION_DEFINITION_OPTION_ALIASES = {"define", "compiler.define"};

  private Map<VirtualFile, Long> configFileToTimestamp = new THashMap<VirtualFile, Long>();
  private Map<VirtualFile, Collection<Pair<String, String>>> configFileToConditionalCompilerDefinitions =
    new THashMap<VirtualFile, Collection<Pair<String, String>>>();

  public boolean containsConstant(final Module module, final String namespace, final String constantName) {
    if (module != null && ModuleType.get(module) instanceof FlexModuleType
        && !StringUtil.isEmpty(namespace) && !StringUtil.isEmpty(constantName)) {
      final boolean searchForPrefix = constantName.contains(CompletionUtil.DUMMY_IDENTIFIER_TRIMMED);
      final String searchedName = namespace + "::" + (searchForPrefix ? "" : constantName);
      final Ref<Boolean> result = new Ref<Boolean>(false);

      processConditionalCompilationDefinitions(module, new Processor<Pair<String, String>>() {
        public boolean process(final Pair<String, String> nameAndValue) {
          if ((searchForPrefix && nameAndValue.first.startsWith(searchedName)) ||
              (!searchForPrefix && nameAndValue.first.equals(searchedName))) {
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

      processConditionalCompilationDefinitions(module, new Processor<Pair<String, String>>() {
        public boolean process(final Pair<String, String> nameAndValue) {
          if (nameAndValue.first.startsWith(beginning)) {
            result.add(nameAndValue.first.substring(beginning.length()));
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
      processConditionalCompilationDefinitions(module, new Processor<Pair<String, String>>() {
        public boolean process(final Pair<String, String> nameAndValue) {
          result.add(nameAndValue.first);
          return true;
        }
      });
    }

    return result;
  }

  private void processConditionalCompilationDefinitions(final Module module, final Processor<Pair<String, String>> processor) {
    final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(module);
    final FlexBuildConfiguration bc = manager.getActiveConfiguration();

    final ModuleOrProjectCompilerOptions moduleLevelOptions = manager.getModuleLevelCompilerOptions();
    final ModuleOrProjectCompilerOptions projectLevelOptions =
      FlexProjectLevelCompilerOptionsHolder.getInstance(module.getProject()).getProjectLevelCompilerOptions();

    if (!FlexUtils.processCompilerOption(module, bc, "compiler.define", processor)) return;

    if (!processDefinitionsFromCompilerOptions(projectLevelOptions.getAdditionalOptions(), processor)) return;
    if (!processDefinitionsFromCompilerOptions(moduleLevelOptions.getAdditionalOptions(), processor)) return;
    if (!processDefinitionsFromCompilerOptions(bc.getCompilerOptions().getAdditionalOptions(), processor)) return;

    for (Pair<String, String> nameAndValue : getDefinitionsFromConfigFile(bc.getCompilerOptions().getAdditionalConfigFilePath())) {
      if (!processor.process(nameAndValue)) return;
    }
  }

  private Collection<Pair<String, String>> getDefinitionsFromConfigFile(final String configFilePath) {
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
        final Collection<Pair<String, String>> result = new ArrayList<Pair<String, String>>();

        final Element rootElement = document.getRootElement();
        if (rootElement.getName().equals(FlexCompilerConfigFileUtilBase.FLEX_CONFIG)) {
          // noinspection unchecked
          for (Element compilerElement : ((Iterable<Element>)rootElement.getChildren(FlexCompilerConfigFileUtilBase.COMPILER, rootElement.getNamespace()))) {
            // noinspection unchecked
            for (Element defineElement : ((Iterable<Element>)compilerElement.getChildren(DEFINE, rootElement.getNamespace()))) {
              final String name = defineElement.getChildText(NAME, rootElement.getNamespace());
              final String value = defineElement.getChildText(VALUE, rootElement.getNamespace());
              if (!StringUtil.isEmpty(name) && value != null) {
                result.add(Pair.create(name, value));
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

  private static boolean processDefinitionsFromCompilerOptions(final String compilerOptions,
                                                               final Processor<Pair<String, String>> processor) {
    if (StringUtil.isEmpty(compilerOptions)) return true;

    for (CommandLineTokenizer stringTokenizer = new CommandLineTokenizer(compilerOptions); stringTokenizer.hasMoreTokens(); ) {
      final String token = stringTokenizer.nextToken();
      for (String option : CONDITIONAL_COMPILATION_DEFINITION_OPTION_ALIASES) {
        if (token.startsWith("-" + option + "=") || token.startsWith("-" + option + "+=")) {
          final String optionValue = token.substring(token.indexOf("=") + 1);
          final int commaIndex = optionValue.indexOf(',');
          if (commaIndex > 0) {
            if (!processor.process(Pair.create(optionValue.substring(0, commaIndex), optionValue.substring(commaIndex + 1)))) return false;
          }
        }
        else if (token.equals("-" + option) && stringTokenizer.countTokens() >= 2) {
          final String name = stringTokenizer.peekNextToken();
          stringTokenizer.nextToken(); // advance tokenizer position
          final String value = stringTokenizer.peekNextToken();
          if (FlexCommonUtils.canBeCompilerOptionValue(value)) {
            stringTokenizer.nextToken(); // advance tokenizer position
            if (!processor.process(Pair.create(name, value))) return false;
          }
        }
      }
    }

    return true;
  }
}
