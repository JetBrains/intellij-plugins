package com.intellij.lang.javascript.flex.debug;

import com.intellij.javascript.JSDebuggerSupportUtils;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.resolve.JSImportHandlingUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Function;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.*;
import com.intellij.xdebugger.ui.DebuggerIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

/**
 * @author nik
 */
public class FlexStackFrame extends XStackFrame {
  private static final String ANONYMOUS = "<anonymous>";

  private final FlexDebugProcess myDebugProcess;
  private final XSourcePosition mySourcePosition;
  @NonNls static final String DELIM = " = ";

  private Map<String,String> qName2IdMap;
  private List<String> scopeChain;
  private final XDebuggerEvaluator myXDebuggerEvaluator = new FlexDebuggerEvaluator();
  private String myScope = UNKNOWN_SCOPE;
  private int myFrameIndex;
  @NonNls protected static final String UNKNOWN_SCOPE = "<unknown>";
  static final String CLASS_MARKER = ", class='";
  private static final String CANNOT_EVALUATE_EXPRESSION = "Cannot evaluate expression: ";

  FlexStackFrame(FlexDebugProcess debugProcess, final XSourcePosition sourcePosition) {
    myDebugProcess = debugProcess;
    mySourcePosition = sourcePosition;
  }

  public XSourcePosition getSourcePosition() {
    return mySourcePosition;
  }

  @Override
  public void computeChildren(@NotNull final XCompositeNode node) {
    List<DebuggerCommand> commands = new ArrayList<DebuggerCommand>();
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

          Boolean insideFunExpr = ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
            public Boolean compute() {
              Project project = getDebugProcess().getSession().getProject();
              PsiElement element =
                JSDebuggerSupportUtils.getContextElement(mySourcePosition.getFile(), mySourcePosition.getOffset(), project);
              JSFunction function = PsiTreeUtil.getParentOfType(element, JSFunction.class);
              return function instanceof JSFunctionExpression;
            }
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

            scopeChain = new ArrayList<String>(2);

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
      new CompositeDebuggerCommand(node, commands.toArray(new DebuggerCommand[commands.size()])) {
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
      String evalCommand = ApplicationManager.getApplication().runReadAction(new Computable<String>() {
        public String compute() {
          final PsiFile fromText =
            PsiFileFactory.getInstance(myDebugProcess.getSession().getProject()).createFileFromText("A.js2", _expression);
          final PsiElement[] elements = fromText.getChildren();

          if (elements.length == 1 && elements[0] instanceof JSExpressionStatement) {
            final JSExpression expression = ((JSExpressionStatement)elements[0]).getExpression();

            if (expression instanceof JSAssignmentExpression) {
              JSAssignmentExpression expr = (JSAssignmentExpression)expression;
              final JSExpression lOperand = expr.getLOperand();
              final String lOperandText = lOperand == null ? null : lOperand.getText();
              final JSExpression rOperand = expr.getROperand();

              if (lOperandText != null && rOperand != null) {
                return addFrameOffset("set " + lOperandText + " = " + rOperand.getText() + "\nprint " + lOperandText);
              }
            }
          }
          return null;
        }
      });

      if (evalCommand != null) return evalCommand;
    }

    return addFrameOffset("print " + _expression);
  }

