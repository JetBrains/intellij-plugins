/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.annotation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.protobuf.ide.PbCompositeModificationTracker;
import com.intellij.protobuf.lang.PbLangBundle;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A helper class to track option occurrences within some scope and annotate the following problems:
 *
 * <ul>
 *   <li>A non-repeated option specified multiple times
 *   <li>Multiple members of a oneof specified
 *   <li>A missing required field
 * </ul>
 */
public class OptionOccurrenceTracker {

  private final Occurrence root = new Occurrence(null, null, null);
  private final Multimap<PsiElement, Occurrence> elementOccurrences = ArrayListMultimap.create();

  private OptionOccurrenceTracker() {}

  /**
   * Returns a tracker containing occurrences for all descendants of the given {@link PbOptionOwner
   * owner}.
   *
   * @param owner the owner
   * @return the tracker
   */
  public static OptionOccurrenceTracker forOptionOwner(PbOptionOwner owner) {
    return CachedValuesManager.getCachedValue(
        owner,
        () -> {
          OptionOccurrenceTracker tracker = new OptionOccurrenceTracker();
          tracker.addAllOccurrences(owner);
          return Result.create(tracker, PbCompositeModificationTracker.byElement(owner));
        });
  }

  /**
   * Returns the appropriate tracker to use for the given {@link PbTextMessage message}.
   *
   * <p>The tracker will be rooted at one of the following:
   *
   * <ul>
   *   <li>The closest descendant Any value if the given message is within an embedded Any
   *   <li>The top-level message if the file is standalone
   *   <li>The host {@link PbOptionOwner} if the file is injected
   * </ul>
   *
   * <p>The tracker is populated with all descendants of the determined root, except for those under
   * embedded Any values.
   *
   * @param message the message
   * @return the tracker
   */
  public static @Nullable OptionOccurrenceTracker forMessage(PbTextMessage message) {
    return CachedValuesManager.getCachedValue(
        message,
        () -> Result.create(computeForMessage(message), PbCompositeModificationTracker.byElement(message)));
  }

  private static OptionOccurrenceTracker computeForMessage(PbTextMessage message) {
    if (message instanceof PbTextRootMessage) {
      if (message.getContainingFile() instanceof PbFile) {
        PbOptionOwner owner = PbPsiImplUtil.getOptionOwner(message);
        if (owner == null) {
          return null;
        }
        return OptionOccurrenceTracker.forOptionOwner(owner);
      }
      OptionOccurrenceTracker tracker = new OptionOccurrenceTracker();
      tracker.addAllOccurrences(message);
      return tracker;
    }

    // Any messages are themselves roots.
    if (isAnyBody(message)) {
      OptionOccurrenceTracker tracker = new OptionOccurrenceTracker();
      tracker.addAllOccurrences(message);
      return tracker;
    }

    PbTextMessage parent = PsiTreeUtil.getParentOfType(message, PbTextMessage.class);
    if (parent == null) {
      return null;
    }
    return forMessage(parent);
  }

  private static boolean isAnyBody(PbTextMessage message) {
    PbTextField parentField = PsiTreeUtil.getParentOfType(message, PbTextField.class);
    if (parentField == null) {
      return false;
    }
    PbTextExtensionName extensionName = parentField.getFieldName().getExtensionName();
    return extensionName != null && extensionName.isAnyTypeUrl();
  }

  /** Returns the root occurrence. */
  public @NotNull Occurrence getRootOccurrence() {
    return root;
  }

  /** Return the occurrence for the given {@link PbOptionName}. */
  public @Nullable Occurrence getOccurrence(PbOptionName name) {
    return elementOccurrences.get(name).stream().findFirst().orElse(null);
  }

  /** Return the occurrences for the given {@link PbTextFieldName}. */
  public @NotNull Collection<Occurrence> getOccurrences(PbTextFieldName name) {
    return elementOccurrences.get(name);
  }

  /** Return the occurrence containing the fields within the given {@link PbTextMessage}. */
  public @Nullable Occurrence getOccurrence(PbTextMessage message) {
    return elementOccurrences.get(message).stream().findFirst().orElse(null);
  }

  private void addAllOccurrences(PbOptionOwner optionOwner) {
    for (PbOptionExpression option : optionOwner.getOptions()) {
      PbOptionName name = option.getOptionName();
      Occurrence occurrence = addName(name);
      if (occurrence == null) {
        continue;
      }

      PbAggregateValue aggregateValue = option.getAggregateValue();
      if (aggregateValue != null) {
        addTextMessage(aggregateValue, occurrence);
      }
    }
  }

  private void addAllOccurrences(PbTextMessage message) {
    addTextMessage(message, root);
  }

  private Occurrence addName(PbOptionName name) {
    return addName(name, false);
  }

