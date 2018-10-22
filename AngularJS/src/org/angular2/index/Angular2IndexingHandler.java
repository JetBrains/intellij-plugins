// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElementStructure;
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import org.angular2.codeInsight.attributes.Angular2EventHandlerDescriptor;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angularjs.codeInsight.AngularJSProcessor;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularSymbolIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

import static org.angular2.Angular2DecoratorUtil.*;
import static org.angularjs.index.AngularIndexUtil.hasFileReference;

public class Angular2IndexingHandler extends FrameworkIndexingHandler {
  public static final String TEMPLATE_URL = "templateUrl";

  private static final String ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING = "a2tui";
  private static final String ANGULAR2_PIPE_INDEX_USER_STRING = "a2pi";
  private static final String ANGULAR2_DIRECTIVE_INDEX_USER_STRING = "a2di";

  private static final String PIPE_TYPE = "P;;;";
  private static final String COMPONENT_TYPE = "C;;;";
  private static final String DIRECTIVE_TYPE = "D;;;";
  private static final String TEMPLATE_DIRECTIVE_TYPE = "T;;;";
  public static final String TEMPLATE_REF = "TemplateRef";

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
    return type.startsWith(COMPONENT_TYPE)
           || type.startsWith(DIRECTIVE_TYPE)
           || type.startsWith(TEMPLATE_DIRECTIVE_TYPE);
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
        addPipe(enclosingClass, data::addImplicitElement, getPropertyName(decorator, NAME_PROP));
      }
      else if (DIRECTIVE_DEC.equals(decoratorName)
               || (isComponent = COMPONENT_DEC.equals(decoratorName))) {
        if (data == null) {
          data = new JSElementIndexingDataImpl();
        }
        addDirective(enclosingClass, isComponent, data::addImplicitElement, getPropertyName(decorator, SELECTOR_PROP));
        if (isComponent) {
          addComponentTemplateRef(decorator, data::addImplicitElement, getPropertyName(decorator, TEMPLATE_URL));
        }
      }
    }
    return data;
  }

  private static void addDirective(@NotNull TypeScriptClass directiveClass,
                                   boolean isComponent,
                                   @NotNull Consumer<JSImplicitElement> processor,
                                   @Nullable String selector) {
    if (StringUtil.isEmpty(selector)) {
      return;
    }
    boolean isTemplate = !isComponent && isTemplate(directiveClass);
    Set<String> indexNames = Angular2EntityUtils.getDirectiveIndexNames(directiveClass.getProject(), selector, isTemplate);
    JSImplicitElement directive = new JSImplicitElementImpl
      .Builder(ObjectUtils.notNull(directiveClass.getName(), selector), directiveClass)
      .setType(JSImplicitElement.Type.Class)
      .setTypeString((isComponent ? COMPONENT_TYPE : isTemplate ? TEMPLATE_DIRECTIVE_TYPE : DIRECTIVE_TYPE) +
                     StringUtil.join(indexNames, "/"))
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
      if (type != null && (type.startsWith(DIRECTIVE_TYPE)
                           || type.startsWith(COMPONENT_TYPE)
                           || type.startsWith(TEMPLATE_DIRECTIVE_TYPE))) {
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

  @Override
  public boolean addTypeFromResolveResult(@NotNull JSTypeEvaluator evaluator,
                                          @NotNull JSEvaluateContext context, @NotNull PsiElement result) {
    if (!(result instanceof JSImplicitElement) || !AngularJSProcessor.$EVENT.equals(((JSImplicitElement)result).getName())) {
      return false;
    }
    XmlAttribute parent = ObjectUtils.tryCast(result.getParent(), XmlAttribute.class);
    Angular2EventHandlerDescriptor descriptor = ObjectUtils.tryCast(parent != null ? parent.getDescriptor() : null,
                                                                    Angular2EventHandlerDescriptor.class);
    PsiElement declaration = descriptor != null ? descriptor.getDeclaration() : null;
    JSType type = null;
    if (declaration instanceof JSField) {
      type = ((JSField)declaration).getType();
    }
    else if (declaration instanceof JSFunction) {
      type = ((JSFunction)declaration).getReturnType();
    }
    type = JSTypeUtils.getValuableType(type);
    if (type instanceof JSGenericTypeImpl) {
      List<JSType> arguments = ((JSGenericTypeImpl)type).getArguments();
      if (arguments.size() == 1) {
        evaluator.addType(arguments.get(0), declaration);
        return true;
      }
    }
    return false;
  }

  public static boolean isTemplate(@NotNull TypeScriptClass clazz) {
    return Stream.of(clazz.getConstructors())
      .map(JSFunction::getParameterList)
      .filter(Objects::nonNull)
      .map(JSParameterList::getParameters)
      .flatMap(Stream::of)
      .map(JSParameterListElement::getType)
      .filter(Objects::nonNull)
      .map(type -> type.getTypeText())
      .anyMatch(t -> t.contains(TEMPLATE_REF));
  }

  private static void addComponentTemplateRef(@NotNull ES6Decorator decorator,
                                              @NotNull Consumer<JSImplicitElement> processor,
                                              @Nullable String templateUrl) {
    if (templateUrl == null) return;
    int lastSlash = templateUrl.lastIndexOf('/');
    String name = templateUrl.substring(lastSlash + 1);
    //don't index if HTML file name matches TS file name and is in the same directory
    if ((lastSlash <= 0 || (lastSlash == 1 && templateUrl.charAt(0) == '.'))
        && name.equals(FileUtil.getNameWithoutExtension(decorator.getContainingFile().getOriginalFile().getName()) + ".html")) {
      return;
    }
    JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(name, decorator)
      .setUserString(ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING);
    processor.consume(elementBuilder.toImplicitElement());
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
    final PsiFile file = context.getContainingFile();
    if (file == null
        || !(file.getLanguage().is(Angular2HtmlLanguage.INSTANCE)
             || file.getLanguage().is(Angular2Language.INSTANCE))) {
      return null;
    }
    PsiFile hostFile = getHostFile(context);
    if (hostFile == null) {
      return null;
    }
    if (!file.getOriginalFile().equals(hostFile)) {
      // inline template
      return PsiTreeUtil
        .getContextOfType(InjectedLanguageManager.getInstance(context.getProject()).getInjectionHost(file), TypeScriptClass.class);
    }
    // non inline template file
    TypeScriptClass result = resolveComponentWithSameName(hostFile);
    if (result != null) {
      return result;
    }
    return resolveComponentFromIndex(hostFile);
  }

  @Nullable
  private static TypeScriptClass resolveComponentWithSameName(@NotNull PsiFile templateFile) {
    final String name = templateFile.getViewProvider().getVirtualFile().getNameWithoutExtension();
    final PsiDirectory dir = templateFile.getParent();
    final PsiFile directiveFile = dir != null ? dir.findFile(name + ".ts") : null;
    if (directiveFile != null) {
      for (PsiElement element : directiveFile.getChildren()) {
        if (element instanceof TypeScriptClass
            && hasTemplateReference(findDecorator((TypeScriptClass)element, COMPONENT_DEC), templateFile)) {
          return (TypeScriptClass)element;
        }
      }
    }
    return null;
  }

  @Nullable
  private static TypeScriptClass resolveComponentFromIndex(@NotNull PsiFile templateFile) {
    final String name = templateFile.getViewProvider().getVirtualFile().getName();
    final Ref<TypeScriptClass> result = new Ref<>();
    AngularIndexUtil.multiResolve(templateFile.getProject(), Angular2TemplateUrlIndex.KEY, name, el -> {
      if (el != null) {
        PsiElement componentDecorator = el.getParent();
        if (componentDecorator instanceof ES6Decorator
            && hasTemplateReference((ES6Decorator)componentDecorator, templateFile)) {
          result.set(PsiTreeUtil.getContextOfType(componentDecorator, TypeScriptClass.class));
          return false;
        }
      }
      return true;
    });
    return result.get();
  }

  private static boolean hasTemplateReference(@Nullable ES6Decorator componentDecorator, @NotNull PsiFile templateFile) {
    JSProperty templateUrl = getProperty(componentDecorator, "templateUrl");
    if (templateUrl == null || templateUrl.getValue() == null) {
      return false;
    }
    return hasFileReference(templateUrl.getValue(), templateFile);
  }

  @Nullable
  private static PsiFile getHostFile(@NotNull PsiElement context) {
    final PsiElement original = CompletionUtil.getOriginalOrSelf(context);
    PsiFile hostFile = FileContextUtil.getContextFile(original != context ? original : context.getContainingFile().getOriginalFile());
    return hostFile != null ? hostFile.getOriginalFile() : null;
  }

}
