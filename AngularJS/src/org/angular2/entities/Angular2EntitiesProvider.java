// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.metadata.psi.Angular2MetadataDirectiveBase;
import org.angular2.entities.metadata.psi.Angular2MetadataEntity;
import org.angular2.entities.metadata.psi.Angular2MetadataPipe;
import org.angular2.entities.source.Angular2SourceComponent;
import org.angular2.entities.source.Angular2SourceDirective;
import org.angular2.entities.source.Angular2SourcePipe;
import org.angular2.index.*;
import org.angular2.lang.Angular2LangUtil;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static com.intellij.util.containers.ContainerUtil.concat;
import static com.intellij.util.containers.ContainerUtil.newHashSet;
import static org.angular2.Angular2DecoratorUtil.*;

public class Angular2EntitiesProvider {

  static final String TRANSFORM_METHOD = "transform";

  @Nullable
  public static Angular2Entity getEntity(@Nullable PsiElement element) {
    Angular2Entity result;
    if ((result = getDirective(element)) != null) {
      return result;
    }
    if ((result = getPipe(element)) != null) {
      return result;
    }
    return null;
  }

  @Nullable
  public static Angular2Component getComponent(@Nullable PsiElement element) {
    return ObjectUtils.tryCast(getDirective(element), Angular2Component.class);
  }

  @Nullable
  public static Angular2Directive getDirective(@Nullable PsiElement element) {
    return getEntity(element, Angular2Directive.class, Angular2IndexingHandler::isDirective,
                     (dec, el) -> DIRECTIVE_DEC.equals(dec.getDecoratorName())
                                  ? new Angular2SourceDirective(dec, el)
                                  : new Angular2SourceComponent(dec, el),
                     DIRECTIVE_DEC, COMPONENT_DEC);
  }

  @Nullable
  public static Angular2Pipe getPipe(@Nullable PsiElement element) {
    if (element instanceof TypeScriptFunction
        && TRANSFORM_METHOD.equals(((TypeScriptFunction)element).getName())
        && element.getContext() instanceof TypeScriptClass) {
      element = element.getContext();
    }
    return getEntity(element, Angular2Pipe.class, Angular2IndexingHandler::isPipe, Angular2SourcePipe::new, PIPE_DEC);
  }

  @NotNull
  public static List<Angular2Directive> findElementDirectivesCandidates(@NotNull Project project, @NotNull String elementName) {
    return findDirectivesCandidates(project, Angular2EntityUtils.getElementDirectiveIndexName(elementName));
  }

  @NotNull
  public static List<Angular2Directive> findAttributeDirectivesCandidates(@NotNull Project project, @NotNull String attributeName) {
    return findDirectivesCandidates(project, Angular2EntityUtils.getAttributeDirectiveIndexName(attributeName));
  }

  @Nullable
  public static Angular2Pipe findPipe(@NotNull Project project, @NotNull String name) {
    JSImplicitElement pipe = AngularIndexUtil.resolve(project, Angular2SourcePipeIndex.KEY, name);
    if (pipe != null) {
      return getPipe(pipe);
    }
    return findMetadataEntity(project, name, Angular2MetadataPipe.class, Angular2MetadataPipeIndex.KEY);
  }

  @SuppressWarnings("BoundedWildcard")
  public static void processDirectives(@NotNull Project project, @NotNull Processor<Angular2Directive> processor) {
    if (!AngularIndexUtil.getAllKeys(Angular2SourceDirectiveIndex.KEY, project)
      .stream()
      .filter(Angular2EntityUtils::isElementDirectiveIndexName)
      .map(name -> AngularIndexUtil.resolve(project, Angular2SourceDirectiveIndex.KEY, name))
      .filter(Objects::nonNull)
      .map(Angular2EntitiesProvider::getComponent)
      .allMatch(c -> processor.process(c))) {
      return;
    }
    AngularIndexUtil.getAllKeys(Angular2MetadataDirectiveIndex.KEY, project)
      .stream()
      .filter(Angular2EntityUtils::isElementDirectiveIndexName)
      .map(name -> findMetadataEntity(project, name, Angular2MetadataDirectiveBase.class, Angular2MetadataDirectiveIndex.KEY))
      .filter(Objects::nonNull)
      .allMatch(c -> processor.process(c));
  }

  public static Collection<String> getAllPipeNames(@NotNull Project project) {
    return newHashSet(concat(AngularIndexUtil.getAllKeys(Angular2SourcePipeIndex.KEY, project),
                             AngularIndexUtil.getAllKeys(Angular2MetadataPipeIndex.KEY, project)));
  }

