package org.jetbrains.plugins.ruby.motion;

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
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Function;
import org.jetbrains.plugins.ruby.motion.symbols.FunctionSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.MethodInsertHandlerCreator;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyAbstractMethodInsertHandler;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyCompletionProvider;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyLookupElement;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;

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
                                        @Nullable String symbolFQN) {
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

    return new RubyLookupElement(function.getName(), "", SymbolUtil.getSymbolFullQualifiedName(parent), bold, AllIcons.Nodes.Method,
                                 null, new SelectorInsertHandler(function));
  }

  private static class SelectorInsertHandler extends RubyAbstractMethodInsertHandler {
    private final Function myFunction;

    public SelectorInsertHandler(Function function) {
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

      final boolean insertParentheses = CodeStyleSettingsManager.getSettings(context.getProject()).PARENTHESES_AROUND_METHOD_ARGUMENTS;
      final List<TextRange> argRanges = new ArrayList<TextRange>(split.length);
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
