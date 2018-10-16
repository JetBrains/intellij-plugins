// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.codeInsight.Angular2PipeUtil;
import org.angular2.codeInsight.metadata.AngularDirectiveMetadata;
import org.angular2.entities.metadata.psi.Angular2MetadataEntity;
import org.angular2.entities.metadata.psi.Angular2MetadataPipe;
import org.angular2.entities.source.Angular2SourcePipe;
import org.angular2.index.Angular2MetadataEntityClassNameIndex;
import org.angular2.index.Angular2MetadataPipeIndex;
import org.angular2.lang.Angular2LangUtil;
import org.angularjs.index.AngularFilterIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static com.intellij.util.containers.ContainerUtil.concat;
import static com.intellij.util.containers.ContainerUtil.newHashSet;

public class Angular2EntitiesProvider {

  @Nullable
  public static Angular2Component getComponent(TypeScriptClass cls) {
    Angular2Directive directive = AngularDirectiveMetadata.create(cls);
    return directive instanceof Angular2Component ? (Angular2Component)directive : null;
  }

  @Nullable
  public static Angular2Component getComponent(JSImplicitElement element) {
    Angular2Directive directive = AngularDirectiveMetadata.create(element);
    return directive instanceof Angular2Component ? (Angular2Component)directive : null;
  }

  @NotNull
  public static Angular2Directive getDirective(JSImplicitElement directive) {
    return AngularDirectiveMetadata.create(directive);
  }

  public static Collection<String> getAllPipeNames(@NotNull Project project) {
    return newHashSet(concat(AngularIndexUtil.getAllKeys(AngularFilterIndex.KEY, project),
                             AngularIndexUtil.getAllKeys(Angular2MetadataPipeIndex.KEY, project)));
  }

  @Nullable
  public static Angular2Pipe findPipe(@NotNull Project project, @NotNull String name) {
    JSImplicitElement pipe = AngularIndexUtil.resolve(project, AngularFilterIndex.KEY, name);
    if (pipe != null) {
      return getPipe(pipe);
    }
    Ref<Angular2Pipe> res = new Ref<>();
    StubIndex.getInstance().processElements(Angular2MetadataPipeIndex.KEY, name, project,
                                            GlobalSearchScope.allScope(project), Angular2MetadataPipe.class, el -> {
        if (el.isValid()) {
          res.set(el);
          return false;
        }
        return true;
      });
    return res.get();
  }

  @Nullable
  public static Angular2Pipe getPipe(@Nullable PsiElement element) {
    if (element instanceof JSImplicitElement
        && Angular2PipeUtil.isPipeType(((JSImplicitElement)element).getTypeString())) {
      element = element.getContext();
    }
    else if (element instanceof TypeScriptFunction
             && Angular2PipeUtil.TRANSFORM_METHOD.equals(((TypeScriptFunction)element).getName())
             && element.getContext() instanceof TypeScriptClass) {
      element = element.getContext();
    }
    if (element instanceof TypeScriptClass
        && Angular2LangUtil.isAngular2Context(element)) {
      ES6Decorator decorator = Angular2DecoratorUtil.findDecorator((TypeScriptClass)element, Angular2PipeUtil.PIPE_DEC);
      if (decorator != null) {
        element = decorator;
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
        Ref<Angular2Pipe> result = new Ref<>();
        StubIndex.getInstance().processElements(Angular2MetadataEntityClassNameIndex.KEY, className, pipeClass.getProject(),
                                                GlobalSearchScope.projectScope(pipeClass.getProject()), Angular2MetadataEntity.class, e -> {
            if (e.isValid() && e instanceof Angular2Pipe && e.getTypeScriptClass() == pipeClass) {
              result.set((Angular2Pipe)e);
              return false;
            }
            return true;
          });
        return result.get();
      }
    }
    if (element instanceof ES6Decorator) {
      ES6Decorator dec = (ES6Decorator)element;
      return CachedValuesManager.getCachedValue(dec, () -> {
        JSImplicitElement pipeElement = null;
        if (dec.getIndexingData() != null) {
          pipeElement = ContainerUtil.find(ObjectUtils.notNull(dec.getIndexingData().getImplicitElements(), Collections::emptyList),
                                           el -> Angular2PipeUtil.isPipeType(el.getTypeString()));
        }
        return create(pipeElement != null ? new Angular2SourcePipe(dec, pipeElement, pipeElement.getName()) : null, dec);
      });
    }
    return null;
  }
}