  private Occurrence addName(PbOptionName name, boolean merge) {
    PbOptionName qualifier = name.getQualifier();
    Occurrence occurrence;
    if (qualifier != null) {
      occurrence = addName(qualifier, true);
      if (occurrence == null) {
        return null;
      }
    } else {
      occurrence = root;
    }

    PbField field = resolveField(name);
    if (field == null) {
      return null;
    }

    // Qualifier names (e.g., "foo" and "bar" in "foo.bar.baz") are merged into the occurrence
    // (added only if they have not previously been added).
    //
    // This is because multiple qualified statements are merged together. e.g.,
    //   option (foo).bar.x = 1;
    //   option (foo).bar.y = 2;
    //   option (foo).baz = 3;
    //
    // In this example, messages are implicitly created for (foo) and (foo).bar, and the values
    // x, y, and baz are set appropriately.
    Occurrence nextOccurrence;
    if (merge) {
      nextOccurrence = occurrence.mergeOccurrence(field);
    } else {
      nextOccurrence = occurrence.addOccurrence(field);
    }

    elementOccurrences.put(name, nextOccurrence);
    return nextOccurrence;
  }

  // Register fields defined in an aggregate value.
  //
  // Note that the only way a sibling option statement can affect annotations within a text format
  // value is if it fulfills a required field. For example:
  //
  //   option (opt) = {
  //     foo: 1
  //   };
  //   option (opt).foo = 2;
  //
  // In this example, the duplicate annotation would be on (opt).foo, as it is the second
  // definition. And, another example:
  //
  //   option (opt).foo = 2;
  //   option (opt) = {
  //     foo: 1
  //   };
  //
  // In this example, the annotation is placed on the second (opt), as the entire opt field
  // re-definition is a duplicate and this invalid.
  //
  // Finally:
  //
  //   option (opt) = {
  //     foo: {
  //       bar: 1
  //     }
  //   };
  //   option (opt).foo.required = 2;
  //
  // In this example, the second option statement prevents a missing required field annotation from
  // being attached to "foo"
  private void addTextMessage(PbTextMessage message, Occurrence occurrence) {
    elementOccurrences.put(message, occurrence);
    for (PbTextField field : message.getFields()) {
      PbTextFieldName fieldName = field.getFieldName();
      PbTextExtensionName extensionName = fieldName.getExtensionName();
      if (extensionName != null && extensionName.isAnyTypeUrl()) {
        // Any fields are handled specially. Instead of adding a single field occurrence, we add
        // occurrences for each of the type_url and value members of the Any type. And we don't
        // recurse into the descendants of the Any.
        AnyType type = AnyType.forElement(message.getDeclaredMessage());
        if (type != null) {
          elementOccurrences.put(fieldName, occurrence.addOccurrence(type.getTypeUrlField()));
          elementOccurrences.put(fieldName, occurrence.addOccurrence(type.getValueField()));
        }
      } else {
        PbField declaredField = fieldName.getDeclaredField();
        if (declaredField == null) {
          continue;
        }
        for (PbTextElement element : field.getValues()) {
          Occurrence nextOccurrence = occurrence.addOccurrence(declaredField);
          elementOccurrences.put(fieldName, nextOccurrence);
          if (element instanceof PbTextMessage) {
            addTextMessage((PbTextMessage) element, nextOccurrence);
          }
        }
      }
    }
  }

  private static PbField resolveField(PbOptionName name) {
    return PbPsiUtil.resolveRefToType(name.getEffectiveReference(), PbField.class);
  }

  private static PbMessageType getFieldType(PbField field) {
    PbTypeName typeName = field.getTypeName();
    if (typeName == null) {
      return null;
    }
    return PbPsiUtil.resolveRefToType(typeName.getEffectiveReference(), PbMessageType.class);
  }

  /**
   * An Occurrence represents an instance of a message in an option setting context. An Occurrence
   * maintains a list of option fields and oneof usage that occur within it.
   *
   * <p>Scopes are necessary to support repeated and recursive option scenarios. For example:
   *
   * <pre>
   *   option (repeated) = {
   *     foo: abc
   *   };
   *   option (repeated) = {
   *     foo: abc
   *   };
   * </pre>
   *
   * In this example, the field "foo" is specified multiple times, but within distinct occurrences.
   * This usage is allowed. And a recursive example:
   *
   * <pre>
   *   option (opt) = {
   *     value: 1
   *     recurse {
   *       value: 1
   *       recurse {
   *         value: 1
   *       }
   *     }
   *   };
   * </pre>
   *
   * Each "recurse" instance corresponds to the same declared field, but defines a unique
   * occurrence. Thus, "value" can be set multiple times.
   */
  public static class Occurrence {
    private final Multimap<PbField, Occurrence> registeredFields = ArrayListMultimap.create();
    private final Map<PbOneofDefinition, PbField> registeredOneofFields = new HashMap<>();
    private final PbField field;
    private final PsiElement annotationElement;
    private final Occurrence parent;

    private Occurrence(PbField field, PsiElement annotationElement, Occurrence parent) {
      this.field = field;
      this.annotationElement = annotationElement;
      this.parent = parent;
    }

