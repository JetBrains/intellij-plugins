package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.abc.AbcFilter;
import com.intellij.flex.uiDesigner.abc.Decoder;
import com.intellij.flex.uiDesigner.abc.DecoderException;
import com.intellij.flex.uiDesigner.abc.Encoder;
import com.intellij.flex.uiDesigner.io.IOUtil;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pass;
import gnu.trove.THashMap;
import gnu.trove.TObjectProcedure;
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
  @Nullable
  private final DefinitionProcessor definitionProcessor;
  @Nullable
  private final DefinitionMapProcessor definitionMapProcessor;

  public LibrarySorter() {
    this(null, null);
  }

  public LibrarySorter(@Nullable DefinitionProcessor definitionProcessor, @Nullable DefinitionMapProcessor definitionMapProcessor) {
    this.definitionProcessor = definitionProcessor;
    this.definitionMapProcessor = definitionMapProcessor;
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

  public SortResult sort(final List<Library> libraries, File outFile, Condition<String> isExternal) throws IOException {
    ArrayList<Library> resourceBundleOnlyItems = null;

    final THashMap<CharSequence, Definition> definitionMap = new THashMap<CharSequence, Definition>(libraries.size() * 128, AbcFilter.HASHING_STRATEGY);
    final List<LibrarySetItem> unsortedItems = collectItems(libraries, definitionMap, isExternal);
    final AbcMerger abcMerger = new AbcMerger(definitionMap, outFile, definitionProcessor);
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
      
      if (definitionMapProcessor != null) {
        definitionMapProcessor.process(definitionMap, abcMerger);
      }

      final List<Decoder> decoders = new ArrayList<Decoder>(definitionMap.size());
      final String[] singleStringArray = new String[1];
      definitionMap.forEachValue(new TObjectProcedure<Definition>() {
        @Override
        public boolean execute(Definition definition) {
          if (definition.doAbcData != null &&
              (definition.resolved == ResolvedState.YES || (definition.resolved == ResolvedState.UNKNOWN &&
                                                            processDependencies(decoders, definition, definitionMap, singleStringArray)))) {
            decoders.add(new Decoder(definition.doAbcData));
            definition.doAbcData = null;
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

  private static boolean processDependencies(List<Decoder> decoders, Definition definition,
                                             Map<CharSequence, Definition> definitionMap, String[] singleStringArray) throws DecoderException {
    // set before to prevent stack overflow for crossed dependencies
    definition.resolved = ResolvedState.YES;

    final String[] dependencies;
    if (definition.dependency == null) {
      dependencies = definition.dependencies;
    }
    else {
      dependencies = singleStringArray;
      dependencies[0] = definition.dependency;
    }

    for (String dependencyId : dependencies) {
      final Definition dependency = definitionMap.get(dependencyId);
      if (dependency == null || dependency.resolved == ResolvedState.NO ||
          (dependency.resolved == ResolvedState.UNKNOWN && !processDependencies(decoders, dependency, definitionMap, singleStringArray))) {
        definition.markAsUnresolved();
        definition.resolved = ResolvedState.NO;
        return false;
      }

      if (dependency.doAbcData != null) {
        decoders.add(new Decoder(dependency.doAbcData));
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