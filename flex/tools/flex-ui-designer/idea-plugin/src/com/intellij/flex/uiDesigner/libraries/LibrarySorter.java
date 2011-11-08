package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.ComplementSwfBuilder;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.flex.uiDesigner.abc.*;
import com.intellij.flex.uiDesigner.io.IOUtil;
import com.intellij.flex.uiDesigner.libraries.FlexOverloadedClasses.InjectionClassifier;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.text.CharArrayUtil;
import gnu.trove.THashMap;
import gnu.trove.TObjectProcedure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.intellij.flex.uiDesigner.libraries.Definition.ResolvedState;

public class LibrarySorter {
  private final Module module;

  private GlobalSearchScope definitionSearchScope;
  private boolean useIndexForFindDefinitions;
  private char[] fqnBuffer;

  public LibrarySorter(@NotNull Module module) {
    this.module = module;
  }

  private static long createInjectionAbc(String flexSdkVersion, InjectionClassifier classifier, boolean force) throws IOException {
    final String rootPath = DebugPathManager.getFudHome() + "/flex-injection/target";
    File abcSource = ComplementSwfBuilder.getSourceFile(rootPath, flexSdkVersion);
    File abc = ComplementSwfBuilder.createAbcFile(rootPath, flexSdkVersion, classifier);
    if (!force && abcSource.lastModified() < abc.lastModified()) {
      return abc.lastModified();
    }

    ComplementSwfBuilder.build(rootPath, flexSdkVersion, classifier);
    return abc.lastModified();
  }

  private static List<LibrarySetItem> collectItems(final List<Library> libraries, Map<CharSequence, Definition> definitionMap,
                                                   Condition<String> isExternal) throws IOException {
    final List<LibrarySetItem> items = new ArrayList<LibrarySetItem>(libraries.size());
    final CatalogXmlBuilder catalogXmlBuilder = new CatalogXmlBuilder(definitionMap, isExternal);
    for (Library library : libraries) {
      LibrarySetItem item = new LibrarySetItem(library);
      catalogXmlBuilder.setLibrary(item);
      IOUtil.parseXml(library.getCatalogFile(), catalogXmlBuilder);
      if (item.hasDefinitions() || library.hasResourceBundles()) {
        items.add(item);
      }
    }

    return items;
  }

