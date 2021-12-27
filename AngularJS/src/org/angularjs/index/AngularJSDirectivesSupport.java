// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.index;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElementStructure;
import com.intellij.lang.javascript.psi.stubs.JSImplicitItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.util.text.Strings;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.CharSequenceSubSequence;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static org.angularjs.index.AngularJSIndexingHandler.ANGULAR_DIRECTIVES_DOC_INDEX_USER_STRING;
import static org.angularjs.index.AngularJSIndexingHandler.ANGULAR_DIRECTIVES_INDEX_USER_STRING;

public final class AngularJSDirectivesSupport {

  private static final String INDEX_TAG_NAME_PREFIX = ">";
  private static final String INDEX_ANY_TAG_NAME = "T";
  private static final String INDEX_ATTRIBUTE_NAME_PREFIX = "=";
  private static final String INDEX_ANY_ATTRIBUTE_NAME = "A";

  public static @NotNull List<JSImplicitElement> findTagDirectives(@NotNull Project project, @Nullable String tagName) {
    return ContainerUtil.filter(findDirectivesCandidates(project, getTagDirectiveIndexName(tagName)),
                                AngularJSDirectivesSupport::isTagDirective);
  }

  public static @NotNull List<JSImplicitElement> findAttributeDirectives(@NotNull Project project, @Nullable String attributeName) {
    return findDirectivesCandidates(project, getAttributeDirectiveIndexName(attributeName));
  }

  public static @Nullable JSImplicitElement findDirective(@NotNull Project project, @NotNull String name) {
    return StreamEx.of(findTagDirectives(project, name))
      .append(findAttributeDirectives(project, name))
      .filter(directive -> name.equals(directive.getName()))
      // Doc directives should go first
      .sortedBy(JSImplicitItem::getUserString)
      .findFirst()
      .orElse(null);
  }

  public static List<String> getDirectiveIndexKeys(@NotNull JSImplicitElementStructure directive) {
    String restrictions = directive.getUserStringData();
    List<String> result = new SmartList<>();
    String name = directive.getName();
    boolean isAttribute = false;
    boolean isTag = false;
    if (restrictions == null) {
      isAttribute = true;
    }
    else {
      int semicolon = restrictions.indexOf(';');
      if (semicolon < 0) {
        isAttribute = true;
      }
      else {
        CharSequence restrict = new CharSequenceSubSequence(restrictions, 0, semicolon);
        if (StringUtil.isEmpty(restrict)) {
          isAttribute = true;
        }
        else {
          if (Strings.indexOfIgnoreCase(restrict, "A", 0) >= 0) {
            isAttribute = true;
          }
          if (StringUtil.equals(AngularJSIndexingHandler.DEFAULT_RESTRICTIONS, restrict)
              || Strings.indexOfIgnoreCase(restrict, "E", 0) >= 0) {
            isTag = true;
          }
        }
      }
    }
    if (isAttribute) {
      result.add(getAttributeDirectiveIndexName(null));
      result.add(getAttributeDirectiveIndexName(name));
    }
    if (isTag) {
      result.add(getTagDirectiveIndexName(name));
      result.add(getTagDirectiveIndexName(null));
    }
    return result;
  }

  public static boolean isTagDirective(@NotNull JSImplicitElement directive) {
    final String restrictions = directive.getUserStringData();
    if (restrictions != null) {
      final CharSequence restrict =
        AngularIndexUtil.convertRestrictions(directive.getProject(), restrictions.subSequence(0, restrictions.indexOf(';')));
      return !StringUtil.isEmpty(restrict) && Strings.indexOfIgnoreCase(restrict, "E", 0) >= 0;
    }
    return false;
  }

  public static boolean isDirective(@NotNull JSImplicitElement directive) {
    final String userString = directive.getUserString();
    return ANGULAR_DIRECTIVES_INDEX_USER_STRING.equals(userString)
           || ANGULAR_DIRECTIVES_DOC_INDEX_USER_STRING.equals(userString);
  }

  private static @NotNull List<JSImplicitElement> findDirectivesCandidates(@NotNull Project project, @NotNull String indexLookupName) {
    List<JSImplicitElement> result = new ArrayList<>();
    Set<String> documentedDirectives = new HashSet<>();
    processDirectivesFromIndex(project, AngularDirectivesDocIndex.KEY, indexLookupName, directive -> {
      result.add(directive);
      documentedDirectives.add(directive.getName());
    });
    processDirectivesFromIndex(project, AngularDirectivesIndex.KEY, indexLookupName, directive -> {
      if (!documentedDirectives.contains(directive.getName())) {
        result.add(directive);
      }
    });
    return result;
  }

  private static void processDirectivesFromIndex(@NotNull Project project, StubIndexKey<String, JSImplicitElementProvider> key,
                                                 @NotNull String indexLookupName, Consumer<JSImplicitElement> consumer) {
    StubIndex.getInstance().processElements(
      key, indexLookupName, project, GlobalSearchScope.allScope(project), JSImplicitElementProvider.class,
      provider -> {
        final JSElementIndexingData indexingData = provider.getIndexingData();
        if (indexingData != null) {
          final Collection<JSImplicitElement> elements = indexingData.getImplicitElements();
          if (elements != null) {
            for (JSImplicitElement element : elements) {
              if (element.isValid() && isDirective(element)) {
                consumer.accept(element);
              }
            }
          }
        }
        return true;
      }
    );
  }

  private static @NotNull String getTagDirectiveIndexName(@Nullable String tagName) {
    return tagName != null ? INDEX_TAG_NAME_PREFIX + tagName : INDEX_ANY_TAG_NAME;
  }

  private static @NotNull String getAttributeDirectiveIndexName(@Nullable String attributeName) {
    return attributeName != null ? INDEX_ATTRIBUTE_NAME_PREFIX + attributeName : INDEX_ANY_ATTRIBUTE_NAME;
  }
}
