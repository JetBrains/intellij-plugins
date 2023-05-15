// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.debug;

import com.intellij.icons.AllIcons;
import com.intellij.javascript.debugger.JSDebuggerSupportUtils;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.resolve.JSImportHandlingUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.ExpressionInfo;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class FlexStackFrame extends XStackFrame {
  private static final String ANONYMOUS = "<anonymous>";

  private final FlexDebugProcess myDebugProcess;
  @Nullable private final XSourcePosition mySourcePosition;
  @Nullable private final String myFileNameIfSourcePositionIsNull;  // for presentation only
  private final int myLineIfSourcePositionIsNull; // for presentation only
  @NonNls static final String DELIM = " = ";

  private Map<String,String> qName2IdMap;
  private List<String> scopeChain;
  private final XDebuggerEvaluator myXDebuggerEvaluator = new FlexDebuggerEvaluator();
  private String myScope = UNKNOWN_SCOPE;
  private int myFrameIndex;
  @NonNls protected static final String UNKNOWN_SCOPE = "<unknown>";
  static final String CLASS_MARKER = ", class='";
  static final String CANNOT_EVALUATE_EXPRESSION = "Cannot evaluate expression: ";

  FlexStackFrame(final FlexDebugProcess debugProcess, final @Nullable XSourcePosition sourcePosition) {
    myDebugProcess = debugProcess;
    mySourcePosition = sourcePosition;
    myFileNameIfSourcePositionIsNull = null;
    myLineIfSourcePositionIsNull = -1;
  }

  /**
   * Use this constructor only if it is not possible to find existing VirtualFile and create corresponding XSourcePosition
   */
  FlexStackFrame(final FlexDebugProcess debugProcess, @Nullable final String fileName, final int line) {
    myDebugProcess = debugProcess;
    mySourcePosition = null;
    myFileNameIfSourcePositionIsNull = "<null>".equals(fileName) ? null : fileName;
    myLineIfSourcePositionIsNull = line;
  }

  @Override
  @Nullable
  public XSourcePosition getSourcePosition() {
    return mySourcePosition;
  }

  @Override
  public void computeChildren(@NotNull final XCompositeNode node) {
    List<DebuggerCommand> commands = new ArrayList<>();
    commands.add(new MyDebuggerCommand("print this", node, true, FlexValue.ValueType.This));
    commands.add(new MyDebuggerCommand("info arguments", node, false, FlexValue.ValueType.Parameter));
    commands.add(new MyDebuggerCommand("info locals", node, false, FlexValue.ValueType.Variable));
    //commands.add(new MyDebuggerCommand("info variables", node, false));

    if (mySourcePosition != null) {
      commands.add(new DebuggerCommand("does not matter", CommandOutputProcessingType.SPECIAL_PROCESSING) {
        @Override
        public void post(FlexDebugProcess flexDebugProcess) throws IOException {
          ensureQName2IdMapLoaded();
          final XValueChildrenList resultChildren = new XValueChildrenList(1);

          Boolean insideFunExpr = ReadAction.compute(() -> {
            Project project = getDebugProcess().getSession().getProject();
            PsiElement element =
              XDebuggerUtil.getInstance().findContextElement(mySourcePosition.getFile(), mySourcePosition.getOffset(), project, true);
            JSFunction function = PsiTreeUtil.getParentOfType(element, JSFunction.class);
            return function instanceof JSFunctionExpression;
          });

          if (Boolean.TRUE.equals(insideFunExpr)) {
            // public function outer(outerArg:String):Function {
            //     return function middle(middleArg:String):Function {
            //         return function inner(innerArg:String):String {
            //             return outerArg + middleArg + innerArg; // [BREAKPOINT]
            //         }
            //     }
            // }
            //
            // info scopechain gives following:
            // 0 = [Object 103989057, class='global']
            // 1 = [Object 101988361, class='Object']
            // 2 = [Object 101989657, class='<anonymous>']
            // 3 = [Object 103989057, class='global']
            // 4 = [Object 102001417, class='Object']
            // 5 = [Object 102001873, class='pack::HelloFlex4/outer']
            // 6 = [Object 106803361, class='pack::HelloFlex4']
            // 7 = [Object 105768929, class='pack::HelloFlex4$']
            // 8 = [Object 54750369, class='spark.components::Application$']
            // 9 = ...
            // Interesting for us: closures and one object after the last closure,
            // i.e. in this case #2, #5 and #6.

            scopeChain = new ArrayList<>(2);

            String firstTokenAfterLastClosure = null;
            for (String token : qName2IdMap.keySet()) {
              final int slashIndex = token.indexOf('/');
              if (slashIndex != -1 || token.contains(ANONYMOUS)) {
                final String funName = token.substring(slashIndex + 1);
                addScopeChainElement(token, funName, resultChildren);
              }
              else if (firstTokenAfterLastClosure == null && resultChildren.size() > 0) {
                firstTokenAfterLastClosure = token;
              }
            }

            if (firstTokenAfterLastClosure != null) {
              addScopeChainElement(firstTokenAfterLastClosure, firstTokenAfterLastClosure, resultChildren);
            }
          }
          node.addChildren(resultChildren, false);
        }

        @Override
        public String read(FlexDebugProcess flexDebugProcess) throws IOException {
          return "";
        }

        private void addScopeChainElement(final String token, final String funName, final XValueChildrenList resultChildren) {
          final String id = qName2IdMap.get(token);
          final String path = "#" + validObjectId(id);
          scopeChain.add(path);
          final String name = "Locals of " + funName;
          final String flexValueResult = "[Object " + id + CLASS_MARKER + token + "']";
          resultChildren.add(name, new FlexValue(FlexStackFrame.this, myDebugProcess, mySourcePosition, name, path, flexValueResult, null,
                                                 FlexValue.ValueType.ScopeChainEntry));
        }
      });
    }

    myDebugProcess.sendCommand(
      new CompositeDebuggerCommand(node, commands.toArray(new DebuggerCommand[0])) {
        @Override
        protected void obsolete() {
          super.obsolete();
          node.addChildren(XValueChildrenList.EMPTY, true);
        }

        @Override
        protected void succeeded() {
          super.succeeded();
          node.addChildren(XValueChildrenList.EMPTY, true);
        }
      }
    );
  }

  private String addFrameOffset(String text) {
    text="frame " + (myFrameIndex != 0 ? myFrameIndex: "")+ "\n"+text;
    return text;
  }

  @Override
  public XDebuggerEvaluator getEvaluator() {
    return myXDebuggerEvaluator;
  }

  public void setFrameIndex(final int frameIndex) {
    myFrameIndex = frameIndex;
  }

  private @NonNls String buildCommandForExpression(final String _expression) {
    if (_expression.indexOf('=') != -1) {
      String evalCommand = ReadAction.compute(() -> {
        final PsiFile fromText =
          PsiFileFactory.getInstance(myDebugProcess.getSession().getProject())
            .createFileFromText("A.js2", JavaScriptSupportLoader.ECMA_SCRIPT_L4, _expression);
        final PsiElement[] elements = fromText.getChildren();

        if (elements.length == 1 && elements[0] instanceof JSExpressionStatement) {
          final JSExpression expression = ((JSExpressionStatement)elements[0]).getExpression();

          if (expression instanceof JSAssignmentExpression expr) {
            final JSExpression lOperand = expr.getLOperand();
            final String lOperandText = lOperand == null ? null : lOperand.getText();
            final JSExpression rOperand = expr.getROperand();

            if (lOperandText != null && rOperand != null) {
              return addFrameOffset("set " + lOperandText + " = " + rOperand.getText() + "\nprint " + lOperandText);
            }
          }
        }
        return null;
      });

      if (evalCommand != null) return evalCommand;
    }

    return addFrameOffset("print " + _expression);
  }

  private void ensureQName2IdMapLoaded() {
    if (qName2IdMap != null) return;
    qName2IdMap = myDebugProcess.getQName2IdIfSameEqualityObject(getEqualityObject());

    if (qName2IdMap != null) return;
    qName2IdMap = new LinkedHashMap<>();
    final DebuggerCommand command = new DebuggerCommand("info scopechain", CommandOutputProcessingType.SPECIAL_PROCESSING) {
      @Override
      CommandOutputProcessingMode onTextAvailable(@NonNls final String s) {
        final StringTokenizer tokenizer = new StringTokenizer(s, "\r\n");
        while (tokenizer.hasMoreElements()) {
          String line = tokenizer.nextToken();
          // 1 = [Object 22610377, class='A$']
          int lBracketPos = line.indexOf('[');
          int rBracketPos = line.lastIndexOf(']');
          if (lBracketPos == -1 || rBracketPos == -1) continue;
          line = line.substring(lBracketPos + 1, rBracketPos);
          String id = line.substring(line.indexOf(' ') + 1, line.indexOf(','));
          String qName = line.substring(line.indexOf('\'') + 1, line.lastIndexOf('\''));
          qName = qName.replace("::",".");
          qName2IdMap.put(qName, id);
        }

        myDebugProcess.setQName2Id(qName2IdMap, getEqualityObject());
        return CommandOutputProcessingMode.DONE;
      }


    };
    myDebugProcess.sendAndProcessOneCommand(command, e -> {
      FlexDebugProcess.log(e);
      return null;
    });
  }

  class EvaluateCommand extends DebuggerCommand {
    private String result;
    private final XDebuggerEvaluator.XEvaluationCallback callback;
    private final String expression;
    private int responseCount;
    private boolean myFinished;

    EvaluateCommand(String _expression, final XDebuggerEvaluator.XEvaluationCallback _callback) {
      super(buildCommandForExpression(_expression), CommandOutputProcessingType.SPECIAL_PROCESSING);
      expression = _expression;
      callback = _callback;
    }

    @Override
    CommandOutputProcessingMode onTextAvailable(@NonNls String line) {
      if (myDebugProcess.filterStdResponse(line)) return CommandOutputProcessingMode.PROCEEDING;
      return proceedWithEvaluationResponse(line);
    }

    private CommandOutputProcessingMode proceedWithEvaluationResponse(String line) {
      ++responseCount;
      if (responseCount == 1) { // skip frame
        return CommandOutputProcessingMode.PROCEEDING;
      }
      return doOnTextAvailable(line);
    }

    CommandOutputProcessingMode doOnTextAvailable(@NonNls String s) {
      if (cannotEvaluateResponse(s) && mySourcePosition != null) {
        ensureQName2IdMapLoaded();
        evaluateFromTypeMap();
        return CommandOutputProcessingMode.DONE;
      }

      if (getText().contains("\n") && s.length() == 0) { // implicit set command was issued with empty result
        return CommandOutputProcessingMode.PROCEEDING;
      }
      dispatchResult(s);
      return CommandOutputProcessingMode.DONE;
    }

    private boolean cannotEvaluateResponse(String s) {
      return s.contains("could not be evaluated");
    }

    private void evaluateFromTypeMap() {
      assert mySourcePosition != null;

      final int dotPos = expression.indexOf('.');
      final String typeName = dotPos != -1 ? expression.substring(0, dotPos):expression;

      final String resolvedName = DumbService.getInstance(myDebugProcess.getSession().getProject()).runReadActionInSmartMode(() -> {
        final VirtualFile virtualFile = mySourcePosition.getFile();
        final PsiFile file = PsiManager.getInstance(myDebugProcess.getSession().getProject()).findFile(virtualFile);
        final int offset = mySourcePosition.getOffset();
        PsiElement element = file == null ? null : file.findElementAt(offset);

        if (file instanceof XmlFile) {
          final PsiLanguageInjectionHost psiLanguageInjectionHost =
            PsiTreeUtil.getParentOfType(element, PsiLanguageInjectionHost.class);

          if (psiLanguageInjectionHost != null) {
            final Ref<PsiElement> result = new Ref<>();
            InjectedLanguageManager.getInstance(file.getProject()).enumerate(psiLanguageInjectionHost, (injectedPsi, places) -> {
              final int injectedStart = InjectedLanguageUtil.getInjectedStart(places);
              result.set(injectedPsi.findElementAt(offset - injectedStart + (places.get(0).getPrefix().length())));
            });
            element = result.get();
          }
        }

        return element == null ? typeName : JSImportHandlingUtil.resolveTypeName(typeName, element);
      });

      boolean isGlobal = false;
      boolean handled = false;
      if (!resolvedName.equals(typeName) || (isGlobal = typeName.equals("global"))) {
        final String id = qName2IdMap.get(resolvedName + (isGlobal ? "":"$"));

        if (id != null) {
          handled = true;
          DebuggerCommand evaluateCommand = new EvaluateCommand("#"+id+(dotPos != -1 ?expression.substring(dotPos):""), callback) {
            @Override
            CommandOutputProcessingMode doOnTextAvailable(@NonNls final String s) {
              dispatchResult(s);
              return CommandOutputProcessingMode.DONE;
            }
          };
          myDebugProcess.sendAndProcessOneCommand(evaluateCommand, e -> {
            FlexDebugProcess.log(e);
            return null;
          });
        }
      } else if (scopeChain != null) {
          for(String id2:scopeChain) {
            final Ref<Boolean> resolved = new Ref<>();
            DebuggerCommand evaluateCommand = new EvaluateCommand(id2 + "." + expression, callback) {
              @Override
              CommandOutputProcessingMode doOnTextAvailable(@NonNls final String s) {
                if (!cannotEvaluateResponse(s)) {
                  resolved.set(Boolean.TRUE);
                  dispatchResult(s);
                }
                return CommandOutputProcessingMode.DONE;
              }
            };
            myDebugProcess.sendAndProcessOneCommand(evaluateCommand, e -> {
              FlexDebugProcess.log(e);
              return null;
            });
            if (resolved.get() == Boolean.TRUE) {
              handled = true;
              break;
            }
          }
        }

      if (!handled) {
        dispatchResult(CANNOT_EVALUATE_EXPRESSION + expression);
      }
    }

    protected void dispatchResult(String s) {
      final int i = s.indexOf(DELIM);
      if (i != -1) s = s.substring(i + DELIM.length());

      result = s.trim();

      if (callback != null) {
        ApplicationManager.getApplication().executeOnPooledThread(
          () -> callback.evaluated(new FlexValue(FlexStackFrame.this, myDebugProcess, mySourcePosition, expression, expression, result, null,
                                               FlexValue.ValueType.Other)));
      } else {
        synchronized (this) {
          myFinished = true;
          notify();
        }
      }
    }

    void waitTillExecutionEnd() {
      synchronized (this) {
        if (myFinished) return;
        try { while(true) { wait(); if (result != null) break;} }
        catch (InterruptedException ex) {
          FlexDebugProcess.log(ex);
        }
      }
    }
  }

  static String validObjectId(String s) {
    // some object ids from Flash player are negative (e.g. on Linux) and can not be consumed back e.g. for tracing
    // so we transform them into unsigned ones assuming there is just sign transmition problem (see IDEA-49837)
    long idVal = Long.parseLong(s);
    return s.charAt(0) == '-' ? Long.toString(idVal & 0xFFFFFFFFL) : Long.toString(idVal);
  }

  private class FlexDebuggerEvaluator extends XDebuggerEvaluator {
    @Override
    public boolean isCodeFragmentEvaluationSupported() {
      return false;
    }

    @Override
    public void evaluate(@NotNull final String expression, @NotNull final XEvaluationCallback callback, @Nullable XSourcePosition expressionPosition) {
      final EvaluateCommand command = new EvaluateCommand(expression, callback);
      myDebugProcess.sendCommand(command);
    }

    @Nullable
    @Override
    public ExpressionInfo getExpressionInfoAtOffset(@NotNull Project project, @NotNull Document document, final int offset, boolean sideEffectsAllowed) {
      return JSDebuggerSupportUtils.getExpressionAtOffset(project, document, offset);
    }
  }

  String eval(final String expression, FlexDebugProcess process) {
    final EvaluateCommand command = new EvaluateCommand(expression, null);
    process.sendAndProcessOneCommand(command, null);

    return command.result;
  }

  public FlexDebugProcess getDebugProcess() {
    return myDebugProcess;
  }

  @Override
  public void customizePresentation(@NotNull final ColoredTextContainer component) {
    component.append(myScope, SimpleTextAttributes.REGULAR_ATTRIBUTES);

    if (mySourcePosition != null) {
      component.append(" in ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
      component.append(mySourcePosition.getFile().getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
      component.append(":" + (mySourcePosition.getLine() + 1), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }
    else if (myFileNameIfSourcePositionIsNull != null) {
      component.append(" in ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
      component.append(myFileNameIfSourcePositionIsNull, SimpleTextAttributes.REGULAR_ATTRIBUTES);
      if (myLineIfSourcePositionIsNull >= 0) {
        component.append(":" + myLineIfSourcePositionIsNull, SimpleTextAttributes.REGULAR_ATTRIBUTES);
      }
    }

    component.setIcon(AllIcons.Debugger.Frame);
  }

  public void setScope(final String scope) {
    myScope = scope;
  }

  private String myQualifiedFunctionName;

  @Override
  public Object getEqualityObject() {
    if (myQualifiedFunctionName == null) {  // myScope is filled async

      if (mySourcePosition != null) {
        final FlexDebugProcess flexDebugProcess = getDebugProcess();
        final Project project = flexDebugProcess.getSession().getProject();
        final VirtualFile file = mySourcePosition.getFile();

        final JSFunction function = ApplicationManager.getApplication().runReadAction((NullableComputable<JSFunction>)() -> {
          final PsiElement element = XDebuggerUtil.getInstance().findContextElement(file, mySourcePosition.getOffset(), project, true);
          return PsiTreeUtil.getParentOfType(element, JSFunction.class);
        });

        String name;
        myQualifiedFunctionName = file.getPath() + (function != null ? "#" + ((name = function.getName()) != null ? name:function.getTextOffset()) :"");
      } else {
        myQualifiedFunctionName = "unknown";
      }
    }
    return myQualifiedFunctionName;
  }

  private class MyDebuggerCommand extends DebuggerCommand {
    private final boolean hasFrame;
    private final XValueChildrenList resultChildren;
    private int current;
    private final XCompositeNode myNode;
    private final FlexValue.ValueType myValueType;

    MyDebuggerCommand(String text, XCompositeNode node, boolean _hasFrame, FlexValue.ValueType valueType) {
      super(_hasFrame ? addFrameOffset(text):text, CommandOutputProcessingType.SPECIAL_PROCESSING);
      myNode = node;
      resultChildren = new XValueChildrenList(3);
      hasFrame = _hasFrame;
      myValueType = valueType;
    }

    @Override
    CommandOutputProcessingMode onTextAvailable(@NonNls final String s) {
      final int offsetIndex = hasFrame ? 1:0; // frame command
      if (current >= offsetIndex) {
        final StringTokenizer tokenizer = new StringTokenizer(s, "\r\n", true);
        Pair<String, StringBuilder> previousNameAndValue = null; // may be incomplete if value contains new line symbols

        while (tokenizer.hasMoreElements()) {
          final String token = tokenizer.nextToken();
          if (token.length() == 0) continue;

          if (token.charAt(0) == '\r' || token.charAt(0) == '\n') {
            // Tokenizer delimiter may be a part of String variable value
            if (previousNameAndValue != null) {
              previousNameAndValue.second.append(token);
            }
            else {
              FlexDebugProcess.log("Unexpected token: [" + token + "], full string: [" + s + "]");
            }
            continue;
          }

          final int i = token.indexOf(DELIM);
          if (i == -1) {
            if (previousNameAndValue != null) {
              previousNameAndValue.second.append(token);
            }
            else {
              FlexDebugProcess.log("Unexpected token: [" + token + "], full string: [" + s + "]");
            }
            continue;
          }

          @NonNls String name = token.substring(0, i);
          if (name.startsWith("$")) {   // $x is legal variable name, this evaluation is $1
            boolean completeDigits = name.length() > 1;
            for (int j = 1; j < name.length(); ++j) {
              completeDigits = Character.isDigit(name.charAt(j));
              if (!completeDigits) break;
            }
            if (completeDigits) name = "this";
          }

          if (previousNameAndValue != null) {
            String prevName = previousNameAndValue.first;
            resultChildren.add(prevName, new FlexValue(FlexStackFrame.this, myDebugProcess, mySourcePosition, prevName, prevName,
                                                       removeTrailingNewLines(previousNameAndValue.second), null, myValueType));
          }

          previousNameAndValue = Pair.create(name, new StringBuilder(token.substring(i + DELIM.length())));
        }

        if (previousNameAndValue != null) {
          String prevName = previousNameAndValue.first;
          resultChildren.add(prevName, new FlexValue(FlexStackFrame.this, myDebugProcess, mySourcePosition, prevName, prevName,
                                                     removeTrailingNewLines(previousNameAndValue.second), null, myValueType));
        }
      }

      ++current;

      if (current == offsetIndex + 1) {
        myNode.addChildren(resultChildren, false);
        return CommandOutputProcessingMode.DONE;
      } else {
        return CommandOutputProcessingMode.PROCEEDING;
      }
    }

    private String removeTrailingNewLines(final StringBuilder builder) {
      while (builder.length() > 0 && ((builder.charAt(builder.length() - 1) == '\r') || builder.charAt(builder.length() - 1) == '\n')) {
        builder.deleteCharAt(builder.length() - 1);
      }
      return builder.toString();
    }
  }
}
