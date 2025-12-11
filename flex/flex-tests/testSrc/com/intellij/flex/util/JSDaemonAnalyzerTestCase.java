// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.util;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.inspections.JSCheckFunctionSignaturesInspection;
import com.intellij.lang.javascript.inspections.JSDeprecatedSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSDuplicatedDeclarationInspection;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.css.inspections.CssUnknownPropertyInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidFunctionInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidHtmlTagReferenceInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidPropertyValueInspection;
import com.intellij.testFramework.*;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Maxim.Mossienko
 */
public abstract class JSDaemonAnalyzerTestCase extends JSDaemonAnalyzerTestCaseBase {
  @NonNls protected final Set<String> ourTestsWithoutDuplicatesCheck = new HashSet<>();
  @NonNls protected final Set<String> ourTestsWithoutDeprecationCheck = new HashSet<>();
  @NonNls protected final Set<String> myTestsWithJSSupportLoader = new HashSet<>();
  @NonNls protected final Set<String> myTestsWithCssLoader = new HashSet<>();

  protected void registerCommonCssInspections() {
    enableInspectionTool(new CssInvalidPropertyValueInspection());
    enableInspectionTool(new CssInvalidFunctionInspection());
    enableInspectionTool(new CssUnknownPropertyInspection());
    enableInspectionTool(new CssInvalidHtmlTagReferenceInspection());
  }

  protected static int countNonInformationHighlights(final Collection<HighlightInfo> infosAfterApplyingAction) {
    int count = 0;
    for (HighlightInfo info : infosAfterApplyingAction) {
      if (info.type != HighlightInfoType.INFORMATION && info.type != HighlightInfoType.INJECTED_LANGUAGE_FRAGMENT && info.type != HighlightInfoType.INJECTED_LANGUAGE_BACKGROUND) {
        count++;
      }
    }
    return count;
  }

  @Override
  protected LocalInspectionTool[] configureLocalInspectionTools() {
    final List<LocalInspectionTool> l = JSDaemonAnalyzerLightTestCase.configureDefaultLocalInspectionTools();

    final String testName = getTestName(false);

    if (!ourTestsWithoutDeprecationCheck.contains(testName)) {
      l.add(new JSDeprecatedSymbolsInspection());
    }
    if (!ourTestsWithoutDuplicatesCheck.contains(testName)) {
      l.add(new JSDuplicatedDeclarationInspection());
    }
    else {
      l.add(new JSDuplicatedDeclarationInspection() {
        @Override
        @NotNull
        public HighlightDisplayLevel getDefaultLevel() {
          return HighlightDisplayLevel.DO_NOT_SHOW;
        }
      });
    }

    return l.toArray(LocalInspectionTool.EMPTY_ARRAY);
  }

  @Override
  protected Collection<HighlightInfo> doTestFor(boolean checkWeakWarnings,
                                                File projectRoot,
                                                Function<? super Collection<HighlightInfo>, Void> action,
                                                String... fileNames) {
    prepareProject(projectRoot, fileNames);

    boolean hasInfos = JSTestUtils.testMethodHasOption(getClass(), getTestName(false), JSTestOption.WithLineMarkers) ||
                       JSTestUtils.testMethodHasOption(getClass(), getTestName(false), JSTestOption.WithInfos);
    final Collection<HighlightInfo> collection =
      doDoTest(
        !JSTestUtils.testMethodHasOption(getClass(), getTestName(false), JSTestOption.WithoutWarnings),
        hasInfos,
        checkWeakWarnings
      );
    if (action != null) action.fun(collection);
    return collection;
  }

  protected void prepareProject(File projectRoot, String[] fileNames) {
    final VirtualFile[] files = new VirtualFile[fileNames != null && fileNames.length != 0 ? fileNames.length : 1];
    if (fileNames == null || fileNames.length == 0) fileNames = new String[] {getTestName(false) + "." + getExtension()};

    for(int i = 0; i < files.length; ++i) {
      files[i] = findVirtualFile(getBasePath() + "/" + fileNames[i]);
    }

    addLibraries(files);
    try {
      configureByFiles(projectRoot, files);
    } catch (IOException e) {
      addSuppressedException(e);
    }
    JSTestUtils.buildJSFileGists(getProject());
  }

  @Override
  @Unmodifiable
  protected Collection<HighlightInfo> doDoTest(boolean checkWarnings, boolean checkInfos, boolean checkWeakWarnings) {
    final boolean checkSemanticKeywords =
      JSTestUtils.testMethodHasOption(getClass(), getTestName(false), JSTestOption.WithSymbolNames);
    ExpectedHighlightingData data =
      new ExpectedHighlightingData(myEditor.getDocument(), checkWarnings, checkWeakWarnings, checkInfos, false);
    if (checkSemanticKeywords) {
      data.checkSymbolNames();
    }
    Collection<HighlightInfo> highlightInfos = checkHighlighting(data);
    return ContainerUtil.filter(
      highlightInfos,
      info -> info.getSeverity() == HighlightSeverity.INFORMATION && checkInfos ||
              info.getSeverity() == HighlightInfoType.SYMBOL_TYPE_SEVERITY && checkSemanticKeywords ||
              info.getSeverity() == HighlightSeverity.WARNING && checkWarnings ||
              info.getSeverity() == HighlightSeverity.WEAK_WARNING && checkWeakWarnings ||
              info.getSeverity().compareTo(HighlightSeverity.WARNING) > 0);
  }

