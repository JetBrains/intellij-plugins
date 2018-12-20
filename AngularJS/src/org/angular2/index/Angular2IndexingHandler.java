// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.ecmascript6.psi.ES6FromClause;
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration;
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElementStructure;
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import one.util.streamex.StreamEx;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularSymbolIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static java.util.Collections.emptyList;
import static org.angular2.Angular2DecoratorUtil.*;

public class Angular2IndexingHandler extends FrameworkIndexingHandler {
  public static final String TEMPLATE_URL = "templateUrl";
  public static final String TEMPLATE = "template";

  public static final String STYLE_URLS = "styleUrls";
  public static final String STYLES = "styles";

  private static final String ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING = "a2tui";
  private static final String ANGULAR2_PIPE_INDEX_USER_STRING = "a2pi";
  private static final String ANGULAR2_DIRECTIVE_INDEX_USER_STRING = "a2di";

  private static final String PIPE_TYPE = "P;;;";
  private static final String DIRECTIVE_TYPE = "D;;;";

  private static final String STYLESHEET_INDEX_PREFIX = "ss/";

  private final static Map<String, StubIndexKey<String, JSImplicitElementProvider>> INDEX_MAP = new HashMap<>();

  static {
    INDEX_MAP.put(ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING, Angular2TemplateUrlIndex.KEY);
    INDEX_MAP.put(ANGULAR2_DIRECTIVE_INDEX_USER_STRING, Angular2SourceDirectiveIndex.KEY);
    INDEX_MAP.put(ANGULAR2_PIPE_INDEX_USER_STRING, Angular2SourcePipeIndex.KEY);
    for (String key : INDEX_MAP.keySet()) {
      JSImplicitElement.ourUserStringsRegistry.registerUserString(key);
    }
  }

  public static boolean isPipe(@NotNull JSImplicitElement element) {
    return PIPE_TYPE.equals(element.getTypeString());
  }

  public static boolean isDirective(@NotNull JSImplicitElement element) {
    String type = element.getTypeString();
    if (type == null) {
      return false;
    }
    return type.startsWith(DIRECTIVE_TYPE);
  }

  @Override
  public int getVersion() {
    return Angular2IndexBase.VERSION;
  }

  @Nullable
  @Override
  public JSElementIndexingDataImpl processDecorator(@NotNull ES6Decorator decorator, @Nullable JSElementIndexingDataImpl data) {
    TypeScriptClass enclosingClass = PsiTreeUtil.getContextOfType(decorator, TypeScriptClass.class);
    if (enclosingClass != null) {
      String decoratorName = decorator.getDecoratorName();
      boolean isComponent = false;
      if (PIPE_DEC.equals(decoratorName)) {
        if (data == null) {
          data = new JSElementIndexingDataImpl();
        }
        addPipe(enclosingClass, data::addImplicitElement, getPropertyValue(decorator, NAME_PROP));
      }
      else if (DIRECTIVE_DEC.equals(decoratorName)
               || (isComponent = COMPONENT_DEC.equals(decoratorName))) {
        if (data == null) {
          data = new JSElementIndexingDataImpl();
        }
        addDirective(enclosingClass, data::addImplicitElement, getPropertyValue(decorator, SELECTOR_PROP));
        if (isComponent) {
          addComponentExternalFilesRefs(decorator, "", data::addImplicitElement,
                                        ContainerUtil.packNullables(getTemplateFileUrl(decorator)));
          addComponentExternalFilesRefs(decorator, STYLESHEET_INDEX_PREFIX, data::addImplicitElement,
                                        getStylesUrls(decorator));
        }
      }
    }
    return data;
  }

  private static void addDirective(@NotNull TypeScriptClass directiveClass,
                                   @NotNull Consumer<JSImplicitElement> processor,
                                   @Nullable String selector) {
    if (StringUtil.isEmpty(selector)) {
      selector = "";
    }
    Set<String> indexNames = Angular2EntityUtils.getDirectiveIndexNames(selector);
    JSImplicitElement directive = new JSImplicitElementImpl
      .Builder(ObjectUtils.notNull(directiveClass.getName(), selector), directiveClass)
      .setType(JSImplicitElement.Type.Class)
      .setTypeString(DIRECTIVE_TYPE + StringUtil.join(indexNames, "/"))
      .setUserString(ANGULAR2_DIRECTIVE_INDEX_USER_STRING)
      .toImplicitElement();
    processor.consume(directive);
  }

