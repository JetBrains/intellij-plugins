package com.intellij.lang.javascript.flex.debug;

import com.intellij.javascript.JSDebuggerSupportUtils;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.JSImportHandlingUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Function;
import com.intellij.util.Icons;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.*;
import com.intellij.xdebugger.ui.DebuggerIcons;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

/**
 * @author nik
 */
public class FlexStackFrame extends XStackFrame {
  private static final String ANONYMOUS = "<anonymous>";

  private final FlexDebugProcess myDebugProcess;
  private final XSourcePosition mySourcePosition;
  @NonNls private static final String DELIM = " = ";

  private Map<String,String> qName2IdMap;
  private List<String> scopeChain;
  private final XDebuggerEvaluator myXDebuggerEvaluator = new FlexDebuggerEvaluator();
  @NonNls private static final String OBJECT_MARKER = "Object ";
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
    commands.add(new MyDebuggerCommand("print this", node, true, ValueType.This));
    commands.add(new MyDebuggerCommand("info arguments", node, false, ValueType.Parameter));
    commands.add(new MyDebuggerCommand("info locals", node, false, ValueType.Variable));
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
          resultChildren.add(name, new FlexValue(name, path, flexValueResult, null, ValueType.ScopeChainEntry));
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

      if (getText().indexOf("\n") != -1 && s.length() == 0) { // implicit set command was issued with empty result
        return CommandOutputProcessingMode.PROCEEDING;
      }
      dispatchResult(s);
      return CommandOutputProcessingMode.DONE;
    }

    private boolean cannotEvaluateResponse(String s) {
      return s.indexOf("could not be evaluated") != -1;
    }

    private void evaluateFromTypeMap() {
      final int dotPos = expression.indexOf('.');
      final String typeName = dotPos != -1 ? expression.substring(0, dotPos):expression;

      final String resolvedName = ApplicationManager.getApplication().runReadAction(new Computable<String>() {
        public String compute() {
          final VirtualFile virtualFile = mySourcePosition.getFile();
          final PsiFile file = PsiManager.getInstance(myDebugProcess.getSession().getProject()).findFile(virtualFile);
          final int offset = mySourcePosition.getOffset();
          PsiElement element = file.findElementAt(offset);

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
            callback.evaluated(new FlexValue(expression, expression, result, null, ValueType.Other));
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

  private static final Comparator<XValue> myArrayElementsComparator = new Comparator<XValue>() {
    public int compare(XValue o1, XValue o2) {
      if (o1 instanceof FlexValue && o2 instanceof FlexValue) {
        String name = ((FlexValue)o1).myName;
        String name2 = ((FlexValue)o2).myName;

        if (!StringUtil.isEmpty(name) &&
            !StringUtil.isEmpty(name2) &&
            Character.isDigit(name.charAt(0)) &&
            Character.isDigit(name2.charAt(0))
           ) {
          try {
            return Integer.parseInt(name) - Integer.parseInt(name2);
          } catch (NumberFormatException ex) {}
        }

        if (name == null) {
          return name2 == null ? 0 : -1;
        } else if (name2 == null) {
          return 1;
        }
        return name.compareToIgnoreCase(name2);
      }
      return 1;
    }
  };

  private static enum ValueType {
    This(Icons.CLASS_ICON),
    Parameter(Icons.PARAMETER_ICON),
    Variable(Icons.VARIABLE_ICON),
    Field(Icons.FIELD_ICON),
    ScopeChainEntry(Icons.CLASS_INITIALIZER),
    Other(null);

    private @Nullable final Icon myIcon;

    private ValueType(final @Nullable Icon icon) {
      myIcon = icon;
    }
  }

  class FlexValue extends XValue {

    private final String myName;
    private final String myExpression;
    private final String myResult;
    private @Nullable final String myParentResult;
    private final ValueType myValueType;

    private static final int MAX_STRING_LENGTH_TO_SHOW = XValueNode.MAX_VALUE_LENGTH;
    static final String TEXT_MARKER = " text ";
    static final String ELEMENT_MARKER = " element ";
    private static final String ESCAPE_START = "IDEA-ESCAPE-START";
    private static final String ESCAPE_END = "IDEA-ESCAPE-END";

    FlexValue(String _name, String _expression, String _result, @Nullable String _parentResult, @NotNull ValueType _valueType) {
      myName = _name;
      myExpression = _expression;
      myResult = unescape(_result);
      myParentResult = _parentResult;
      myValueType = _valueType;
    }

    String getResult() {
      return myResult;
    }

    /**
     * Looks for IDEA-ESCAPE-START and IDEA-ESCAPE-END markers in input string and unescapes symbols inside these markers. Markers are removed.
     */
    private String unescape(String str) {
      int escapeEndIndex = 0;
      int escapeStartIndex;

      while ((escapeStartIndex = str.indexOf(ESCAPE_START, escapeEndIndex - ESCAPE_START.length())) > -1) {
        escapeEndIndex = str.indexOf(ESCAPE_END, escapeStartIndex);
        if (escapeEndIndex < 0) {
          escapeEndIndex = str.length();
        }
        str = str.substring(0, escapeStartIndex) +
              StringUtil.unescapeStringCharacters(str.substring(escapeStartIndex + ESCAPE_START.length(), escapeEndIndex)) +
              (escapeEndIndex + ESCAPE_END.length() <= str.length() ? str.substring(escapeEndIndex + ESCAPE_END.length()) : "");
      }
      return str;
    }

    public void computePresentation(@NotNull final XValueNode node) {
      final boolean isObject = myResult.indexOf(OBJECT_MARKER) != -1;
      String val;
      String type = null;
      String additionalInfo = null;

      if (isObject) {
        val = myResult;
        final Pair<String, String> classNameAndAdditionalInfo = getTypeAndAdditionalInfo(myResult);
        type = classNameAndAdditionalInfo.first;
        additionalInfo = classNameAndAdditionalInfo.second;

        if (type != null) {
          val = "[".concat(getObjectId(myResult, myResult.indexOf(OBJECT_MARKER), OBJECT_MARKER)).concat("]");
        }
      }
      else {
        val = myResult;
      }

      if (("XML".equals(type) || "XMLList".equals(type)) && myExpression.indexOf('=') == -1) {
        if (myDebugProcess.isDebuggerFromSdk4()) {
          final String finalType = type;
          final EvaluateCommand command = new EvaluateCommand(myExpression + ".toXMLString()", new XDebuggerEvaluator.XEvaluationCallback() {
            public void evaluated(@NotNull XValue result) {
              setResult(((FlexValue)result).myResult, node, finalType, isObject);
            }

            public void errorOccurred(@NotNull String errorMessage) {
              setResult(errorMessage, node, finalType, isObject);
            }

            private void setResult(String s, XValueNode node, String finalType, boolean b) {
              if (!node.isObsolete()) {
                s = setFullValueEvaluatorIfNeeded(node, s, true);
                node.setPresentation(myValueType.myIcon, finalType, s, b);
              }
            }
          });
          myDebugProcess.addPendingCommand(new CompositeDebuggerCommand(node, command), 700);
          return;
        }

        else if (myDebugProcess.isDebuggerFromSdk3()) {
          if ("XMLList".equals(type)) {
            node.setFullValueEvaluator(new XFullValueEvaluator(FlexBundle.message("debugger.show.full.value")) {
              public void startEvaluation(@NotNull XFullValueEvaluationCallback callback) {
                new XmlObjectEvaluator(FlexValue.this, callback).startEvaluation();
              }
            });

            node.setPresentation(myValueType.myIcon, type, val.concat(" "), isObject);
            return;
          }
          else if (additionalInfo != null) {
            /*
              additionalInfo may look like following:
              "text element content"
              "element <root attr=\"attrValue\">"
              "element <child/>"
            */
            final boolean isElement = additionalInfo.startsWith(ELEMENT_MARKER + "<") && additionalInfo.endsWith(">");
            final boolean isEmptyElement = isElement && additionalInfo.endsWith("/>");
            final boolean isText = !isElement && additionalInfo.startsWith(TEXT_MARKER);

            if (isText || isElement) {
              String textToShow;

              if (isText) {
                textToShow = additionalInfo.substring(TEXT_MARKER.length());
              }
              else if (isEmptyElement) {
                textToShow = additionalInfo.substring(ELEMENT_MARKER.length());
              }
              else {
                final String startTag = additionalInfo.substring(ELEMENT_MARKER.length());

                final int spaceIndex = startTag.indexOf(" ");
                final String tagName = startTag.substring(1, spaceIndex > 0 ? spaceIndex : startTag.length() - 1);
                textToShow = startTag + "..." + "</" + tagName + "> ";
                if (textToShow.length() > MAX_STRING_LENGTH_TO_SHOW) {
                  textToShow = textToShow.substring(0, MAX_STRING_LENGTH_TO_SHOW).concat("... ");
                }

                node.setFullValueEvaluator(new XFullValueEvaluator(FlexBundle.message("debugger.show.full.value")) {
                  public void startEvaluation(@NotNull XFullValueEvaluationCallback callback) {
                    new XmlObjectEvaluator(FlexValue.this, callback).startEvaluation();
                  }
                });

                node.setPresentation(myValueType.myIcon, type, textToShow, isObject);
                return;
              }

              textToShow = setFullValueEvaluatorIfNeeded(node, textToShow, true);
              node.setPresentation(myValueType.myIcon, type, textToShow, isObject);
              return;
            }

            val = setFullValueEvaluatorIfNeeded(node, additionalInfo, true);
            node.setPresentation(myValueType.myIcon, type, val, isObject);
            return;
          }
        }
      }

      val = setFullValueEvaluatorIfNeeded(node, val, false);
      node.setPresentation(myValueType.myIcon, type, val, isObject);
    }

    private String setFullValueEvaluatorIfNeeded(final XValueNode node, String value, final boolean isXml) {
      final String fullValue = value;

      final int lfIndex = fullValue.indexOf('\n');
      final int crIndex = fullValue.indexOf('\r');

      if (fullValue.length() > MAX_STRING_LENGTH_TO_SHOW ||
          lfIndex > -1 && lfIndex < fullValue.length() - 1 ||
          crIndex > -1 && crIndex < fullValue.length() - 1) {

        final boolean quoted = fullValue.charAt(0) == '\'' && fullValue.charAt(fullValue.length() - 1) == '\'';
        final boolean doubleQuoted = fullValue.charAt(0) == '\"' && fullValue.charAt(fullValue.length() - 1) == '\"';

        if (value.length() > MAX_STRING_LENGTH_TO_SHOW) {
          final String ending = doubleQuoted ? "\" " : quoted ? "\' " : " ";
          value = value.substring(0, MAX_STRING_LENGTH_TO_SHOW).concat("...").concat(ending);
        }
        else if (!value.endsWith(" ")) {
          value = value.concat(" ");  // just a separator between text value and hyperlink
        }

        final String unquoted = quoted || doubleQuoted ? fullValue.substring(1, fullValue.length() - 1) : fullValue;
        node.setFullValueEvaluator(
          new XFullValueEvaluator(FlexBundle.message("debugger.show.full.value")) {
            public void startEvaluation(@NotNull XFullValueEvaluationCallback callback) {
              callback.evaluated(unquoted, isXml ? XmlObjectEvaluator.MONOSPACED_FONT : null);
            }
          });
      }

      return value;
    }

    @Override
    public String getEvaluationExpression() {
      return myExpression;
    }

    @Override
     public XValueModifier getModifier() {
      return new XValueModifier() {
        @Override
        public void setValue(@NotNull String _expression, @NotNull final XModificationCallback callback) {
          EvaluateCommand command = new EvaluateCommand(myExpression + "=" + _expression, null) {
            protected void dispatchResult(String s) {
              super.dispatchResult(s);
              callback.valueModified();
            }
          };
          myDebugProcess.sendCommand(command);
        }
      };
    }

    @Override
    public void computeChildren(@NotNull final XCompositeNode node) {
      final int i = myResult.indexOf(OBJECT_MARKER);
      if (i == -1) super.computeChildren(node);

      final String type = getTypeAndAdditionalInfo(myResult).first;

      final EvaluateCommand command = new EvaluateCommand(referenceObjectBase(i, OBJECT_MARKER), null) {
        @Override
        CommandOutputProcessingMode doOnTextAvailable(@NonNls final String resultS) {
          StringTokenizer tokenizer = new StringTokenizer(resultS, "\r\n");

          tokenizer
            .nextToken(); // skip first token; it contains $-prefix followed by myResult: $6 = [Object 30860193, class='__AS3__.vec::Vector.<String>']
          final boolean isCollection = type != null && isCollection(type);

          final List<FlexValue> ownMembers = new ArrayList<FlexValue>(tokenizer.countTokens());
          final List<FlexValue> inheritedMembers = new ArrayList<FlexValue>(tokenizer.countTokens());

          while (tokenizer.hasMoreElements()) {
            final String s = tokenizer.nextToken().trim();
            if (s.length() == 0) continue;
            final int i1 = s.indexOf(DELIM);
            if (i1 == -1) {
              FlexDebugProcess.log("Unrecognized string:" + s);
              continue;
            }
            final String fieldName = s.substring(0, i1);
            String evaluatedPath = myExpression;

            if (fieldName.length() > 0 && Character.isDigit(fieldName.charAt(0))) {
              evaluatedPath += "[\"" + fieldName + "\"]";
            }
            else {
              evaluatedPath += "." + fieldName;
            }
            // either parameter of static function from scopechain or a field. Static functions from scopechain look like following:
            // // [Object 52571545, class='Main$/staticFunction']
            final ValueType valueType = type != null && type.indexOf('/') > -1 ? ValueType.Parameter : ValueType.Field;
            ownMembers
              .add(new FlexValue(fieldName, evaluatedPath, s.substring(i1 + DELIM.length()), FlexValue.this.myResult, valueType));
          }

          if (!isCollection) {
            ApplicationManager.getApplication().runReadAction(new Runnable() {
              public void run() {
                final JSClass jsClass =
                  mySourcePosition == null
                  ? null
                  : ApplicationManager.getApplication().runReadAction(new NullableComputable<JSClass>() {
                    public JSClass compute() {
                      final Project project = myDebugProcess.getSession().getProject();
                      return findJSClass(project, ModuleUtil.findModuleForFile(mySourcePosition.getFile(), project), type);
                    }
                  });

                final Iterator<FlexValue> iterator = ownMembers.iterator();
                while (iterator.hasNext()) {
                  final FlexValue flexValue = iterator.next();
                  if (findFieldOrGetter(flexValue.myName, jsClass, false) == null) {
                    iterator.remove();
                    inheritedMembers.add(flexValue);
                  }
                }
              }
            });
          }

          final List<FlexValue> directChildren = ownMembers.isEmpty() ? inheritedMembers : ownMembers;
          final List<FlexValue> indirectChildren = ownMembers.isEmpty() ? Collections.<FlexValue>emptyList() : inheritedMembers;

          if (isCollection) {
            Collections.sort(directChildren, myArrayElementsComparator);
          }

          final XValueChildrenList children = new XValueChildrenList();
          for (FlexValue value : directChildren) {
            children.add(value.myName, value);
          }

          if (!indirectChildren.isEmpty()) {
            final XValue inheritedNode = new XValue() {
              public void computePresentation(@NotNull final XValueNode node) {
                node.setPresentation((Icon)null, null, "", "Inherited members", true);
              }

              public void computeChildren(@NotNull final XCompositeNode node) {
                final XValueChildrenList inheritedChildren = new XValueChildrenList();
                for (FlexValue value : indirectChildren) {
                  inheritedChildren.add(value.myName, value);
                }
                node.addChildren(inheritedChildren, true);
              }
            };
            children.add("", inheritedNode);
          }

          node.addChildren(children, true);
          return CommandOutputProcessingMode.DONE;
        }
      };

      myDebugProcess.sendCommand(command);
    }

    private boolean isCollection(final @NotNull String type) {
      return type.contains("Array") ||
             type.contains("Vector") ||
             type.contains("Collection") ||
             type.contains("List");
    }

    private String referenceObjectBase(int i, String marker) {
      // expression may have incorrect syntax like x.dict1.-1. (see examples in http://youtrack.jetbrains.net/issue/IDEA-56653)
      // so it is more reliable to use objectId

      //if (myDebugProcess.isDebuggerFromSdk4()) {
      //  return expression + ".";
      //}

      return "#" + getObjectId(myResult, i, marker) + ".";
    }

    private String getObjectId(String result, int i, String marker) {
      String s = result.substring(i + marker.length(), result.indexOf(','));
      return validObjectId(s);
    }

    public void computeSourcePosition(@NotNull final XNavigatable navigatable) {
      if (mySourcePosition == null) {
        navigatable.setSourcePosition(null);
        return;
      }

      XSourcePosition result = null;
      final Project project = myDebugProcess.getSession().getProject();

      if (myValueType == ValueType.Variable) {
        final PsiElement contextElement =
          JSDebuggerSupportUtils.getContextElement(mySourcePosition.getFile(), mySourcePosition.getOffset(), project);
        final JSFunction jsFunction = PsiTreeUtil.getParentOfType(contextElement, JSFunction.class);

        if (jsFunction != null) {
          final Ref<JSVariable> varRef = new Ref<JSVariable>();
          jsFunction.accept(new JSElementVisitor() {
            public void visitJSElement(final JSElement node) {
              if (varRef.isNull()) {
                node.acceptChildren(this);
              }
            }

            public void visitJSVariable(final JSVariable node) {
              if (myName.equals(node.getName())) {
                varRef.set(node);
              }
              super.visitJSVariable(node);
            }
          });

          if (!varRef.isNull()) {
            result = calcSourcePosition(varRef.get());
          }
        }
      }
      else if (myValueType == ValueType.Parameter) {
        final PsiElement contextElement =
          JSDebuggerSupportUtils.getContextElement(mySourcePosition.getFile(), mySourcePosition.getOffset(), project);
        final JSFunction jsFunction = PsiTreeUtil.getParentOfType(contextElement, JSFunction.class);
        final JSParameterList parameterList = jsFunction == null ? null : jsFunction.getParameterList();
        final JSParameter[] parameters = parameterList == null ? JSParameter.EMPTY_ARRAY : parameterList.getParameters();
        for (final JSParameter parameter : parameters) {
          if (myName.equals(parameter.getName())) {
            result = calcSourcePosition(parameter);
            break;
          }
        }
      }
      else if (myValueType == ValueType.Field && myParentResult != null) {
        final String type = getTypeAndAdditionalInfo(myParentResult).first;
        final JSClass jsClass = findJSClass(project, ModuleUtil.findModuleForFile(mySourcePosition.getFile(), project), type);

        if (jsClass != null) {
          result = calcSourcePosition(findFieldOrGetter(myName, jsClass, true));
        }
      }
      navigatable.setSourcePosition(result);
    }

    @Nullable
    private XSourcePosition calcSourcePosition(final JSQualifiedNamedElement element) {
      if (element != null) {
        final PsiElement navigationElement = element.getNavigationElement();
        final VirtualFile file = navigationElement.getContainingFile().getVirtualFile();
        if (file != null) {
          return XDebuggerUtil.getInstance().createPositionByOffset(file, navigationElement.getTextOffset());
        }
      }
      return null;
    }
  }

  private static Pair<String, String> getTypeAndAdditionalInfo(final @Nullable String fdbText) {
    if (fdbText == null) return Pair.create(null, null);

    // [Object 52571545, class='flash.events::MouseEvent']
    // [Object 52571545, class='Main$/staticFunction']
    // [Object 62823129, class='XML@3be9ad9 element <abc/>']
    String type = null;
    String additionalInfo = null;

    final int classIndex = fdbText.indexOf(CLASS_MARKER);
    final int lastQuoteIndex = fdbText.lastIndexOf("'");

    if (classIndex != -1 && lastQuoteIndex > classIndex) {
      int typeStart = classIndex + CLASS_MARKER.length();
      final String inQuotes = fdbText.substring(typeStart, lastQuoteIndex);
      final int atIndex = inQuotes.indexOf("@");
      if (atIndex > 0) {
        type = inQuotes.substring(0, atIndex);
        final int spaceIndex = inQuotes.indexOf(" ", atIndex);
        if (spaceIndex != -1) {
          additionalInfo = inQuotes.substring(spaceIndex, inQuotes.length());
        }
      }
      else {
        type = inQuotes;
      }
    }

    if ("[]".equals(type)) {
      type = "Array";
    }

    return Pair.create(type, additionalInfo);
  }

  @Nullable
  private static JSClass findJSClass(final Project project, final @Nullable Module module, final String typeFromFlexValueResult) {
    if (typeFromFlexValueResult != null && !typeFromFlexValueResult.contains("/")) {
      final String fqn = typeFromFlexValueResult.replace("::", ".");
      final JavaScriptIndex jsIndex = JavaScriptIndex.getInstance(project);
      PsiElement jsClass = JSResolveUtil.findClassByQName(fqn, jsIndex, module);

      if (!(jsClass instanceof JSClass) && fqn.endsWith("$")) { // fdb adds '$' to class name in case of static context
        jsClass = JSResolveUtil.findClassByQName(fqn.substring(0, fqn.length() - 1), jsIndex, module);
      }

      if (!(jsClass instanceof JSClass) && module != null) {
        // probably this class came from dynamically loaded module that is not in moduleWithDependenciesAndLibrariesScope(module)
        final GlobalSearchScope scope = ProjectScope.getAllScope(project);
        jsClass = JSResolveUtil.findClassByQName(fqn, scope);

        if (!(jsClass instanceof JSClass) && fqn.endsWith("$")) {
          jsClass = JSResolveUtil.findClassByQName(fqn.substring(0, fqn.length() - 1), scope);
        }
      }

      return jsClass instanceof JSClass ? (JSClass)jsClass : null;
    }

    return null;
  }

  @Nullable
  private static JSQualifiedNamedElement findFieldOrGetter(final String name,
                                                           final JSClass jsClass,
                                                           final boolean lookInSupers) {
    return findFieldOrGetter(name, jsClass, lookInSupers, lookInSupers ? new THashSet<JSClass>() : Collections.<JSClass>emptySet());
  }

  @Nullable
  private static JSQualifiedNamedElement findFieldOrGetter(final String name,
                                                           final JSClass jsClass,
                                                           final boolean lookInSupers,
                                                           final Set<JSClass> visited) {
    if (visited.contains(jsClass)) return null;

    final JSVariable field = jsClass.findFieldByName(name);
    if (field != null) return field;

    final JSFunction getter = jsClass.findFunctionByNameAndKind(name, JSFunction.FunctionKind.GETTER);
    if (getter != null) return getter;

    if (lookInSupers) {
      visited.add(jsClass);

      for (final JSClass superClass : jsClass.getSuperClasses()) {
        final JSQualifiedNamedElement inSuper = findFieldOrGetter(name, superClass, lookInSupers, visited);
        if (inSuper != null) {
          return inSuper;
        }
      }
    }

    return null;
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
    private final ValueType myValueType;

    public MyDebuggerCommand(String text, XCompositeNode node, boolean _hasFrame, ValueType valueType) {
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
            resultChildren.add(prevName,
              new FlexValue(
                prevName, prevName,
                removeTrailingNewLines(previousNameAndValue.second),
                null,
                myValueType
              )
            );
          }

          previousNameAndValue = new Pair<String, StringBuilder>(name, new StringBuilder(token.substring(i + DELIM.length())));
        }

        if (previousNameAndValue != null) {
          String prevName = previousNameAndValue.first;
          resultChildren.add(prevName,
            new FlexValue(
              prevName, prevName,
              removeTrailingNewLines(previousNameAndValue.second),
              null,
              myValueType
            )
          );
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