    /**
     * Attach errors with this occurrence to the given holder and element.
     *
     * <p>The following cases are annotated:
     *
     * <ul>
     *   <li>non-repeated fields specified multiple times
     *   <li>multiple members of a oneof specified
     *   <li>missing required fields
     *   <li>repeated message fields initialized without an aggregate value
     * </ul>
     *
     * @param holder the {@link AnnotationHolder}
     * @param annotationElement the element to which error annotations should be attached
     */
    public void annotate(AnnotationHolder holder, PsiElement annotationElement) {
      // The Multimap used to store occurrences for each field maintains insertion order. If
      // this occurrence is not the first in the list, then it's a duplicate. Only duplicates are
      // annotated.
      Occurrence first = parent.firstOccurrence(field);
      if (first != null && !first.equals(this) && !field.isRepeated()) {
        // Already set.
        holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("non.repeated.field.specified.multiple.times", field.getName()))
            .range(annotationElement)
            .create();
      } else {
        PbOneofDefinition oneof = field.getOneof();
        PbField previousOneofField = oneof != null ? parent.registeredOneofFields.get(oneof) : null;
        if (previousOneofField != null && !previousOneofField.equals(field)) {
          // Another field in the same oneof was already set.
          holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message(
              "multiple.oneof.fields.specified",
              field.getName(),
              previousOneofField.getName(),
              oneof.getName()))
              .range(annotationElement)
              .create();
        }
      }

      // Annotate missing required fields.
      annotateMissingRequiredFields(holder, annotationElement);
    }

    /**
     * Returns <code>true</code> if the given field can be used again within the scope of this
     * occurrence.
     *
     * @param field the field to test
     * @return <code>true</code> if the field can be used.
     */
    public boolean canFieldBeUsed(PbField field) {
      PbOneofDefinition oneof = field.getOneof();
      if (oneof != null && registeredOneofFields.containsKey(oneof)) {
        return false;
      }
      if (registeredFields.containsKey(field) && !field.isRepeated()) {
        return false;
      }
      return true;
    }

    /**
     * Returns <code>true</code> if the given field can be used or merged into an existing usage
     * within the scope of this occurrence.
     *
     * @param field the field to test
     * @return <code>true</code> if the field can be used or merged.
     */
    public boolean canFieldBeUsedOrMerged(PbField field) {
      PbOneofDefinition oneof = field.getOneof();
      if (oneof != null) {
        // The field can be merged if its the already-specified oneof field.
        PbField oneofField = registeredOneofFields.get(oneof);
        if (oneofField != null && !field.equals(oneofField)) {
          return false;
        }
      }
      if (registeredFields.containsKey(field) && !field.isRepeated()) {
        // We always return true for message fields, since they might be used as a namespace rather
        // than a type.
        PbTypeName typeName = field.getTypeName();
        if (typeName == null) {
          return false;
        }
        PbNamedTypeElement namedTypeElement =
            PbPsiUtil.resolveRefToType(typeName.getEffectiveReference(), PbNamedTypeElement.class);
        return namedTypeElement instanceof PbMessageType;
      }
      return true;
    }

    /**
     * Registers usage of the given field within this occurrence.
     *
     * <p>Annotations will be created at this time if:
     *
     * <ul>
     *   <li>the field is non-repeated and has already been used
     *   <li> the field is part of a oneof definition and another field from the same oneof has
     *       already been used
     * </ul>
     *
     * @param field the field to register
     * @return the newly-created {@link Occurrence} instance.
     */
    private Occurrence addOccurrence(PbField field) {
      PbOneofDefinition oneof = field.getOneof();
      if (oneof != null && !registeredOneofFields.containsKey(oneof)) {
        registeredOneofFields.put(oneof, field);
      }

      Occurrence occurrence = new Occurrence(field, annotationElement, this);
      registeredFields.put(field, occurrence);

      return occurrence;
    }

    /**
     * Merges usage of this field with any pre-existing occurrence.
     *
     * <p>If an occurrence already exists for the given field, it is returned. Else, a new
     * occurrence is created via {@link #addOccurrence(PbField)}.
     *
     * @param field the field to merge or register
     * @return the resulting occurrence, either already-existing or newly-created
     */
    private Occurrence mergeOccurrence(PbField field) {
      Occurrence occurrence = firstOccurrence(field);

      if (occurrence != null) {
        // We already have a occurrence for this field. Reuse it.
        return occurrence;
      } else {
        // Create a new occurrence.
        return addOccurrence(field);
      }
    }

    private void annotateMissingRequiredFields(
        AnnotationHolder holder, PsiElement annotationElement) {

      PbMessageType message = getFieldType(field);
      if (message == null) {
        return;
      }

      // Iterate through the message type's fields looking for required fields. If a required field
      // is not present in the occurrence's registeredFields collection, record it for an error
      // annotation.
      List<String> missingFieldNames =
          message
              .getSymbols(PbField.class)
              .stream()
              .filter(f -> f.isRequired() && !registeredFields.containsKey(f))
              .map(PbField::getName)
              .collect(Collectors.toList());

      if (!missingFieldNames.isEmpty()) {
        // Required field is missing.
        holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message(
            "missing.required.fields",
            message.getName(),
            String.join(", ", missingFieldNames)))
            .range(annotationElement)
            .create();
      }
    }

    private Occurrence firstOccurrence(PbField field) {
      return registeredFields.get(field).stream().findFirst().orElse(null);
    }
  }
}
