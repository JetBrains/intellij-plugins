// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.json.psi.JsonFile;
import com.intellij.lang.javascript.index.JSImplicitElementsIndex;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.codeInsight.metadata.AngularPipeMetadata;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;

public class Angular2PipeUtil {

  public static final String PIPE_DEC = "Pipe";
  public static final String NAME_PROP = "name";
  public static final String TRANSFORM_METHOD = "transform";

  @Nullable
  public static JSImplicitElement getPipe(@Nullable PsiElement element) {
    if (element instanceof JSImplicitElement
        && isPipeType(((JSImplicitElement)element).getTypeString())) {
      return (JSImplicitElement)element;
    }
    if (element instanceof TypeScriptFunction
        && TRANSFORM_METHOD.equals(((TypeScriptFunction)element).getName())
        && element.getParent() instanceof TypeScriptClass) {
      element = element.getContext();
    }
    if (element instanceof TypeScriptClass
        && Angular2LangUtil.isAngular2Context(element)) {
      JSCallExpression decorator = Angular2DecoratorUtil.getDecorator((TypeScriptClass)element, PIPE_DEC);
      if (decorator != null) {
        return decorator.getIndexingData() != null ?
               ContainerUtil.find(ObjectUtils.notNull(decorator.getIndexingData().getImplicitElements(), Collections::emptyList),
                                  el -> isPipeType(el.getTypeString()))
                                                   : null;
      }
      else {
        TypeScriptClass pipeClass = (TypeScriptClass)element;
        String className = pipeClass.getName();
        if (className == null
            //performance check
            || !className.endsWith("Pipe")
            //check classes only from d.ts files
            || !Objects.requireNonNull(pipeClass.getAttributeList()).hasModifier(JSAttributeList.ModifierType.DECLARE)) {
          return null;
        }
        Ref<JSImplicitElement> result = new Ref<>();
        FileBasedIndex.getInstance().processValues(
          JSImplicitElementsIndex.INDEX_ID, className, null, (virtualFile, value) -> {
            final PsiFile psiFile = pipeClass.getManager().findFile(virtualFile);
            if (psiFile instanceof JsonFile) {
              for (JSImplicitElementsIndex.JSElementProxy proxy : value) {
                JSOffsetBasedImplicitElement implicitElement = proxy.toOffsetBasedImplicitElement(psiFile);
                String pipeName = getPipeName(implicitElement.getTypeString());
                if (pipeName != null) {
                  JSImplicitElement pipeElement =
                    implicitElement.toBuilder().setName(pipeName).setTypeString(createTypeString(null)).toImplicitElement();
                  AngularPipeMetadata metadata = AngularPipeMetadata.create(pipeElement);
                  if (metadata.getPipeClass() == pipeClass) {
                    result.set(pipeElement);
                    return false;
                  }
                }
              }
            }
            return true;
          },
          GlobalSearchScope.allScope(pipeClass.getProject()));
        return result.get();
      }
    }
    return null;
  }

  public static boolean isPipeTransformMethod(PsiElement element) {
    return element instanceof TypeScriptFunction
           && TRANSFORM_METHOD.equals(((TypeScriptFunction)element).getName())
           && getPipe(element) != null;
  }

  public static boolean isPipeType(@Nullable String type) {
    return type != null && type.startsWith("P;") && type.endsWith(";;") && type.length() >= 4;
  }

  public static boolean isPipeClassType(@Nullable String type) {
    return type != null && type.startsWith("PC;") && type.endsWith(";;") && type.length() >= 5;
  }

  @Nullable
  private static String getPipeName(@Nullable String type) {
    if (!isPipeClassType(type)) {
      return null;
    }
    return type.substring(3, type.length() - 2);
  }

  public static String createTypeString(@Nullable String aClass) {
    return "P;" + StringUtil.notNullize(aClass) + ";;";
  }

  public static String createClassTypeString(@Nullable String pipeName) {
    return "PC;" + StringUtil.notNullize(pipeName) + ";;";
  }
}