  private void ensureQName2IdMapLoaded() {
    if (qName2IdMap != null) return;
    qName2IdMap = myDebugProcess.getQName2IdIfSameEqualityObject(getEqualityObject());

    if (qName2IdMap != null) return;
    qName2IdMap = new LinkedHashMap<String, String>();
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
    myDebugProcess.sendAndProcessOneCommand(command, new Function<Exception, Void>() {
      public Void fun(final Exception e) {
        FlexDebugProcess.log(e);
        return null;
      }
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
      final int dotPos = expression.indexOf('.');
      final String typeName = dotPos != -1 ? expression.substring(0, dotPos):expression;

      final String resolvedName = ApplicationManager.getApplication().runReadAction(new Computable<String>() {
        public String compute() {
          final VirtualFile virtualFile = mySourcePosition.getFile();
          final PsiFile file = PsiManager.getInstance(myDebugProcess.getSession().getProject()).findFile(virtualFile);
          final int offset = mySourcePosition.getOffset();
          PsiElement element = file == null ? null : file.findElementAt(offset);

          if (file instanceof XmlFile) {
            final PsiLanguageInjectionHost psiLanguageInjectionHost =
                PsiTreeUtil.getParentOfType(element, PsiLanguageInjectionHost.class);

            if (psiLanguageInjectionHost != null) {
              final Ref<PsiElement> result = new Ref<PsiElement>();
              psiLanguageInjectionHost.processInjectedPsi(new PsiLanguageInjectionHost.InjectedPsiVisitor() {
                public void visit(@NotNull final PsiFile injectedPsi, @NotNull final List<PsiLanguageInjectionHost.Shred> places) {
                  final PsiLanguageInjectionHost.Shred shred = places.get(0);
                  final int injectedStart = shred.host.getTextOffset() + shred.getRangeInsideHost().getStartOffset();
                  final PsiElement value = injectedPsi.findElementAt(
                      offset - injectedStart + (shred.prefix != null ? shred.prefix.length() : 0));
                  result.set(value);
                }
              });
              element = result.get();
            }
          }

          return element == null ? typeName : JSImportHandlingUtil.resolveTypeName(typeName, element);

        }
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
          myDebugProcess.sendAndProcessOneCommand(evaluateCommand, new Function<Exception, Void>() {
            public Void fun(final Exception e) {
              FlexDebugProcess.log(e);
              return null;
            }
          });
        }
      } else if (scopeChain != null) {
          for(String id2:scopeChain) {
            final Ref<Boolean> resolved = new Ref<Boolean>();
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
            myDebugProcess.sendAndProcessOneCommand(evaluateCommand, new Function<Exception, Void>() {
              public Void fun(final Exception e) {
                FlexDebugProcess.log(e);
                return null;
              }
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
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
          public void run() {
            callback.evaluated(new FlexValue(FlexStackFrame.this, myDebugProcess, mySourcePosition, expression, expression, result, null,
                                             FlexValue.ValueType.Other));
          }
        });
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
          return;
        }
      }
    }
  }

  static String validObjectId(String s) {
    // some object ids from Flash player are negative (e.g. on Linux) and can not be consumed back e.g. for tracing
    // so we transform them into unsigned ones assuming there is just sign transmition problem (see IDEADEV-39082)
    long idVal = Long.parseLong(s);
    return Long.toString(idVal & 0xFFFFFFFFL);
  }

  private class FlexDebuggerEvaluator extends XDebuggerEvaluator {

    public boolean evaluateCondition(@NotNull final String expression) {
      final String result = eval(expression, myDebugProcess);

      if (result.equalsIgnoreCase("true") || result.equalsIgnoreCase("false")) {
        return Boolean.valueOf(result);
      }
      else {
        final String message = result.startsWith(CANNOT_EVALUATE_EXPRESSION)
                               ? FlexBundle.message("failed.to.evaluate.breakpoint.condition", expression)
                               : FlexBundle.message("not.boolean.breakpoint.condition", expression, result);
        final Ref<Boolean> stopRef = new Ref<Boolean>(false);

        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
          public void run() {
            final Project project = getDebugProcess().getSession().getProject();
            final int answer =
              Messages.showYesNoDialog(project, message, FlexBundle.message("breakpoint.condition.error"), Messages.getQuestionIcon());
            stopRef.set(answer == 0);
          }
        }, ModalityState.defaultModalityState());

        return stopRef.get();
      }
    }

    public String evaluateMessage(@NotNull final String expression) {
      return eval(expression, myDebugProcess);
    }

    public void evaluate(@NotNull final String expression, final XEvaluationCallback callback, @Nullable XSourcePosition expressionPosition) {
      final EvaluateCommand command = new EvaluateCommand(expression, callback);
      myDebugProcess.sendCommand(command);
    }

    public TextRange getExpressionRangeAtOffset(final Project project, final Document document, final int offset, boolean sideEffectsAllowed) {
      return JSDebuggerSupportUtils.getExpressionAtOffset(project, document, offset);
    }
  }

  private String eval(final String expression, FlexDebugProcess process) {
    final EvaluateCommand command = new EvaluateCommand(expression, null);
    process.sendAndProcessOneCommand(command, null);

    return command.result;
  }

  public FlexDebugProcess getDebugProcess() {
    return myDebugProcess;
  }

  @Override
  public void customizePresentation(final SimpleColoredComponent component) {
    XSourcePosition position = getSourcePosition();
    component.append(myScope, SimpleTextAttributes.REGULAR_ATTRIBUTES);
    component.append(" in ", SimpleTextAttributes.REGULAR_ATTRIBUTES);

    if (position != null) {
      component.append(position.getFile().getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
      component.append(":" + (position.getLine() + 1), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }
    else {
      component.append("<file name is not available>", SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }
    component.setIcon(DebuggerIcons.STACK_FRAME_ICON);
  }

  public void setScope(final String scope) {
    myScope = scope;
  }

  private String myQualifiedFunctionName;

  public Object getEqualityObject() {
    if (myQualifiedFunctionName == null) {  // myScope is filled async
      final XSourcePosition position = getSourcePosition();

      if (position != null) {
        final FlexDebugProcess flexDebugProcess = getDebugProcess();
        final Project project = flexDebugProcess.getSession().getProject();
        final VirtualFile file = position.getFile();

        final JSFunction function = ApplicationManager.getApplication().runReadAction(new NullableComputable<JSFunction>() {
          public JSFunction compute() {
            final PsiElement element = JSDebuggerSupportUtils.getContextElement(file, position.getOffset(), project);
            return PsiTreeUtil.getParentOfType(element, JSFunction.class);
          }
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

    public MyDebuggerCommand(String text, XCompositeNode node, boolean _hasFrame, FlexValue.ValueType valueType) {
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
              completeDigits &= Character.isDigit(name.charAt(j));
              if (!completeDigits) break;
            }
            if (completeDigits) name = "this";
          }

          if (previousNameAndValue != null) {
            String prevName = previousNameAndValue.first;
            resultChildren.add(prevName, new FlexValue(FlexStackFrame.this, myDebugProcess, mySourcePosition, prevName, prevName,
                                                       removeTrailingNewLines(previousNameAndValue.second), null, myValueType));
          }

          previousNameAndValue = new Pair<String, StringBuilder>(name, new StringBuilder(token.substring(i + DELIM.length())));
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