  protected void addLibraries(VirtualFile[] files) {
  }

  @Override
  protected boolean doTestLineMarkers() {
    return JSTestUtils.testMethodHasOption(getClass(), getTestName(false), JSTestOption.WithLineMarkers);
  }

  @Override
  protected boolean isAddDirToSource() {
    return !JSTestUtils.testMethodHasOption(getClass(), getTestName(false), JSTestOption.WithoutSourceRoot);
  }

  protected void invokeNamedActionWithExpectedFileCheck(final String testName, @Language("devkit-action-id") @NonNls final String actionId, @NonNls String ext) throws Exception {
    PlatformTestUtil.invokeNamedAction(actionId);

    checkResultByFile(getBasePath() + "/" + testName + "_after." + ext);
  }

  protected void doGenerateTest(@Language("devkit-action-id") @NonNls final String actionId, @NonNls String ext) throws Exception {
    doGenerateTest(actionId, "", ext);
  }

  protected void doGenerateTest(@Language("devkit-action-id") @NonNls final String actionId, final String suffix, @NonNls final String ext) throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + suffix + "." + ext);
    PlatformTestUtil.invokeNamedAction(actionId);
    checkResultByFile(getBasePath() + "/" + testName + suffix + "_after." + ext);
  }

  @Override
  public Object getData(@NotNull final String dataId) {
    if (OpenFileDescriptor.NAVIGATE_IN_EDITOR.is(dataId)) {
      return myEditor;
    }
    else if (CommonDataKeys.PSI_FILE.is(dataId)) {
      return myFile;
    }
    else if (CommonDataKeys.VIRTUAL_FILE.is(dataId) && myFile != null) {
      return myFile.getVirtualFile();
    }
    return super.getData(dataId);
  }

  public static List<HighlightInfo> filterUnwantedInfos(List<HighlightInfo> infoList, UsefulTestCase test) {
    boolean checkWarnings = !JSTestUtils.testMethodHasOption(test.getClass(), UsefulTestCase.getTestName(test.getName(), false), JSTestOption.WithoutWarnings);
    boolean checkInfos = JSTestUtils.testMethodHasOption(test.getClass(), UsefulTestCase.getTestName(test.getName(), false), JSTestOption.WithLineMarkers);
    return filterUnwantedInfos(infoList, checkWarnings, checkInfos, true);
  }

  public static List<HighlightInfo> filterUnwantedInfos(List<HighlightInfo> infoList, final boolean checkWarnings, final boolean checkInfos, final boolean checkWeakWarnings) {
    Iterator<HighlightInfo> iter = infoList.iterator();
    while (iter.hasNext()) {
      HighlightInfo highlightInfo = iter.next();
      boolean severityFits = highlightInfo.getSeverity() == HighlightSeverity.WEAK_WARNING && checkWeakWarnings ||
                             highlightInfo.getSeverity() == HighlightSeverity.INFORMATION && checkInfos ||
                             highlightInfo.getSeverity() == HighlightSeverity.WARNING && checkWarnings ||
                             highlightInfo.getSeverity().compareTo(HighlightSeverity.WARNING) > 0;
      if (!severityFits ||
          highlightInfo.type == HighlightInfoType.INJECTED_LANGUAGE_FRAGMENT  && checkInfos ||
          highlightInfo.type == HighlightInfoType.TODO && checkInfos
        ) {
        iter.remove();
      }
    }

    return infoList;
  }

  protected void defaultTestWithTyping(char c) throws IOException {
    defaultTest();
    EditorTestUtil.performTypingAction(myEditor, c);
    checkAfterHighlighting();
  }

  protected void checkAfterHighlighting() throws IOException {
    checkHighlightingByFile(getTestName(false) + "_after." + getExtension());
  }

  protected void checkHighlightingByFile(String fileName) throws IOException {
    final String text = VfsUtilCore.loadText(findVirtualFile(getBasePath() + "/" + fileName));
    final DocumentImpl document = new DocumentImpl(StreamUtil.convertSeparators(text));
    final ExpectedHighlightingData data = new ExpectedHighlightingData(document, true, true, false);
    checkHighlighting(data);
  }

  protected void enableCheckGuessedTypes() {
    JSCheckFunctionSignaturesInspection inspection = new JSCheckFunctionSignaturesInspection();
    inspection.myCheckGuessedTypes = true;
    InspectionsKt.enableInspectionTool(getProject(), inspection, getTestRootDisposable());
  }
}
