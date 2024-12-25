package com.intellij.javascript.bower;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.impl.LiveTemplateWeigher;
import com.intellij.codeInsight.lookup.*;
import com.intellij.execution.ExecutionException;
import com.intellij.javascript.bower.browsePackages.BowerPackageSearcher;
import com.intellij.json.codeinsight.JsonStringPropertyInsertHandler;
import com.intellij.json.psi.*;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.CollectionFactory;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class BowerCompletionContributor extends CompletionContributor {

  private static final Logger LOG = Logger.getInstance(BowerCompletionContributor.class);
  private static final List<String> DEPENDENCIES_NAMES = Arrays.asList("dependencies", "devDependencies");

  private static final Map<String, List<String>> ourVersionCache = CollectionFactory.createConcurrentSoftMap();

  @Override
  public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    JsonFile packageJson = BowerPackageUtil.getContainingBowerJsonFile(parameters.getOriginalFile());
    if (packageJson != null) {
      completeDependencies(parameters, result);
    }
  }

  private static @Nullable JsonProperty findEnclosingDependenciesProperty(@NotNull PsiElement element) {
    JsonObject obj = PsiTreeUtil.getParentOfType(element, JsonObject.class, false);
    if (obj != null) {
      JsonProperty dependenciesProperty = ObjectUtils.tryCast(obj.getParent(), JsonProperty.class);
      if (dependenciesProperty != null
          && DEPENDENCIES_NAMES.contains(dependenciesProperty.getName())
          && PackageJsonUtil.isTopLevelProperty(dependenciesProperty)) {
        return dependenciesProperty;
      }
    }
    return null;
  }

  private static void completeDependencies(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    PsiElement element = ObjectUtils.notNull(parameters.getOriginalPosition(), parameters.getPosition());
    JsonProperty dependenciesProperty = findEnclosingDependenciesProperty(element);
    if (dependenciesProperty != null) {
      CompletionSorter sorter = CompletionSorter.emptySorter()
        .weigh(new LiveTemplateWeigher())
        .weigh(new LookupElementWeigher("priority") {
          @Override
          public @NotNull Double weigh(@NotNull LookupElement element) {
            PrioritizedLookupElement<?> ple = ObjectUtils.tryCast(element, PrioritizedLookupElement.class);
            return ple != null ? ple.getPriority() : 0.0;
          }
        });
      result = result.withRelevanceSorter(sorter);

      JsonProperty parentProperty = PsiTreeUtil.getParentOfType(element, JsonProperty.class, false);
      if (parentProperty == dependenciesProperty) {
        parentProperty = PsiTreeUtil.getParentOfType(parameters.getPosition(), JsonProperty.class);
      }

      if (parentProperty != null) {
        JsonValue nameElement = parentProperty.getNameElement();
        if (nameElement.getTextRange().containsOffset(parameters.getOffset())) {
          int prefixLength = parameters.getOffset() - nameElement.getTextRange().getStartOffset();
          String prefix = StringUtil.unquoteString(nameElement.getText().substring(0, prefixLength));
          prefix = StringUtil.trimStart(prefix, "\"");
          completeDependenciesNames(parameters, result.withPrefixMatcher(prefix), prefix, parentProperty);
        }
        else {
          result = result.withPrefixMatcher(new PlainPrefixMatcher(result.getPrefixMatcher().getPrefix()));
          completeDependenciesVersions(parameters, result, parentProperty);
        }
      }
    }
  }

  private static void completeDependenciesNames(@NotNull CompletionParameters parameters,
                                                @NotNull CompletionResultSet result,
                                                @NotNull String packageNamePrefix,
                                                @NotNull JsonProperty property) {
    Set<String> alreadyUsedNames = JsonPsiUtil.getOtherSiblingPropertyNames(property);

    BowerPackageSearcher searcher = BowerPackageSearcher.getInstance();
    List<BowerPackageBasicInfo> popularPackages = searcher.findPopularPackagesByPrefix(packageNamePrefix, 10,
      info -> {
        //noinspection CodeBlock2Expr
        return !alreadyUsedNames.contains(info.getName());
      });
    AtomicInteger priority = new AtomicInteger(0);
    for (BowerPackageBasicInfo pkg : popularPackages) {
      addPackageInfo(parameters, result, pkg, priority.getAndIncrement());
    }
    Set<String> popularPackageNames = ContainerUtil.map2Set(popularPackages, BowerPackageBasicInfo::getName);
    try {
      //noinspection CodeBlock2Expr
      searcher.findPackagesByPrefix(
        ProgressManager.getInstance().getProgressIndicator(),
        packageNamePrefix,
        10,
        info -> {
          //noinspection CodeBlock2Expr
          return !alreadyUsedNames.contains(info.getName()) && !popularPackageNames.contains(info.getName());
        },
        info -> {
          addPackageInfo(parameters, result, info, priority.getAndIncrement());
        }
      );
    }
    catch (ProcessCanceledException e) {
      LOG.info("Fetching '" + packageNamePrefix + "*' packages from registry cancelled");
    }
    catch (IOException e) {
      LOG.info("Cannot fetch '" + packageNamePrefix + "*' packages from registry", e);
    }
    result.restartCompletionOnAnyPrefixChange();
  }

  private static <T> T runWithoutReadAction(@Nullable ProgressIndicator indicator, @NotNull Callable<? extends T> callable) throws Exception {
    if (ApplicationManager.getApplication().isReadAccessAllowed()) {
      return ApplicationUtil.runWithCheckCanceled(callable, EmptyProgressIndicator.notNullize(indicator));
    }
    return callable.call();
  }

  private static void addPackageInfo(@NotNull CompletionParameters parameters,
                                     @NotNull CompletionResultSet result,
                                     @NotNull BowerPackageBasicInfo info,
                                     int priority) {
    // Add a trailing space to ensure that exactly matched packages don't have highest priority.
    // See CompletionLookupArranger.addPrefixItems()
    String lookupString = info.getName() + " ";
    if (!isInsideStringLiteral(parameters)) {
      lookupString = StringUtil.wrapWithDoubleQuote(lookupString);
    }
    LookupElementBuilder builder = LookupElementBuilder.create(info, lookupString);
    builder = builder.withRenderer(new LookupElementRenderer<>() {
      @Override
      public void renderElement(LookupElement element, LookupElementPresentation presentation) {
        presentation.setItemText(info.getName());
        presentation.setTypeGrayed(true);
        presentation.setTypeText(info.getDescription());
      }
    }).withInsertHandler(new JsonStringPropertyInsertHandler(info.getName()));
    result.addElement(PrioritizedLookupElement.withPriority(
      builder.withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE),
      priority));
  }

  private static boolean isInsideStringLiteral(@NotNull CompletionParameters parameters) {
    return parameters.getPosition().getParent() instanceof JsonStringLiteral;
  }

  private static void completeDependenciesVersions(@NotNull CompletionParameters parameters,
                                                   @NotNull CompletionResultSet result,
                                                   @NotNull JsonProperty packageProperty) {
    String packageName = StringUtil.unquoteString(packageProperty.getNameElement().getText());
    List<String> versions;
    try {
      BowerSettings settings = BowerSettingsManager.getInstance(packageProperty.getProject()).getSettings();
      versions = Objects.requireNonNull(runWithoutReadAction(ProgressManager.getInstance().getProgressIndicator(), () -> {
        return getOrFetchVersions(packageName, settings);
      }));
    }
    catch (ProcessCanceledException e) {
      throw e;
    }
    catch (Exception e) {
      if (ApplicationManager.getApplication().isUnitTestMode()) {
        throw new RuntimeException(e);
      }
      LOG.info("Cannot get versions of '" + packageName + "'", e);
      return;
    }
    int order = 0;
    for (String version : versions) {
      addVersionCompletionItem(parameters, result, version, order++);
    }
  }

  private static @NotNull List<String> getOrFetchVersions(@NotNull String packageName, @NotNull BowerSettings settings) {
    List<String> versions = ourVersionCache.get(packageName);
    if (versions != null) {
      return versions;
    }
    try {
      BowerPackageInfo info = BowerPackageUtil.loadPackageInfo(ProgressManager.getInstance().getProgressIndicator(),
                                                               settings, packageName);
      versions = info.getVersions();
      ourVersionCache.put(packageName, versions);
      return versions;
    }
    catch (ExecutionException e) {
      LOG.info("Cannot load versions for " + packageName + ": " + e.getMessage());
      return Collections.emptyList();
    }
  }

  private static void addVersionCompletionItem(@NotNull CompletionParameters parameters,
                                               @NotNull CompletionResultSet result,
                                               @NotNull String version,
                                               int order) {
    String value = getVersion(parameters, version);
    String lookupString = isInsideStringLiteral(parameters) ? value
                                                            : StringUtil.wrapWithDoubleQuote(value);
    LookupElementBuilder builder = LookupElementBuilder.create(version, lookupString);
    builder = builder.withRenderer(new LookupElementRenderer<>() {
      @Override
      public void renderElement(LookupElement element, LookupElementPresentation presentation) {
        presentation.setItemText(value);
        presentation.setTypeGrayed(true);
      }
    }).withInsertHandler(new JsonStringPropertyInsertHandler(value));
    result.addElement(PrioritizedLookupElement.withPriority(
      builder.withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE),
      order));
  }

  private static @NotNull String getVersion(@NotNull CompletionParameters parameters, @NotNull String version) {
    PsiElement position = parameters.getOriginalPosition();
    if (position != null) {
      String text = StringUtil.unquoteString(position.getText());
      if (text.startsWith("~") || text.startsWith("^")) {
        return text.charAt(0) + version;
      }
    }
    return version;
  }
}
