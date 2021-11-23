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
package com.intellij.protobuf.lang.resolve.directive;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.util.TextRange;
import com.intellij.protobuf.ide.PbCompositeModificationTracker;
import com.intellij.protobuf.lang.PbTextLanguage;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.psi.PbMessageType;
import com.intellij.protobuf.lang.resolve.PbSymbolResolver;
import com.intellij.protobuf.lang.resolve.directive.SchemaComment.Type;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates information held in optional text format schema directive comments.
 *
 * <p>Text format files can be optionally annotated with comments that instruct the IDE where to
 * find the proper protobuf message for validation. For example:
 *
 * <pre>
 *   # proto-file: net/proto/foo.proto
 *   # proto-message: MyMessage
 *   # proto-import: net/proto/baz.proto
 *
 *   my_message {
 *     foo: BAR
 *     [com.foo.baz]: 10
 *   }
 * </pre>
 *
 * <p>Semantics are as follows:
 *
 * <ul>
 *   <li>proto-file and proto-message are required. proto-import optionally adds additional paths
 *       for extension imports.
 *   <li>The filename is relative to the configured proto lookup path. For IntelliJ, this path is
 *       configured in language settings.
 *   <li>The message name follows standard protobuf scope rules, and is relative to the file's
 *       package name.
 * </ul>
 */
public class SchemaDirective {

  // Pattern that matches one of the proto-* comments:
  //   # proto-file: ...
  //   # proto-message: ...
  //   # proto-import: ...
  private static final Pattern commentPattern =
      Pattern.compile("^#\\s*(?<key>proto-(?<type>file|message|import)\\s*:)\\s*(?<name>.*)");

  private final FileComment fileComment;
  private final MessageComment messageComment;
  private final ImmutableList<FileComment> importComments;

  private SchemaDirective(
      FileComment fileComment,
      MessageComment messageComment,
      ImmutableList<FileComment> importComments) {
    this.fileComment = fileComment;
    this.messageComment = messageComment;
    this.importComments = importComments;
  }

  /** Returns the SchemaDirective for the given file, or <code>null</code> if one is not found. */
  @Nullable
  public static SchemaDirective find(PsiFile file) {
    return CachedValuesManager.getCachedValue(
        file, () -> Result.create(findNoCache(file), PbCompositeModificationTracker.byElement(file)));
  }

  private static SchemaDirective findNoCache(PsiFile file) {
    if (!PbTextLanguage.INSTANCE.is(file.getLanguage())) {
      return null;
    }

    FileComment fileComment = null;
    MessageComment messageComment = null;
    ImmutableList.Builder<FileComment> importCommentsBuilder = ImmutableList.builder();

    for (PsiComment comment : SyntaxTraverser.psiTraverser(file).filter(PsiComment.class)) {
      Matcher matcher = commentPattern.matcher(comment.getText());
      if (!matcher.matches()) {
        continue;
      }
      TextRange keyRange = TextRange.create(matcher.start("key"), matcher.end("key"));
      TextRange nameRange = null;
      if (!matcher.group("name").isEmpty()) {
        nameRange = TextRange.create(matcher.start("name"), matcher.end("name"));
      }

      String type = matcher.group("type");
      if ("file".equals(type)) {
        if (fileComment == null) {
          fileComment = new FileComment(comment, keyRange, nameRange, Type.FILE);
        }
      } else if ("message".equals(type)) {
        if (messageComment == null) {
          messageComment = new MessageComment(comment, keyRange, nameRange);
        }
      } else if ("import".equals(type)) {
        importCommentsBuilder.add(new FileComment(comment, keyRange, nameRange, Type.IMPORT));
      }
    }

    ImmutableList<FileComment> importComments = importCommentsBuilder.build();

    // If we didn't actually find any comments, return null.
    if (fileComment == null && messageComment == null && importComments.isEmpty()) {
      return null;
    }

    // Give the message comment a reference to the file comment.
    if (messageComment != null) {
      messageComment.setFileComment(fileComment);
    }

    return new SchemaDirective(fileComment, messageComment, importComments);
  }

  /** Returns the proto-file comment component. */
  @Nullable
  public SchemaComment getFileComment() {
    return fileComment;
  }

  /** Returns the proto-message comment component. */
  @Nullable
  public SchemaComment getMessageComment() {
    return messageComment;
  }

  /** Returns the (possibly empty) list proto-import comments. */
  @NotNull
  public ImmutableList<? extends SchemaComment> getImportComments() {
    return importComments;
  }

  /** Returns the {@link SchemaComment} for the given {@link PsiComment}. */
  @Nullable
  public SchemaComment getSchemaComment(PsiComment comment) {
    if (comment == null) {
      return null;
    }
    if (fileComment != null && comment.equals(fileComment.getComment())) {
      return fileComment;
    }
    if (messageComment != null && comment.equals(messageComment.getComment())) {
      return messageComment;
    }
    for (SchemaComment importComment : importComments) {
      if (comment.equals(importComment.getComment())) {
        return importComment;
      }
    }
    return null;
  }

  /** Returns the resolved {@link PbMessageType Message}, or <code>null</code>. */
  @Nullable
  public PbMessageType getMessage() {
    if (messageComment == null) {
      return null;
    }
    PsiReference reference = messageComment.getReference();
    if (reference == null) {
      return null;
    }
    PsiElement resolved = reference.resolve();
    if (resolved instanceof PbMessageType) {
      return (PbMessageType) resolved;
    }
    return null;
  }

  /**
   * Returns a {@link PbSymbolResolver} that searches exported symbols from the proto-file file and
   * all proto-import files.
   */
  PbSymbolResolver getExtensionResolver() {
    ImmutableList.Builder<PbFile> builder = ImmutableList.builder();
    addFileIfResolved(fileComment, builder);
    for (SchemaComment importComment : importComments) {
      addFileIfResolved(importComment, builder);
    }
    return PbSymbolResolver.forFileExports(builder.build());
  }

  /** Returns the filename portion of the directive, or <code>null</code> if it's missing. */
  public String getFilename() {
    if (fileComment == null) {
      return null;
    }
    return fileComment.getName();
  }

  /** Returns the message name portion of the directive, or <code>null</code> if it's missing. */
  public String getMessageName() {
    if (messageComment == null) {
      return null;
    }
    return messageComment.getName();
  }

  private void addFileIfResolved(SchemaComment comment, ImmutableList.Builder<PbFile> builder) {
    if (comment == null) {
      return;
    }
    PsiReference ref = comment.getReference();
    if (ref == null) {
      return;
    }
    PsiElement resolved = ref.resolve();
    if (resolved instanceof PbFile) {
      builder.add((PbFile) resolved);
    }
  }
}
