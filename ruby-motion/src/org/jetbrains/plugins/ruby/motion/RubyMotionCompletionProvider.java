/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.ruby.motion;

import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.TemplateBuilder;
import com.intellij.codeInsight.template.TemplateBuilderFactory;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Function;
import org.jetbrains.plugins.ruby.motion.symbols.FunctionSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.MethodInsertHandlerCreator;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyAbstractMethodInsertHandler;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyCompletionProvider;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyLookupElement;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.fqn.FQN;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.settings.RubyCodeStyleSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionCompletionProvider extends RubyCompletionProvider {
  @Nullable
  @Override
  public LookupElement createLookupItem(@NotNull Symbol symbol,
                                        String name,
                                        boolean bold,
                                        @Nullable Symbol typeSourceSymbol,
                                        @Nullable RType originalType,
                                        boolean isInsertHandlerCanBeApplied,
                                        @Nullable MethodInsertHandlerCreator insertHandler,
                                        @Nullable FQN symbolFQN) {
    if (symbol instanceof FunctionSymbol) {
      return createLookupItem((FunctionSymbol)symbol, bold, isInsertHandlerCanBeApplied);
    }
    return null;
  }

  private static LookupElement createLookupItem(@NotNull final FunctionSymbol symbol,
                                                final boolean bold,
                                                final boolean isInsertHandlerCanBeApplied) {
    final Symbol parent = symbol.getParentSymbol();
    if (parent == null || !isInsertHandlerCanBeApplied) return null;
    final Function function = symbol.getFunction();
    if (function.getArguments().size() < 2 || !function.getName().contains(":")) return null;

    return new RubyLookupElement(function.getName(), "", parent.getFQNWithNesting().getFullPath(), bold, AllIcons.Nodes.Method,
                                 null, new SelectorInsertHandler(function));
  }

  private static class SelectorInsertHandler extends RubyAbstractMethodInsertHandler {
    private final Function myFunction;

    SelectorInsertHandler(Function function) {
      super(null);
      myFunction = function;
    }

    @Override
    public void handleInsertMethodSignature(final InsertionContext context, final RPsiElement method, final String lookupItem) {
      final String[] split = myFunction.getName().split(":");
      final Document document = context.getDocument();
      final int initialOffset = context.getStartOffset();
      int offset = initialOffset;
      document.deleteString(offset, context.getTailOffset());
      document.insertString(offset, split[0]);
      offset += split[0].length();

      final boolean insertParentheses = CodeStyle.getCustomSettings(context.getFile(), RubyCodeStyleSettings.class).PARENTHESES_AROUND_METHOD_ARGUMENTS;
      final List<TextRange> argRanges = new ArrayList<>(split.length);
      document.insertString(offset, insertParentheses ? "(" : " ");
      offset++;
      int offsetBefore = offset;
      offset += insertArg(document, offset, 0);
      argRanges.add(new TextRange(offsetBefore, offset));
      for (int i = 1; i < split.length; i++) {
        document.insertString(offset, ", ");
        offset += 2;
        final String argName = split[i];
        document.insertString(offset, argName + ": ");
        offset += argName.length() + 2;
        offsetBefore = offset;
        offset += insertArg(document, offset, i);
        argRanges.add(new TextRange(offsetBefore, offset));
      }
      document.insertString(offset, insertParentheses ? ")" : "");
      PsiDocumentManager.getInstance(context.getProject()).commitDocument(document);

      if (ApplicationManager.getApplication().isUnitTestMode()) return;

      final PsiElement element = context.getFile().findElementAt(initialOffset);
      final RCall call = PsiTreeUtil.getParentOfType(element, RCall.class);
      final TemplateBuilder builder = TemplateBuilderFactory.getInstance().createTemplateBuilder(call);
      for (int i = 0; i < argRanges.size(); i++) {
        TextRange range = argRanges.get(i);
        builder.replaceRange(range.shiftRight(-call.getTextOffset()), myFunction.getArguments().get(i).first);
      }
      builder.run(context.getEditor(), false);
    }

    private int insertArg(Document document, int offset, int i) {
      final String arg = myFunction.getArguments().get(i).first;
      document.insertString(offset, arg);
      return arg.length();
    }
  }
}
