// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.DataInputOutputUtilRt;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.PsiDependentFileContent;
import com.intellij.util.io.BooleanDataDescriptor;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.text.StringSearcher;
import org.jetbrains.annotations.NotNullByDefault;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NotNullByDefault
public abstract class CucumberStepIndex extends FileBasedIndexExtension<Boolean, List<Integer>> {

  //@formatter:off Temporarily disable formatter because of bug IDEA-371809
  /// Regenerate it with:
  ///
  /// ```
  /// cat ./contrib/cucumber/resources/gherkin-languages.json \
  ///   | jq '(map_values(([.and, .but, .given, .then, .when] | flatten | [.[] | select(. != "* ") | gsub("\\s+|'|,\''"; "")])))' \
  ///   > ./contrib/cucumber/resources/step-keywords.json
  /// ```
  ///
  /// The keywords are generated with spaces and punctuation removed for reasons described in IDEA-295155.
  /// 
  /// @see org.jetbrains.plugins.cucumber.psi.i18n.JsonGherkinKeywordProvider JsonGherkinKeywordProvider
  //@formatter:on
  private static final List<String> STEP_KEYWORDS = loadStepKeywords();

  private static List<String> loadStepKeywords() {
    ClassLoader classLoader = CucumberStepIndex.class.getClassLoader();
    if (classLoader == null) throw new IllegalStateException("ClassLoader not available");

    String filename = "step-keywords.json";
    InputStream stream = classLoader.getResourceAsStream(filename);
    if (stream == null) throw new IllegalStateException("Could not load " + filename);

    try (Reader in = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      var typeOfMap = new TypeToken<Map<String, List<String>>>() {
      };
      Map<String, List<String>> fromJson = new Gson().fromJson(in, typeOfMap.getType());
      if (fromJson == null) throw new IllegalStateException("Failed to parse step keywords JSON");

      return fromJson.values().stream().flatMap(List::stream).toList();
    }
    catch (Exception e) {
      Logger.getInstance(CucumberStepIndex.class.getName()).error(e);
      throw new IllegalStateException("Failed to read step keywords", e);
    }
  }

  @Override
  public DataIndexer<Boolean, List<Integer>, FileContent> getIndexer() {
    return inputData -> {
      CharSequence text = inputData.getContentAsText();
      if (!hasCucumberImport(text)) {
        return Collections.emptyMap();
      }

      LighterAST lighterAst = ((PsiDependentFileContent)inputData).getLighterAST();
      List<Integer> result = getStepDefinitionOffsets(lighterAst, text);
      Map<Boolean, List<Integer>> resultMap = new HashMap<>();
      resultMap.put(true, result);
      return resultMap;
    };
  }

  @Override
  public KeyDescriptor<Boolean> getKeyDescriptor() {
    return BooleanDataDescriptor.INSTANCE;
  }

  @Override
  public DataExternalizer<List<Integer>> getValueExternalizer() {
    return DATA_EXTERNALIZER;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  protected abstract String[] getPackagesToScan();

  private boolean hasCucumberImport(CharSequence text) {
    for (String pkg : getPackagesToScan()) {
      StringSearcher searcher = new StringSearcher(pkg, true, true);
      if (searcher.scan(text) > 0) {
        return true;
      }
    }
    return false;
  }

  protected static boolean isStepDefinitionCall(LighterASTNode methodName, CharSequence text) {
    String actualMethodName = text.subSequence(methodName.getStartOffset(), methodName.getEndOffset()).toString();
    return STEP_KEYWORDS.contains(actualMethodName);
  }

  protected static boolean isStringLiteral(LighterASTNode element, CharSequence text) {
    return text.charAt(element.getStartOffset()) == '"';
  }

  protected static boolean isNumber(LighterASTNode element, CharSequence text) {
    for (int i = element.getStartOffset(); i < element.getEndOffset(); i++) {
      if (!Character.isDigit(text.charAt(i))) {
        return false;
      }
    }

    return element.getTextLength() > 0;
  }

  protected abstract List<Integer> getStepDefinitionOffsets(LighterAST lighterAst, CharSequence text);

  private static final DataExternalizer<List<Integer>> DATA_EXTERNALIZER = new DataExternalizer<>() {
    @Override
    public void save(DataOutput out, List<Integer> value) throws IOException {
      DataInputOutputUtilRt.writeSeq(out, value, descriptor -> {
        DataInputOutputUtilRt.writeINT(out, descriptor.intValue());
      });
    }

    @Override
    public List<Integer> read(DataInput in) throws IOException {
      return DataInputOutputUtilRt.readSeq(in, () -> DataInputOutputUtilRt.readINT(in));
    }
  };
}
