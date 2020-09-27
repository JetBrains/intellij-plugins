// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.types.*;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.html.HtmlEmbeddedContentImpl;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.util.containers.ContainerUtil;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.angularjs.index.AngularTemplateUrlIndex;
import org.angularjs.lang.parser.AngularJSElementTypes;
import org.angularjs.lang.psi.AngularJSRecursiveVisitor;
import org.angularjs.lang.psi.AngularJSRepeatExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static org.angularjs.codeInsight.DirectiveUtil.normalizeAttributeName;
import static org.angularjs.index.AngularIndexUtil.hasFileReference;
import static org.angularjs.index.AngularJSIndexingHandler.unquote;

/**
 * @author Dennis.Ushakov
 */
public final class AngularJSProcessor {
  private static final Map<String, String> NG_REPEAT_IMPLICITS = new HashMap<>();

  private static final Set<String> COMPONENT_LIFECYCLE_EVENTS = ContainerUtil.newHashSet(
    "$onInit", "$onChanges", "$doCheck", "$onDestroy", "$postLink"
  );

  public static final String $EVENT = "$event";

  private static final String $CTRL = "$ctrl";

  static {
    NG_REPEAT_IMPLICITS.put("$index", "Number");
    NG_REPEAT_IMPLICITS.put("$first", "Boolean");
    NG_REPEAT_IMPLICITS.put("$middle", "Boolean");
    NG_REPEAT_IMPLICITS.put("$last", "Boolean");
    NG_REPEAT_IMPLICITS.put("$even", "Boolean");
    NG_REPEAT_IMPLICITS.put("$odd", "Boolean");
  }

