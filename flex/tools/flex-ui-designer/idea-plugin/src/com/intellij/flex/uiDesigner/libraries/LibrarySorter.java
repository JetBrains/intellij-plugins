package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.ComplementSwfBuilder;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.flex.uiDesigner.abc.AbcFilter;
import com.intellij.flex.uiDesigner.abc.Decoder;
import com.intellij.flex.uiDesigner.abc.DecoderException;
import com.intellij.flex.uiDesigner.io.IOUtil;
import com.intellij.flex.uiDesigner.libraries.FlexOverloadedClasses.InjectionClassifier;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.text.CharArrayUtil;
import com.intellij.util.text.CharSequenceBackedByArray;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TObjectObjectProcedure;
import gnu.trove.TObjectProcedure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static com.intellij.flex.uiDesigner.libraries.Definition.UnresolvedState;
import static com.intellij.flex.uiDesigner.libraries.LibrarySorter.FlexLibsNames.*;

public class LibrarySorter {


  @SuppressWarnings("unchecked")
  final static Pair<String, String>[] FLEX_LIBS_PATTERNS = new Pair[]{
    new Pair<String, String>(FRAMEWORK, "FrameworkClasses"),
    new Pair<String, String>(AIRFRAMEWORK, "AIRFrameworkClasses"),
    new Pair<String, String>(SPARK, "SparkClasses"),
    new Pair<String, String>(AIRSPARK, "AIRSparkClasses"),

    new Pair<String, String>(MX, "MxClasses"),
    new Pair<String, String>(RPC, "RPCClasses"),
    new Pair<String, String>(MOBILECOMPONENTS, "MobileComponentsClasses")};

  private final Module module;

  private GlobalSearchScope definitionSearchScope;
  private boolean useIndexForFindDefinitions;
  private char[] fqnBuffer;

  private static final Map<String,Set<CharSequence>> BAD_FLEX_CLASSES = new THashMap<String, Set<CharSequence>>();
  
  public interface FlexLibsNames {
    String AIRSPARK = "airspark";
    String SPARK = "spark";
    String FRAMEWORK = "framework";
    String AIRFRAMEWORK = "airframework";
    String MX = "mx";
    String RPC = "rpc";
    String MOBILECOMPONENTS = "mobilecomponents";
  }

  static {
    Set<CharSequence> set;
    for (Pair<String, String> pair : FLEX_LIBS_PATTERNS) {
      if (pair.first.equals(AIRSPARK)) {
        set = createSet(FlexOverloadedClasses.AIR_SPARK_CLASSES.size() + 1);
        set.addAll(FlexOverloadedClasses.AIR_SPARK_CLASSES);
      }
      else {
        set = createSet(1);
      }

      set.add(pair.second);
      BAD_FLEX_CLASSES.put(pair.first, set);
    }
  }

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

  private List<LibrarySetItem> collectItems(final List<Library> libraries, final boolean isFromSdk,
                                            Map<CharSequence, Definition> definitionMap, Condition<String> isExternal) throws IOException {
    final List<LibrarySetItem> unsortedItems = new ArrayList<LibrarySetItem>(libraries.size());
    final CatalogXmlBuilder catalogXmlBuilder = new CatalogXmlBuilder(definitionMap, isExternal);
    for (Library library : libraries) {
      LibrarySetItem filteredLibrary = new LibrarySetItem(library);
      catalogXmlBuilder.setLibrary(filteredLibrary);
      IOUtil.parseXml(library.getCatalogFile(), catalogXmlBuilder);
      if (filteredLibrary.hasDefinitions() || library.hasResourceBundles()) {
        unsortedItems.add(filteredLibrary);
      }
    }

    //if (isFromSdk) {
    //  analyzeDefinitions(definitionMap);
    //}
    //else {
    //  final AccessToken token = ReadAction.start();
    //  try {
    //    analyzeDefinitions(definitionMap);
    //  }
    //  finally {
    //    token.finish();
    //  }
    //}

    return unsortedItems;
  }

  public SortResult sort(final List<Library> libraries, final String flexSdkVersion, final boolean isFromSdk, File outFile,
                         Condition<String> isExternal) throws IOException {
    useIndexForFindDefinitions = !isFromSdk;

    ArrayList<LibrarySetItem> resourceBundleOnlyItems = null;

    final THashMap<CharSequence, Definition> definitionMap = new THashMap<CharSequence, Definition>(libraries.size() * 128, AbcFilter.HASHING_STRATEGY);
    final List<LibrarySetItem> unsortedItems = collectItems(libraries, isFromSdk, definitionMap, isExternal);

    AbcMerger abcMerger = new AbcMerger(definitionMap, flexSdkVersion, outFile);
    final ArrayList<LibrarySetItem> items = new ArrayList<LibrarySetItem>(unsortedItems.size());
    for (LibrarySetItem item : unsortedItems) {
      if (!item.hasDefinitions()) {
        if (item.library.hasResourceBundles()) {
          if (resourceBundleOnlyItems == null) {
            resourceBundleOnlyItems = new ArrayList<LibrarySetItem>();
          }
          resourceBundleOnlyItems.add(item);
        }
        continue;
      }

      items.add(item);
      abcMerger.process(item.library);
    }

    final List<Decoder> decoders = new ArrayList<Decoder>(definitionMap.size());
    definitionMap.forEachValue(new TObjectProcedure<Definition>() {
      @Override
      public boolean execute(Definition definition) {
        if (definition.doAbcData != null && definition.dependencies != null && (definition.unresolvedState == UnresolvedState.NO ||
                                                                                (definition.unresolvedState == UnresolvedState.UNKNOWN &&
                                                                                 processDependencies(decoders, definition, definitionMap)))) {
          // dependecies may be cyclic, see charts.swc from Flex SDK 4.5.1 (mx.charts.chartClasses:DataTransform and mx.charts.chartClasses:IChartElement)
          if (definition.doAbcData != null) {
            decoders.add(new Decoder(definition.doAbcData));
            definition.doAbcData = null;
          }
        }
        return true;
      }
    });

    abcMerger.end(decoders, flexSdkVersion);
    return new SortResult(items, resourceBundleOnlyItems);
  }

  private static THashSet<CharSequence> createSet(int size) {
    return new THashSet<CharSequence>(size, AbcFilter.HASHING_STRATEGY);
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
    definition.unresolvedState = UnresolvedState.NO;

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

      if (dependency == null || dependency.unresolvedState == UnresolvedState.YES ||
          (dependency.unresolvedState == UnresolvedState.UNKNOWN &&
           !processDependencies(decoders, dependency, definitionMap))) {
        definition.markAsUnresolved();
        definition.unresolvedState = UnresolvedState.YES;
        return false;
      }

      if (dependency.doAbcData != null) {
        decoders.add(new Decoder(dependency.doAbcData));
        dependency.doAbcData = null;
      }
    }

    return true;
  }

  public static class SortResult {
    List<LibrarySetItem> items;
    List<LibrarySetItem> resourceBundleOnlyitems;

    private SortResult(List<LibrarySetItem> items, List<LibrarySetItem> resourceBundleOnlyitems) {
      this.items = items;
      this.resourceBundleOnlyitems = resourceBundleOnlyitems;
    }
  }
}