  public SortResult sort(final List<Library> libraries, final boolean isFromSdk, File outFile,
                         Condition<String> isExternal, @Nullable Pass<Map<CharSequence, Definition>> definitionMapPostProcessor) throws IOException {
    useIndexForFindDefinitions = !isFromSdk;

    ArrayList<Library> resourceBundleOnlyItems = null;

    final THashMap<CharSequence, Definition> definitionMap = new THashMap<CharSequence, Definition>(libraries.size() * 128, AbcFilter.HASHING_STRATEGY);
    final List<LibrarySetItem> unsortedItems = collectItems(libraries, definitionMap, isExternal);
    final AbcMerger abcMerger = new AbcMerger(definitionMap, outFile);
    try {
      final ArrayList<Library> items = new ArrayList<Library>(unsortedItems.size());
      for (LibrarySetItem item : unsortedItems) {
        if (!item.hasDefinitions()) {
          if (item.library.hasResourceBundles()) {
            if (resourceBundleOnlyItems == null) {
              resourceBundleOnlyItems = new ArrayList<Library>();
            }
            resourceBundleOnlyItems.add(item.library);
          }
          continue;
        }

        items.add(item.library);
        abcMerger.process(item.library);
      }
      
      if (definitionMapPostProcessor != null) {
        definitionMapPostProcessor.pass(definitionMap);
      }

      final List<Decoder> decoders = new ArrayList<Decoder>(definitionMap.size());
      definitionMap.forEachValue(new TObjectProcedure<Definition>() {
        @Override
        public boolean execute(Definition definition) {
          final BufferWrapper abcData = definition.doAbcData;
          if (abcData == null) {
            return true;
          }

          // dependencies may be cyclic, see charts.swc from Flex SDK 4.5.1 (mx.charts.chartClasses:DataTransform and mx.charts.chartClasses:IChartElement)
          // but we must write definition _only_ after it's dependencies (any way, even if it is important only if dep type is inheritance)
          definition.doAbcData = null;
          if (definition.dependencies != null && (definition.resolved == ResolvedState.YES ||
                                                  (definition.resolved == ResolvedState.UNKNOWN &&
                                                   processDependencies(decoders, definition, definitionMap)))) {
              decoders.add(new Decoder(abcData));
          }

          return true;
        }
      });

      final Encoder encoder = new Encoder();
      //final Encoder encoder = flexSdkVersion != null ? new FlexEncoder("test", flexSdkVersion) : new Encoder();
      abcMerger.end(decoders, encoder);
      return new SortResult(definitionMap, items, resourceBundleOnlyItems);
    }
    finally {
      abcMerger.close();
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @TestOnly
  private static Map<CharSequence, Definition> getDefinitions(LibrarySetItem library, THashMap<CharSequence, Definition> definitionMap) {
    Map<CharSequence, Definition> definitions = new HashMap<CharSequence, Definition>();
    for (Entry<CharSequence, Definition> entry : definitionMap.entrySet()) {
      if (entry.getValue().getLibrary() == library) {
        definitions.put(entry.getKey(), entry.getValue());
      }
    }

    return definitions;
  }

  private boolean processDependencies(List<Decoder> decoders, Definition definition,
                                      Map<CharSequence, Definition> definitionMap) throws DecoderException {
    // set before to prevent stack overflow for crossed dependencies
    definition.resolved = ResolvedState.YES;

    final String[] dependencies = definition.dependencies;
    for (int i = 0, dependenciesLength = dependencies.length; i < dependenciesLength; i++) {
      final String dependencyId = dependencies[i];
      final Definition dependency = definitionMap.get(dependencyId);
      if (dependency == null && useIndexForFindDefinitions) {
        final int length = dependencyId.length();
        if (definitionSearchScope == null) {
          definitionSearchScope = module.getModuleWithDependenciesAndLibrariesScope(false);
          fqnBuffer = new char[Math.max(length, 512)];
        }
        else if (fqnBuffer.length < length) {
          fqnBuffer = new char[length];
        }

        dependencyId.getChars(0, length, fqnBuffer, 0);
        fqnBuffer[CharArrayUtil.lastIndexOf(fqnBuffer, ':', 1, length - 1)] = '.';
        if (JSResolveUtil.findClassByQName(new String(fqnBuffer, 0, length), definitionSearchScope) != null) {
          dependencies[i] = null;
          continue;
        }
      }

      final BufferWrapper depAbcData;
      if (dependency == null) {
        depAbcData = null;
      }
      else {
        depAbcData = dependency.doAbcData;
        dependency.doAbcData = null;
      }

      if (dependency == null || dependency.resolved == ResolvedState.NO ||
          (dependency.resolved == ResolvedState.UNKNOWN && !processDependencies(decoders, dependency, definitionMap))) {
        definition.markAsUnresolved();
        definition.resolved = ResolvedState.NO;
        return false;
      }

      if (depAbcData != null) {
        decoders.add(new Decoder(depAbcData));
        dependency.doAbcData = null;
      }
    }

    return true;
  }

  static class SortResult {
    final THashMap<CharSequence, Definition> definitionMap;
    final List<Library> items;
    final List<Library> resourceBundleOnlyItems;

    private SortResult(THashMap<CharSequence, Definition> definitionMap, List<Library> items,
                       List<Library> resourceBundleOnlyItems) {
      this.definitionMap = definitionMap;
      this.items = items;
      this.resourceBundleOnlyItems = resourceBundleOnlyItems;
    }
  }
}