  @Override
  public boolean indexImplicitElement(@NotNull JSImplicitElementStructure element, @Nullable IndexSink sink) {
    if (sink == null) {
      return false;
    }
    final String userID = element.getUserString();
    final StubIndexKey<String, JSImplicitElementProvider> index = userID != null ? INDEX_MAP.get(userID) : null;
    if (index == Angular2SourceDirectiveIndex.KEY) {
      String type = element.toImplicitElement(null).getTypeString();
      if (type != null && type.startsWith(DIRECTIVE_TYPE)) {
        type = type.substring(DIRECTIVE_TYPE.length());
        StringUtil.split(type, "/")
          .forEach(name -> sink.occurrence(index, name));
      }
      return true;
    }
    else if (index != null) {
      sink.occurrence(index, element.getName());
      if (index == Angular2SourcePipeIndex.KEY) {
        sink.occurrence(AngularSymbolIndex.KEY, element.getName());
      }
      else {
        return true;
      }
    }
    return false;
  }

  private static void addComponentExternalFilesRefs(@NotNull ES6Decorator decorator,
                                                    @NotNull String namePrefix,
                                                    @NotNull Consumer<JSImplicitElement> processor,
                                                    @NotNull List<String> fileUrls) {
    for (String fileUrl : fileUrls) {
      int lastSlash = fileUrl.lastIndexOf('/');
      String name = fileUrl.substring(lastSlash + 1);
      //don't index if file name matches TS file name and is in the same directory
      if ((lastSlash <= 0 || (lastSlash == 1 && fileUrl.charAt(0) == '.'))
          && FileUtil.getNameWithoutExtension(name)
            .equals(FileUtil.getNameWithoutExtension(decorator.getContainingFile().getOriginalFile().getName()))) {
        continue;
      }
      JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(namePrefix + name, decorator)
        .setUserString(ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING);
      processor.consume(elementBuilder.toImplicitElement());
    }
  }

  private static void addPipe(@NotNull TypeScriptClass pipeClass,
                              @NotNull Consumer<JSImplicitElement> processor,
                              String pipe) {
    if (pipe == null) return;
    JSImplicitElementImpl pipeElement = new JSImplicitElementImpl.Builder(pipe, pipeClass)
      .setUserString(ANGULAR2_PIPE_INDEX_USER_STRING)
      .setTypeString(PIPE_TYPE)
      .setType(JSImplicitElement.Type.Class)
      .toImplicitElement();
    processor.consume(pipeElement);
  }

  @Nullable
  public static TypeScriptClass findComponentClass(@NotNull PsiElement context) {
    return ContainerUtil.getFirstItem(findComponentClasses(context));
  }

  @NotNull
  public static List<TypeScriptClass> findComponentClasses(@NotNull PsiElement context) {
    final PsiFile file = context.getContainingFile();
    if (file == null
        || !(file.getLanguage().is(Angular2HtmlLanguage.INSTANCE)
             || file.getLanguage().is(Angular2Language.INSTANCE)
             || isStylesheet(file))) {
      return emptyList();
    }
    PsiFile hostFile = getHostFile(context);
    if (hostFile == null) {
      return emptyList();
    }
    if (!file.getOriginalFile().equals(hostFile) && DialectDetector.isTypeScript(hostFile)) {
      // inline content
      return ContainerUtil.packNullables(PsiTreeUtil.getContextOfType(
        InjectedLanguageManager.getInstance(context.getProject()).getInjectionHost(file),
        TypeScriptClass.class));
    }
    // external content
    List<TypeScriptClass> result = new SmartList<>(
      resolveComponentsFromSimilarFile(hostFile));
    if (!result.isEmpty() && !isStylesheet(file)) {
      return result;
    }
    result.addAll(resolveComponentsFromIndex(hostFile));
    return result;
  }

