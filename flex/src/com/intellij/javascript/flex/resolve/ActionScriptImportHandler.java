// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.JSResolveHelper;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptImportHandler extends JSImportHandler {
  private static final ActionScriptImportHandler INSTANCE = new ActionScriptImportHandler();

  protected ActionScriptImportHandler() {}

  @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
  public static JSImportHandler getInstance() {
    return INSTANCE;
  }

  @Override
  public @NotNull JSTypeResolveResult resolveName(@NotNull String type, @NotNull PsiElement context) {
    final JSImportedElementResolveResult result = _resolveTypeName(type, context);
    String resolvedType = result != null ? result.qualifiedName : type;
    return new JSTypeResolveResult(resolvedType != null ? resolvedType : type, null);
  }

  // TODO _str should be JSReferenceExpression for caching!
  private @Nullable JSImportedElementResolveResult _resolveTypeName(final @Nullable String _name, @NotNull PsiElement context) {
    String name = _name;
    if (name == null) return null;
    JSResolveUtil.GenericSignature genericSignature = JSResolveUtil.extractGenericSignature(name);

    if (genericSignature != null) {
      name = genericSignature.elementType;
    }

    final Ref<JSImportedElementResolveResult> resultRef = new Ref<>();

    final String name1 = name;
    ActionScriptResolveUtil.walkOverStructure(context, context1 -> {
      JSImportedElementResolveResult resolved = null;

      if (context1 instanceof XmlBackedJSClassImpl) { // reference list in mxml
        XmlTag rootTag = ((XmlBackedJSClassImpl)context1).getParent();
        if (rootTag != null && name1.equals(rootTag.getLocalName())) {
          final XmlElementDescriptor descriptor = rootTag.getDescriptor();
          PsiElement element = descriptor != null ? descriptor.getDeclaration():null;
          if (element instanceof XmlFile) {
            element = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)element);
          }

          final String s = element instanceof JSClass ? ((JSClass)element).getQualifiedName() : rootTag.getLocalName();
          resolved = new JSImportedElementResolveResult(s);
        } else {
          resolved = resolveTypeNameUsingImports(name1, context1);
        }
      } else if (context1 instanceof JSQualifiedNamedElement) {
        if (context1 instanceof JSClass && name1.equals(context1.getName())) {
          resolved = new JSImportedElementResolveResult(((JSQualifiedNamedElement)context1).getQualifiedName());
        } else {
          resolved = resolveTypeNameUsingImports(name1, context1);

          if (resolved == null && context1.getParent() instanceof JSFile) {
            final String qName = ((JSQualifiedNamedElement)context1).getQualifiedName();
            final String packageName = qName == null ? "" :
                                       context1 instanceof JSPackageStatement ? qName + "." :
                                       qName.substring( 0, qName.lastIndexOf('.') + 1);

            if (packageName.length() != 0) {
              final PsiElement byQName = JSClassResolver.findClassFromNamespace(packageName + name1, context1);

              if (byQName instanceof JSQualifiedNamedElement) {
                resolved = new JSImportedElementResolveResult(((JSQualifiedNamedElement)byQName).getQualifiedName());
              }
            }
          }
        }
      }
      else {
        resolved = resolveTypeNameUsingImports(name1, context1);
        PsiElement contextOfContext;

        if (resolved == null && context1 instanceof JSFile && (contextOfContext = context1.getContext()) != null) {
          XmlBackedJSClassImpl clazz = contextOfContext instanceof XmlElement
                                       ? (XmlBackedJSClassImpl)XmlBackedJSClassImpl.getContainingComponent((XmlElement)contextOfContext)
                                       : null;

          if (clazz != null) {
            SinkResolveProcessor r = new SinkResolveProcessor(name1, new ResolveResultSink(null, name1));
            r.setForceImportsForPlace(true);
            boolean b = clazz.doImportFromScripts(r, clazz);

            if(!b) {
              PsiElement resultFromProcessor = r.getResult();
              JSQualifiedNamedElement clazzFromComponent =
                resultFromProcessor instanceof JSQualifiedNamedElement ? (JSQualifiedNamedElement)resultFromProcessor : null;

              if (clazzFromComponent != null) {
                resolved = new JSImportedElementResolveResult(clazzFromComponent.getQualifiedName(), clazz, null);
              }
            }
          }
        }
      }

      if (resolved != null) {
        resultRef.set(resolved);
        return false;
      }

      if (context1 instanceof JSPackageStatement) return false;
      return true;
    });

    JSImportedElementResolveResult result = resultRef.get();

    if (genericSignature != null && result != null) {
      // TODO: more than one type parameter
      StringBuilder genericSignatureBuffer = new StringBuilder();
      genericSignatureBuffer.append(".<");
      genericSignatureBuffer.append(resolveTypeName(genericSignature.genericType, context).getQualifiedName());
      genericSignatureBuffer.append(">");
      result = result.appendSignature(genericSignatureBuffer.toString());
    }
    return result;
  }

  private static JSNamedElement resolveTypeNameInTheSamePackage(@NotNull String str, @NotNull PsiElement context) {
    JSNamedElement fileLocalElement = JSResolveUtil.findFileLocalElement(str, context);
    if (fileLocalElement != null) return fileLocalElement;

    final String packageQualifierText = JSResolveUtil.findPackageStatementQualifier(context);
    PsiElement byQName;

    if (packageQualifierText != null) {
      byQName = JSClassResolver.findClassFromNamespace(packageQualifierText + "." + str, context);
      if (byQName instanceof JSQualifiedNamedElement) {
        return (JSQualifiedNamedElement)byQName;
      }
    }

    byQName = JSDialectSpecificHandlersFactory.forElement(context).getClassResolver().findClassByQName(str, context);
    if (byQName instanceof JSQualifiedNamedElement &&
        ActionScriptResolveUtil
          .acceptableSymbol((JSQualifiedNamedElement)byQName, ActionScriptResolveUtil.GlobalSymbolsAcceptanceState.WHATEVER, false,
                            context)) {
      return (JSQualifiedNamedElement)byQName;
    }

    return null;
  }


  @Override
  public @Nullable JSImportedElementResolveResult resolveTypeNameUsingImports(@NotNull JSReferenceExpression expr) {
    if (expr.getQualifier() != null) return null;
    if (JSResolveUtil.getElementThatShouldBeQualified(expr, null) != null) return null;
    if (expr.getReferencedName() == null) return null;

    return _resolveTypeName(expr.getText(), expr);
  }

  private static @Nullable JSImportedElementResolveResult resolveTypeNameUsingImports(final @NotNull String referencedName, PsiNamedElement parent) {
    Map<String, JSImportedElementResolveResult> map = CachedValuesManager.getProjectPsiDependentCache(parent, __ -> new ConcurrentHashMap<>());
    JSImportedElementResolveResult result = map.get(referencedName);

    if (result == null) {
      SinkResolveProcessor<ResolveResultSink> resolveProcessor = new SinkResolveProcessor<>(referencedName, new ResolveResultSink(null, referencedName));
      resolveTypeNameUsingImportsInner(resolveProcessor, parent);
      final ResolveResult[] resolveResults = resolveProcessor.getResultsAsResolveResults();
      assert resolveResults.length < 2;
      if (resolveResults.length == 1 && resolveResults[0] instanceof JSResolveResult) {
        JSResolveResult resolveResult = (JSResolveResult)resolveResults[0];
        final PsiElement element = resolveResult.getElement();
        String typeName = ((JSQualifiedNamedElement)element).getQualifiedName();
        result = new JSImportedElementResolveResult(typeName, element, resolveResult.getActionScriptImport());
      }
      map.put(referencedName, result != null ? result: JSImportedElementResolveResult.EMPTY_RESULT);
    }

    return result != JSImportedElementResolveResult.EMPTY_RESULT ? result:null;
  }

  private static boolean resolveTypeNameUsingImportsInner(final ResolveProcessor resolveProcessor, final PsiNamedElement parent) {
    final PsiElement element = JSResolveUtil.getClassReferenceForXmlFromContext(parent);

    if (!FlexResolveHelper.ourPsiScopedImportSet.tryResolveImportedClass(parent, resolveProcessor)) return false;

    if (parent instanceof JSPackageStatement) {
      final JSNamedElement jsClass = resolveTypeNameInTheSamePackage(resolveProcessor.getName(), parent);
      return jsClass == null || resolveProcessor.execute(jsClass, ResolveState.initial());
    } else if (parent instanceof JSFile && parent.getLanguage().isKindOf(JavaScriptSupportLoader.ECMA_SCRIPT_L4)) {
      if (element instanceof XmlBackedJSClassImpl) {
        //if(!((XmlBackedJSClassImpl)element).doImportFromScripts(resolveProcessor, parent)) return false; REMOVE?

        JSNamedElement jsClass = resolveTypeNameInTheSamePackage(resolveProcessor.getName(), element);

        if (jsClass == null) {
          final JSClass parentClass = (JSClass)element;
          final JSClass[] classes = parentClass.getSuperClasses();

          if (classes.length > 0 && resolveProcessor.getName().equals(classes[0].getName())) {
            jsClass = classes[0];
          }
        }

        if (jsClass != null && !resolveProcessor.execute(jsClass, ResolveState.initial())) return false;
      } else {
        JSNamedElement jsClass = element != null ? resolveTypeNameInTheSamePackage(resolveProcessor.getName(), element) : null;
        return jsClass == null || resolveProcessor.execute(jsClass, ResolveState.initial());
      }
    }
    else if (parent instanceof XmlBackedJSClassImpl) {
      JSNamedElement jsClass = resolveTypeNameInTheSamePackage(resolveProcessor.getName(), parent);
      if (jsClass != null && !resolveProcessor.execute(jsClass, ResolveState.initial())) return false;

    }
    for (JSResolveHelper helper : JSResolveHelper.EP_NAME.getExtensionList()) {
      if (!helper.resolveTypeNameUsingImports(resolveProcessor, parent)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean importClass(final PsiScopeProcessor processor, final PsiNamedElement parent) {
    final ResolveProcessor resolveProcessor = (ResolveProcessor)processor;
    if(resolveProcessor.isLocalResolve() || resolveProcessor.needPackages()) return true;
    final String s = resolveProcessor.getName();

    if (s != null) {
      if (resolveProcessor.needsAllVariants()) {
        return resolveTypeNameUsingImportsInner(resolveProcessor, parent);
      }

      final JSImportedElementResolveResult expression = resolveTypeNameUsingImports(s, parent);

      return !dispatchResult(expression, processor);
    } else {
      return importClassViaHelper(processor, parent);
    }
  }

  private static boolean dispatchResult(JSImportedElementResolveResult expression, PsiScopeProcessor processor) {
    if (expression != null) {
      final PsiElement element = expression.resolvedElement;

      if (element != null) {
        ResolveState state = ResolveState.initial();
        if (expression.importStatement != null) state = state.put(JSResolveResult.IMPORT_KEY, expression.importStatement);
        return !processor.execute(element, state);
      }
    }

    return false;
  }

  public static boolean importClassViaHelper(final PsiScopeProcessor processor,
                                             final PsiNamedElement file) {
    for(JSResolveHelper helper: JSResolveHelper.EP_NAME.getExtensionList()) {
      if (!helper.importClass(processor, file)) return false;
    }
    return true;
  }
}