  public static boolean isPipeTransformMethod(@Nullable PsiElement element) {
    return element instanceof TypeScriptFunction
           && TRANSFORM_METHOD.equals(((TypeScriptFunction)element).getName())
           && getPipe(element) != null;
  }

  private static List<Angular2Directive> findDirectivesCandidates(@NotNull Project project, @NotNull String indexLookupName) {
    List<Angular2Directive> result = new ArrayList<>();
    StubIndex.getInstance().processElements(
      Angular2SourceDirectiveIndex.KEY, indexLookupName, project, GlobalSearchScope.allScope(project), JSImplicitElementProvider.class,
      provider -> {
        final JSElementIndexingData indexingData = provider.getIndexingData();
        if (indexingData != null) {
          final Collection<JSImplicitElement> elements = indexingData.getImplicitElements();
          if (elements != null) {
            for (JSImplicitElement element : elements) {
              if (element.isValid()) {
                Angular2Directive directive = getDirective(element);
                if (directive != null) {
                  result.add(directive);
                  return true;
                }
              }
            }
          }
        }
        return true;
      }
    );
    processMetadataEntities(project, indexLookupName, Angular2MetadataDirectiveBase.class, Angular2MetadataDirectiveIndex.KEY, el -> {
      result.add(el);
      return true;
    });
    return result;
  }

  private static <T extends Angular2MetadataEntity> T findMetadataEntity(@NotNull Project project,
                                                                         @NotNull String name,
                                                                         Class<T> entityClass,
                                                                         StubIndexKey<String, T> key) {
    Ref<T> res = new Ref<>();
    StubIndex.getInstance().processElements(key, name, project, GlobalSearchScope.allScope(project), entityClass, el -> {
      if (el.isValid()) {
        res.set(el);
        return false;
      }
      return true;
    });
    return res.get();
  }

  private static <T extends Angular2MetadataEntity> void processMetadataEntities(@NotNull Project project, @NotNull String name,
                                                                                 Class<T> entityClass, StubIndexKey<String, T> key,
                                                                                 Processor<T> processor) {
    StubIndex.getInstance().processElements(key, name, project, GlobalSearchScope.allScope(project), entityClass, el -> {
      if (el.isValid()) {
        return processor.process(el);
      }
      return true;
    });
  }

  private static <T extends Angular2Entity> T getEntity(@Nullable PsiElement element,
                                                        @NotNull Class<T> entityClass,
                                                        @NotNull Condition<JSImplicitElement> implicitElementTester,
                                                        @NotNull BiFunction<ES6Decorator, JSImplicitElement, T> constructor,
                                                        String... decoratorNames) {
    if (element instanceof JSImplicitElement) {
      if (!implicitElementTester.value((JSImplicitElement)element)) {
        return null;
      }
      element = element.getContext();
    }
    if (element instanceof TypeScriptClass
        && Angular2LangUtil.isAngular2Context(element)) {
      ES6Decorator decorator = findDecorator((TypeScriptClass)element, decoratorNames);
      if (decorator != null) {
        element = decorator;
      }
      else {
        TypeScriptClass typeScriptClass = (TypeScriptClass)element;
        String className = typeScriptClass.getName();
        if (className == null
            ////performance check
            //|| !className.endsWith("Pipe")
            //check classes only from d.ts files
            || !Objects.requireNonNull(typeScriptClass.getAttributeList()).hasModifier(JSAttributeList.ModifierType.DECLARE)) {
          return null;
        }
        Ref<T> result = new Ref<>();
        StubIndex.getInstance().processElements(
          Angular2MetadataEntityClassNameIndex.KEY, className, typeScriptClass.getProject(),
          GlobalSearchScope.projectScope(typeScriptClass.getProject()), Angular2MetadataEntity.class,
          e -> {
            if (e.isValid() && entityClass.isInstance(e) && e.getTypeScriptClass() == typeScriptClass) {
              //noinspection unchecked
              result.set((T)e);
              return false;
            }
            return true;
          });
        return result.get();
      }
    }
    if (element instanceof ES6Decorator) {
      ES6Decorator dec = (ES6Decorator)element;
      if (!ArrayUtil.contains(dec.getDecoratorName(), decoratorNames)) {
        return null;
      }
      return CachedValuesManager.getCachedValue(dec, () -> {
        JSImplicitElement entityElement = null;
        if (dec.getIndexingData() != null) {
          entityElement = ContainerUtil.find(ObjectUtils.notNull(dec.getIndexingData().getImplicitElements(), Collections::emptyList),
                                             implicitElementTester);
        }
        return create(entityElement != null ? constructor.apply(dec, entityElement) : null, dec);
      });
    }
    return null;
  }
}