  @NotNull
  private static List<TypeScriptClass> resolveComponentsFromSimilarFile(@NotNull PsiFile file) {
    final String name = file.getViewProvider().getVirtualFile().getNameWithoutExtension();
    final PsiDirectory dir = file.getParent();
    final PsiFile directiveFile = dir != null ? dir.findFile(name + ".ts") : null;
    if (directiveFile != null) {
      return StreamEx.of(JSStubBasedPsiTreeUtil.getFileOrModuleChildrenStream(directiveFile))
        .select(TypeScriptClass.class)
        .filter(cls -> {
          ES6Decorator dec = findDecorator(cls, COMPONENT_DEC);
          return hasFileReference(dec, file);
        })
        .toList();
    }
    return emptyList();
  }

  @NotNull
  private static List<TypeScriptClass> resolveComponentsFromIndex(@NotNull PsiFile file) {
    final String name = (isStylesheet(file) ? STYLESHEET_INDEX_PREFIX : "") + file.getViewProvider().getVirtualFile().getName();
    final List<TypeScriptClass> result = new SmartList<>();
    AngularIndexUtil.multiResolve(file.getProject(), Angular2TemplateUrlIndex.KEY, name, el -> {
      if (el != null) {
        PsiElement componentDecorator = el.getParent();
        if (componentDecorator instanceof ES6Decorator
            && hasFileReference((ES6Decorator)componentDecorator, file)) {
          ContainerUtil.addIfNotNull(result, PsiTreeUtil.getContextOfType(componentDecorator, TypeScriptClass.class));
        }
      }
      return true;
    });
    return result;
  }

  private static boolean hasFileReference(@Nullable ES6Decorator componentDecorator, @NotNull PsiFile file) {
    Angular2Component component = Angular2EntitiesProvider.getComponent(componentDecorator);
    if (component != null) {
      return isStylesheet(file) ? component.getCssFiles().contains(file) : file.equals(component.getTemplateFile());
    }
    return false;
  }

  private static boolean isStylesheet(@NotNull PsiFile file) {
    return file instanceof StylesheetFile;
  }

  @Nullable
  private static String getTemplateFileUrl(@NotNull ES6Decorator decorator) {
    String templateUrl = getPropertyValue(decorator, TEMPLATE_URL);
    if (templateUrl != null) {
      return templateUrl;
    }
    JSProperty property = getProperty(decorator, TEMPLATE);
    if (property != null) {
      return getExprReferencedFileUrl(property.getValue());
    }
    return null;
  }

  @NotNull
  private static List<String> getStylesUrls(@NotNull ES6Decorator decorator) {
    List<String> result = new SmartList<>();

    BiConsumer<String, Function<JSExpression, String>> urlsGetts = (name, func) ->
      StreamEx.of(name)
        .map(prop -> getProperty(decorator, prop))
        .nonNull()
        .map(JSProperty::getValue)
        .select(JSArrayLiteralExpression.class)
        .flatArray(JSArrayLiteralExpression::getExpressions)
        .map(func)
        .nonNull()
        .into(result);

    urlsGetts.accept(STYLE_URLS, Angular2DecoratorUtil::getExpressionStringValue);
    urlsGetts.accept(STYLES, Angular2IndexingHandler::getExprReferencedFileUrl);

    return result;
  }

  @Nullable
  private static String getExprReferencedFileUrl(@Nullable JSExpression expression) {
    if (expression instanceof JSReferenceExpression) {
      for (PsiElement resolvedElement : AngularIndexUtil.resolveLocally((JSReferenceExpression)expression)) {
        if (resolvedElement instanceof ES6ImportedBinding) {
          ES6FromClause from = doIfNotNull(((ES6ImportedBinding)resolvedElement).getDeclaration(),
                                           ES6ImportDeclaration::getFromClause);
          if (from != null) {
            return doIfNotNull(from.getReferenceText(), StringUtil::unquoteString);
          }
        }
      }
    }
    return null;
  }

  @Nullable
  private static PsiFile getHostFile(@NotNull PsiElement context) {
    final PsiElement original = CompletionUtil.getOriginalOrSelf(context);
    PsiFile hostFile = FileContextUtil.getContextFile(original != context ? original : context.getContainingFile().getOriginalFile());
    return hostFile != null ? hostFile.getOriginalFile() : null;
  }
}