  public static void process(final PsiElement element, final Consumer<? super JSPsiElementBase> consumer) {
    final PsiElement original = CompletionUtil.getOriginalOrSelf(element);
    PsiFile hostFile = FileContextUtil.getContextFile(original != element ? original : element.getContainingFile().getOriginalFile());
    if (!(hostFile instanceof XmlFile)) {
      hostFile = original.getContainingFile();
    }
    if (!(hostFile instanceof XmlFile)) return;

    final XmlFile file = (XmlFile)hostFile;

    final Collection<JSPsiElementBase> cache = CachedValuesManager.getCachedValue(file, () -> {
      final Collection<JSPsiElementBase> result = new ArrayList<>();
      processDocument(file.getDocument(), result);

      JSObjectLiteralExpression component = getReferencingComponentInitializer(file);
      if (component != null) {
        processComponentInitializer(file, component, result);
      }
      return CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT);
    });
    for (JSPsiElementBase namedElement : cache) {
      if (scopeMatches(original, namedElement)) {
        consumer.accept(namedElement);
      }
    }
  }

  private static void processComponentInitializer(final @NotNull XmlFile file,
                                                  @NotNull JSObjectLiteralExpression componentInitializer,
                                                  @NotNull Collection<? super JSPsiElementBase> result) {
    result.add(new AngularJSLocalImplicitElement(file, componentInitializer));
  }

  private static String getCtrlVarName(@NotNull JSObjectLiteralExpression componentInitializer) {
    String ctrlName = null;
    JSProperty ctrlAs = componentInitializer.findProperty(AngularJSIndexingHandler.CONTROLLER_AS);
    if (ctrlAs != null && ctrlAs.getValue() instanceof JSLiteralExpression && ((JSLiteralExpression)ctrlAs.getValue()).isQuotedLiteral()) {
      ctrlName = unquote(ctrlAs.getValue());
    }
    return ctrlName != null ? ctrlName : $CTRL;
  }

  private static final class AngularJSLocalImplicitElement extends JSLocalImplicitElementImpl {
    private final @NotNull XmlFile myFile;

    private AngularJSLocalImplicitElement(final @NotNull XmlFile file,
                                          @NotNull JSObjectLiteralExpression componentInitializer) {
      super(getCtrlVarName(componentInitializer), getComponentScopeType(file, componentInitializer), componentInitializer,
            JSImplicitElement.Type.Class);
      myFile = file;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      AngularJSLocalImplicitElement element = (AngularJSLocalImplicitElement)o;
      if (!myName.equals(element.myName)) return false;
      if (!Objects.equals(myFile, element.myFile)) return false;
      if (!Objects.equals(myProvider, element.myProvider)) return false;
      if (myKind != element.myKind) return false;
      return true;
    }

    @Override
    public int hashCode() {
      return Objects.hash(getClass(), myFile, myName, myProvider, myKind);
    }
  }

  private static @NotNull JSType getComponentScopeType(final @NotNull XmlFile file,
                                                       @NotNull JSObjectLiteralExpression componentInitializer) {
    List<JSRecordType.TypeMember> memberList = new ArrayList<>();
    Set<String> names = new HashSet<>();
    Consumer<JSRecordType.PropertySignature> processor = member -> {
      if (!COMPONENT_LIFECYCLE_EVENTS.contains(member.getMemberName()) && names.add(member.getMemberName())) {
        memberList.add(member);
      }
    };

    JSProperty property;
    if ((property = componentInitializer.findProperty(AngularJSIndexingHandler.BINDINGS)) != null) {
      contributeBindingProperties(property, processor);
    }
    else if ((property = componentInitializer.findProperty(AngularJSIndexingHandler.BIND_TO_CONTROLLER)) != null) {
      JSExpression bindToController = property.getValue();
      if (bindToController instanceof JSObjectLiteralExpression) {
        contributeBindingProperties(property, processor);
      }
      else if (bindToController instanceof JSLiteralExpression
               && ((JSLiteralExpression)bindToController).getValue() == Boolean.TRUE
               && (property = componentInitializer.findProperty(AngularJSIndexingHandler.SCOPE)) != null) {
        contributeBindingProperties(property, processor);
      }
    }
    contributeControllerProperties(componentInitializer.findProperty(AngularJSIndexingHandler.CONTROLLER),
                                   processor);
    return new JSRecordTypeImpl(
      JSTypeSourceFactory.createTypeSource(file, true), memberList);
  }

  private static void contributeBindingProperties(@Nullable JSProperty bindingsProperty,
                                                  @NotNull Consumer<? super JSRecordType.PropertySignature> processor) {
    if (bindingsProperty != null && (bindingsProperty.getValue() instanceof JSObjectLiteralExpression)) {
      JSObjectLiteralExpression bindings = (JSObjectLiteralExpression)bindingsProperty.getValue();
      for (JSProperty binding : bindings.getProperties()) {
        if (binding.getName() != null) {
          processor.accept(new JSRecordTypeImpl.PropertySignatureImpl(binding.getName(), JSAnyType.get(bindings, true), true, false));
        }
      }
    }
  }

  private static void contributeControllerProperties(@Nullable JSProperty controllerProperty,
                                                     @NotNull Consumer<? super JSRecordType.PropertySignature> processor) {
    if (controllerProperty != null && controllerProperty.getValue() != null) {
      PsiElement controller = controllerProperty.getValue();
      JSNamespace namespace = null;
      if (controller instanceof JSLiteralExpression && ((JSLiteralExpression)controller).isQuotedLiteral()) {
        for (PsiReference ref : controller.getReferences()) {
          PsiElement resolved = ref.resolve();
          if (resolved instanceof JSImplicitElement) {
            resolved = resolved.getParent();
          }
          if (resolved instanceof JSLiteralExpression
              && resolved.getParent() instanceof JSArgumentList) {
            JSQualifiedName qName = JSSymbolUtil.getLiteralValueAsQualifiedName((JSLiteralExpression)resolved);
            if (qName != null) {
              namespace = JSNamedTypeFactory.createNamespace(qName, JSContext.INSTANCE, resolved);
            }
          }
        }
      }
      if (controller instanceof JSReferenceExpression) {
        PsiElement resolved = ((JSReferenceExpression)controller).resolve();
        if (resolved != null) {
          controller = resolved;
        }
      }
      if (controller instanceof JSFunctionExpression) {
        JSType type = JSResolveUtil.getExpressionJSType((JSExpression)controller);
        if (type instanceof JSNamespace) {
          namespace = (JSNamespace)type;
        }
      }
      else if (controller instanceof JSFunction) {
        namespace = JSNamedTypeFactory.buildProvidedNamespace((JSFunction)controller, true);
      }
      if (namespace != null && namespace.getQualifiedName() != null) {
        JSClassResolver.getInstance()
          .findNamespaceMembers(namespace.getQualifiedName().getQualifiedName(), controller.getResolveScope())
          .stream()
          .filter(el -> el.getName() != null)
          .map(el -> new JSRecordTypeImpl.PropertySignatureImpl(
            el.getName(), JSResolveUtil.getElementJSType(el),
            true, false, el))
          .forEach(processor);
      }
    }
  }

  private static JSObjectLiteralExpression getReferencingComponentInitializer(PsiFile templateFile) {
    final String name = templateFile.getViewProvider().getVirtualFile().getName();
    final Ref<JSObjectLiteralExpression> result = new Ref<>();
    AngularIndexUtil.multiResolve(templateFile.getProject(), AngularTemplateUrlIndex.KEY, name, el -> {
      if (el.getParent() instanceof JSProperty && el.getParent().getParent() instanceof JSObjectLiteralExpression) {
        JSExpression value = ((JSProperty)el.getParent()).getValue();
        if (value != null && hasFileReference(value, templateFile)) {
          result.set((JSObjectLiteralExpression)el.getParent().getParent());
          return false;
        }
      }
      return true;
    });
    return result.get();
  }

  private static void processDocument(XmlDocument document, final Collection<? super JSPsiElementBase> result) {
    if (document == null) return;
    final AngularInjectedFilesVisitor visitor = new AngularInjectedFilesVisitor(result);

    for (XmlTag tag : PsiTreeUtil.getChildrenOfTypeAsList(document, XmlTag.class)) {
      ProgressIndicatorProvider.checkCanceled();
      new XmlBackedJSClassImpl.InjectedScriptsVisitor(tag, null, true, true, visitor, true) {
        @Override
        public boolean execute(@NotNull PsiElement element) {
          ProgressIndicatorProvider.checkCanceled();
          if (element instanceof HtmlEmbeddedContentImpl) {
            processDocument(PsiTreeUtil.findChildOfType(element, XmlDocument.class), result);
          }
          if (element instanceof XmlAttribute) {
            visitor.accept(element);
          }
          return super.execute(element);
        }
      }.go();
    }
  }

  private static boolean scopeMatches(PsiElement element, PsiElement declaration) {
    final InjectedLanguageManager injector = InjectedLanguageManager.getInstance(element.getProject());
    if (declaration instanceof JSImplicitElement) {
      if ($EVENT.equals(((JSImplicitElement)declaration).getName())) {
        return eventScopeMatches(injector, element, declaration.getParent());
      }
      declaration = declaration.getParent();
    }
    final PsiLanguageInjectionHost elementContainer = injector.getInjectionHost(element);
    final XmlTagChild elementTag = PsiTreeUtil.getNonStrictParentOfType(elementContainer, XmlTag.class, XmlText.class);
    final PsiLanguageInjectionHost declarationContainer = injector.getInjectionHost(declaration);
    final XmlTagChild declarationTag = PsiTreeUtil.getNonStrictParentOfType(declarationContainer, XmlTag.class);

    if (declarationContainer != null && elementContainer != null && elementTag != null && declarationTag != null) {
      return PsiTreeUtil.isAncestor(declarationTag, elementTag, true) ||
             PsiTreeUtil.isAncestor(declarationTag, elementTag, false) &&
             declarationContainer.getTextOffset() < elementContainer.getTextOffset() ||
             isInRepeatStartEnd(declarationTag, declarationContainer, elementContainer);
    }
    return true;
  }

  private static boolean isInRepeatStartEnd(XmlTagChild declarationTag,
                                            PsiLanguageInjectionHost declarationContainer,
                                            PsiLanguageInjectionHost elementContainer) {
    PsiElement parent = declarationContainer.getParent();
    if (parent instanceof XmlAttribute && "ngRepeatStart".contentEquals(normalizeAttributeName(((XmlAttribute)parent).getName(), false))) {
      XmlTagChild next = declarationTag.getNextSiblingInTag();
      while (next != null) {
        if (PsiTreeUtil.isAncestor(next, elementContainer, true)) return true;
        if (next instanceof XmlTag
            && ContainerUtil.find(((XmlTag)next).getAttributes(),
                                  attr -> "ngRepeatEnd".contentEquals(normalizeAttributeName(attr.getName(), false))) != null) {
          break;
        }
        next = next.getNextSiblingInTag();
      }
    }
    return false;
  }

  private static boolean eventScopeMatches(InjectedLanguageManager injector, PsiElement element, PsiElement parent) {
    XmlAttribute attribute = PsiTreeUtil.getNonStrictParentOfType(element, XmlAttribute.class);
    if (attribute == null) {
      final PsiLanguageInjectionHost elementContainer = injector.getInjectionHost(element);
      attribute = PsiTreeUtil.getNonStrictParentOfType(elementContainer, XmlAttribute.class);
    }
    return attribute != null && CompletionUtil.getOriginalOrSelf(attribute) == CompletionUtil.getOriginalOrSelf(parent);
  }

  private static class AngularInjectedFilesVisitor extends JSResolveUtil.JSInjectedFilesVisitor {
    private final Collection<? super JSPsiElementBase> myResult;

    AngularInjectedFilesVisitor(Collection<? super JSPsiElementBase> result) {
      myResult = result;
    }

    @Override
    protected void process(JSFile file) {
      accept(file);
    }

    protected void accept(PsiElement element) {
      element.accept(new AngularJSRecursiveVisitor() {
        @Override
        public void visitJSDefinitionExpression(JSDefinitionExpression node) {
          myResult.add(node);
          super.visitJSDefinitionExpression(node);
        }

        @Override
        public void visitJSVariable(JSVariable node) {
          myResult.add(node);
          super.visitJSVariable(node);
        }

        @Override
        public void visitAngularJSRepeatExpression(AngularJSRepeatExpression repeatExpression) {
          if (repeatExpression.getNode().getElementType() == AngularJSElementTypes.REPEAT_EXPRESSION) {
            for (Map.Entry<String, String> entry : NG_REPEAT_IMPLICITS.entrySet()) {
              myResult.add(new JSLocalImplicitElementImpl(entry.getKey(), entry.getValue(), repeatExpression));
            }
          }
          super.visitAngularJSRepeatExpression(repeatExpression);
        }
      });
    }
  }
}
