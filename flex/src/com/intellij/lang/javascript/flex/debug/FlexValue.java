// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.debug;

import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PlatformIcons;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.debugger.DebuggerSupportUtils;

import javax.swing.*;
import java.util.*;

class FlexValue extends XValue {
  private final FlexStackFrame myFlexStackFrame;
  private final FlexDebugProcess myDebugProcess;
  private final @Nullable XSourcePosition mySourcePosition;

  private final String myName;
  private final String myExpression;
  private final String myResult;
  private @Nullable final String myParentResult;
  private final ValueType myValueType;
  private Icon myPreferredIcon;

  private static final String OBJECT_MARKER = "Object ";
  private static final String XML_TYPE = "XML";
  private static final String XMLLIST_TYPE = "XMLList";
  static final String TEXT_MARKER = " text ";
  static final String ELEMENT_MARKER = " element ";
  private static final String ESCAPE_START = "IDEA-ESCAPE-START";
  private static final String ESCAPE_END = "IDEA-ESCAPE-END";
  private static final String VECTOR_PREFIX = "__AS3__.vec::";

  private static final String VECTOR = "Vector";
  private static final String GENERIC_VECTOR_PREFIX = "Vector.<";
  private static final String[] COLLECTIONS_WITH_DIRECT_CONTENT = {
    "Array",
    VECTOR,
    "mx.collections.ListCollectionView",
    "mx.collections.ArrayCollection",
    "mx.collections.XMLListCollection",
  };

  private static final String[] COLLECTION_CLASSES = ArrayUtil.mergeArrays(
    COLLECTIONS_WITH_DIRECT_CONTENT,
    "mx.collections.ArrayList",
    "mx.collections.AsyncListView"
  );

  private static final Comparator<XValue> ourArrayElementsComparator = (o1, o2) -> {
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
        }
        catch (NumberFormatException ignore) {/**/}
      }

      if (name == null) {
        return name2 == null ? 0 : -1;
      }
      else if (name2 == null) {
        return 1;
      }
      return name.compareToIgnoreCase(name2);
    }
    return 1;
  };

  enum ValueType {
    This(PlatformIcons.CLASS_ICON),
    Parameter(PlatformIcons.PARAMETER_ICON),
    Variable(PlatformIcons.VARIABLE_ICON),
    Field(PlatformIcons.FIELD_ICON),
    ScopeChainEntry(PlatformIcons.CLASS_INITIALIZER),
    Other(null);

    private @Nullable final Icon myIcon;

    ValueType(final @Nullable Icon icon) {
      myIcon = icon;
    }
  }

  FlexValue(final FlexStackFrame flexStackFrame,
            final FlexDebugProcess flexDebugProcess,
            final @Nullable XSourcePosition sourcePosition,
            final String name,
            final String expression,
            final String result,
            final @Nullable String parentResult,
            final @NotNull ValueType valueType) {
    myFlexStackFrame = flexStackFrame;
    myDebugProcess = flexDebugProcess;
    mySourcePosition = sourcePosition;
    myName = name;
    myExpression = expression;
    myResult = unescape(result, flexDebugProcess.isFlexSdk_4_12plus_IdeMode());
    myParentResult = parentResult;
    myValueType = valueType;
  }

  String getResult() {
    return myResult;
  }

  @Override
  public String getEvaluationExpression() {
    return myExpression;
  }

  public void setPreferredIcon(final Icon preferredIcon) {
    myPreferredIcon = preferredIcon;
  }

  private Icon getIcon() {
    return myPreferredIcon == null ? myValueType.myIcon : myPreferredIcon;
  }

  @Override
  public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
    final boolean isObject = myResult.contains(OBJECT_MARKER);
    String val = myResult;
    String typeFromFlexValueResult = null;
    String additionalInfo = null;

    if (isObject) {
      final Pair<String, String> typeAndAdditionalInfo = getTypeAndAdditionalInfo(myResult);
      typeFromFlexValueResult = typeAndAdditionalInfo.first;
      additionalInfo = typeAndAdditionalInfo.second;

      if (typeFromFlexValueResult != null) {
        try {
          val = "[".concat(getObjectId(myResult, myResult.indexOf(OBJECT_MARKER), OBJECT_MARKER)).concat("]");
        }
        catch (StringIndexOutOfBoundsException e) {
          FlexDebugProcess.log(new Exception(myResult, e));
        }
      }
    }

    if ((XML_TYPE.equals(typeFromFlexValueResult) || XMLLIST_TYPE.equals(typeFromFlexValueResult)) && myExpression.indexOf('=') == -1) {
      if (myDebugProcess.isDebuggerFromSdk3()) {
        if (XMLLIST_TYPE.equals(typeFromFlexValueResult)) {
          setXmlListPresentation(node, val, this);
          return;
        }
        else if (additionalInfo != null) {
          setXmlPresentation(node, additionalInfo, this);
          return;
        }
      }
      else {
        scheduleToXmlStringCalculation(node, typeFromFlexValueResult);
        // return; no return - show default presentation until toXmlString calculated
      }
    }

    final String type = getType(typeFromFlexValueResult);
    if (type != null && isCollection(type)) {
      if (type.equals(VECTOR) || type.startsWith(GENERIC_VECTOR_PREFIX)) {
        scheduleVectorPresentation(node, typeFromFlexValueResult);
      }
      else {
        scheduleCollectionSizePresentation(node, typeFromFlexValueResult, "");
      }
    }

    val = setFullValueEvaluatorIfNeeded(node, val, false);
    node.setPresentation(getIcon(), typeFromFlexValueResult, val, isObject);
  }

  private static boolean isCollectionWithDirectContent(final String fqn) {
    return fqn != null && ArrayUtil.contains(fqn, COLLECTIONS_WITH_DIRECT_CONTENT);
  }

  private static boolean isCollection(final @NotNull String type) {
    return isGenericVector(type) || ArrayUtil.contains(type, COLLECTION_CLASSES);
  }

  private void scheduleVectorPresentation(final XValueNode node, final String type) {
    final FlexStackFrame.EvaluateCommand command =
      myFlexStackFrame.new EvaluateCommand(myExpression + ".fixed", new XDebuggerEvaluator.XEvaluationCallback() {

        @Override
        public void evaluated(@NotNull XValue result) {
          if (!node.isObsolete()) {
            final String resultText = ((FlexValue)result).myResult;
            final String prefix = ("true".equals(resultText) || "false".equals(resultText)) ? "fixed = " + resultText : "";
            node.setPresentation(getIcon(), type, prefix, true);
            scheduleCollectionSizePresentation(node, type, prefix);
          }
        }

        @Override
        public void errorOccurred(@NotNull String errorMessage) {
        }
      });

    myDebugProcess.addPendingCommand(new CompositeDebuggerCommand(node, command), 100);
  }

  private void scheduleCollectionSizePresentation(final XValueNode node, final String type, final String prefix) {
    final FlexStackFrame.EvaluateCommand command =
      myFlexStackFrame.new EvaluateCommand(myExpression + ".length", new XDebuggerEvaluator.XEvaluationCallback() {

        @Override
        public void evaluated(@NotNull XValue result) {
          if (!node.isObsolete()) {
            final String resultText = ((FlexValue)result).myResult;
            final int index = resultText.indexOf(" (0x");
            if (index != -1) {
              final String value = (prefix.isEmpty() ? "" : prefix + ", ") + "size = " + resultText.substring(0, index);
              node.setPresentation(getIcon(), type, value, true);
            }
          }
        }

        @Override
        public void errorOccurred(@NotNull String errorMessage) {
        }
      });

    myDebugProcess.addPendingCommand(new CompositeDebuggerCommand(node, command), 100);
  }

  private static void setXmlListPresentation(final XValueNode node, final String value, final FlexValue flexValue) {
    node.setFullValueEvaluator(new XFullValueEvaluator() {
      @Override
      public void startEvaluation(@NotNull XFullValueEvaluationCallback callback) {
        new XmlObjectEvaluator(flexValue, callback).startEvaluation();
      }
    });

    node.setPresentation(flexValue.getIcon(), XMLLIST_TYPE, value, true);
  }

  private static void setXmlPresentation(final XValueNode node, final String additionalInfo, final FlexValue flexValue) {
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
        if (textToShow.length() > XValueNode.MAX_VALUE_LENGTH) {
          textToShow = textToShow.substring(0, XValueNode.MAX_VALUE_LENGTH);
        }

        node.setFullValueEvaluator(new XFullValueEvaluator() {
          @Override
          public void startEvaluation(@NotNull XFullValueEvaluationCallback callback) {
            new XmlObjectEvaluator(flexValue, callback).startEvaluation();
          }
        });

        node.setPresentation(flexValue.getIcon(), XML_TYPE, textToShow, true);
        return;
      }

      textToShow = setFullValueEvaluatorIfNeeded(node, textToShow, true);
      node.setPresentation(flexValue.getIcon(), XML_TYPE, textToShow, true);
      return;
    }

    node.setPresentation(flexValue.getIcon(), XML_TYPE, setFullValueEvaluatorIfNeeded(node, additionalInfo, true), true);
  }

  private void scheduleToXmlStringCalculation(final XValueNode node, final String type) {
    final FlexStackFrame.EvaluateCommand
      command = myFlexStackFrame.new EvaluateCommand(myExpression + ".toXMLString()", new XDebuggerEvaluator.XEvaluationCallback() {

      @Override
      public void evaluated(@NotNull XValue result) {
        setResult(((FlexValue)result).myResult, node, type, true);
      }

      @Override
      public void errorOccurred(@NotNull String errorMessage) {
        setResult(errorMessage, node, type, true);
      }

      private void setResult(String value, XValueNode node, String type, boolean hasChildren) {
        if (!node.isObsolete()) {
          value = setFullValueEvaluatorIfNeeded(node, value, true);
          node.setPresentation(getIcon(), type, value, hasChildren);
        }
      }
    });

    myDebugProcess.addPendingCommand(new CompositeDebuggerCommand(node, command), 700);
  }

  private static String setFullValueEvaluatorIfNeeded(final XValueNode node, String value, final boolean isXml) {
    final String fullValue = value;

    final int lfIndex = fullValue.indexOf('\n');
    final int crIndex = fullValue.indexOf('\r');

    if (fullValue.length() > XValueNode.MAX_VALUE_LENGTH ||
        lfIndex > -1 && lfIndex < fullValue.length() - 1 ||
        crIndex > -1 && crIndex < fullValue.length() - 1) {

      final boolean quoted = fullValue.charAt(0) == '\'' && fullValue.charAt(fullValue.length() - 1) == '\'';
      final boolean doubleQuoted = fullValue.charAt(0) == '\"' && fullValue.charAt(fullValue.length() - 1) == '\"';

      if (value.length() > XValueNode.MAX_VALUE_LENGTH) {
        final String ending = doubleQuoted ? "\" " : quoted ? "' " : " ";
        value = value.substring(0, XValueNode.MAX_VALUE_LENGTH).concat(ending);
      }
      value = value.trim();

      final String unquoted = quoted || doubleQuoted ? fullValue.substring(1, fullValue.length() - 1) : fullValue;
      node.setFullValueEvaluator(
        new XFullValueEvaluator() {
          @Override
          public void startEvaluation(@NotNull XFullValueEvaluationCallback callback) {
            callback.evaluated(StringUtil.convertLineSeparators(unquoted), isXml ? XmlObjectEvaluator.MONOSPACED_FONT : null);
          }
        });
    }

    return value;
  }

  @Override
  public XValueModifier getModifier() {
    return new XValueModifier() {
      @Override
      public void setValue(@NotNull XExpression _expression, @NotNull final XModificationCallback callback) {
        FlexStackFrame.EvaluateCommand command =
          myFlexStackFrame.new EvaluateCommand(myExpression + "=" + _expression.getExpression(), null) {
          @Override
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

    final String typeFromFlexValueResult = getTypeAndAdditionalInfo(myResult).first;

    final String expression;
    try {
      expression = referenceObjectBase(i, OBJECT_MARKER);
    }
    catch (StringIndexOutOfBoundsException e) {
      FlexDebugProcess.log(new Exception(myResult, e));
      node.addChildren(XValueChildrenList.EMPTY, true);
      return;
    }

    final FlexStackFrame.EvaluateCommand
      command = myFlexStackFrame.new EvaluateCommand(expression, null) {
      @Override
      CommandOutputProcessingMode doOnTextAvailable(@NonNls final String resultS) {
        StringTokenizer tokenizer = new StringTokenizer(resultS, "\r\n");

        // skip first token; it contains $-prefix followed by myResult: $6 = [Object 30860193, class='__AS3__.vec::Vector.<String>']
        tokenizer.nextToken();

        final LinkedHashMap<String, FlexValue> fieldNameToFlexValueMap = new LinkedHashMap<>(tokenizer.countTokens());

        final NodeClassInfo nodeClassInfo =
          DumbService.getInstance(myDebugProcess.getSession().getProject()).runReadActionInSmartMode(() -> {
            final Project project = myDebugProcess.getSession().getProject();
            final JSClass jsClass = mySourcePosition == null
                                    ? null
                                    : findJSClass(project,
                                                  ModuleUtilCore.findModuleForFile(mySourcePosition.getFile(), project),
                                                  typeFromFlexValueResult);
            return jsClass == null ? null : NodeClassInfo.getNodeClassInfo(jsClass);
          });

        while (tokenizer.hasMoreElements()) {
          final String s = tokenizer.nextToken().trim();
          if (s.length() == 0) continue;
          final int delimIndex = s.indexOf(FlexStackFrame.DELIM);
          if (delimIndex == -1) {
            FlexDebugProcess.log("Unrecognized string:" + s);
            continue;
          }
          final String fieldName = s.substring(0, delimIndex);
          final String result = s.substring(delimIndex + FlexStackFrame.DELIM.length());

          if (result.startsWith("[Setter ")) {
            // such values do not give any useful information:
            // [Setter 62, name='Child@3d613bb::staticSetter']
            // [Setter 78]
            continue;
          }

          String evaluatedPath = myExpression;

          if (fieldName.length() > 0 && Character.isDigit(fieldName.charAt(0))) {
            evaluatedPath += "[\"" + fieldName + "\"]";
          }
          else {
            evaluatedPath += "." + fieldName;
          }
          // either parameter of static function from scopechain or a field. Static functions from scopechain look like following:
          // // [Object 52571545, class='Main$/staticFunction']
          final ValueType valueType =
            typeFromFlexValueResult != null && typeFromFlexValueResult.indexOf('/') > -1 ? ValueType.Parameter : ValueType.Field;
          final FlexValue flexValue =
            new FlexValue(myFlexStackFrame, myDebugProcess, mySourcePosition, fieldName, evaluatedPath, result, FlexValue.this.myResult,
                          valueType);

          addValueCheckingDuplicates(flexValue, fieldNameToFlexValueMap);
        }

        addChildren(node, fieldNameToFlexValueMap, nodeClassInfo);

        return CommandOutputProcessingMode.DONE;
      }
    };

    myDebugProcess.sendCommand(command);
  }

  @Override
  public boolean canNavigateToTypeSource() {
    final boolean isObject = myResult.contains(OBJECT_MARKER);

    if (isObject && mySourcePosition != null) {
      final Pair<String, String> typeAndAdditionalInfo = getTypeAndAdditionalInfo(myResult);
      final String typeFromFlexValueResult = typeAndAdditionalInfo.first;
      return typeFromFlexValueResult != null;
    }

    return false;
  }

  @Override
  public void computeTypeSourcePosition(@NotNull final XNavigatable navigatable) {
    if (mySourcePosition == null) {
      navigatable.setSourcePosition(null);
      return;
    }

    final boolean isObject = myResult.contains(OBJECT_MARKER);

    if (isObject) {
      final Pair<String, String> typeAndAdditionalInfo = getTypeAndAdditionalInfo(myResult);
      final String typeFromFlexValueResult = typeAndAdditionalInfo.first;
      if (typeFromFlexValueResult != null) {
        final Project project = myDebugProcess.getSession().getProject();
        final JSClass jsClass =
          findJSClass(project, ModuleUtilCore.findModuleForFile(mySourcePosition.getFile(), project), typeFromFlexValueResult);
        navigatable.setSourcePosition(DebuggerSupportUtils.calcSourcePosition(jsClass));
        return;
      }
    }

    navigatable.setSourcePosition(null);
  }

  @Override
  public boolean canNavigateToSource() {
    return mySourcePosition != null && (myValueType == ValueType.Variable ||
                                        myValueType == ValueType.Parameter ||
                                        myValueType == ValueType.Field && myParentResult != null);
  }

  @Override
  public void computeSourcePosition(@NotNull final XNavigatable navigatable) {
    if (mySourcePosition == null) {
      navigatable.setSourcePosition(null);
      return;
    }

    XSourcePosition result = null;
    final Project project = myDebugProcess.getSession().getProject();

    if (myValueType == ValueType.Variable) {
      final PsiElement contextElement =
        XDebuggerUtil.getInstance().findContextElement(mySourcePosition.getFile(), mySourcePosition.getOffset(), project, true);
      final JSFunction jsFunction = PsiTreeUtil.getParentOfType(contextElement, JSFunction.class);

      if (jsFunction != null) {
        final Ref<JSVariable> varRef = new Ref<>();
        jsFunction.accept(new JSElementVisitor() {
          @Override
          public void visitJSElement(final JSElement node) {
            if (varRef.isNull()) {
              node.acceptChildren(this);
            }
          }

          @Override
          public void visitJSVariable(final JSVariable node) {
            if (myName.equals(node.getName())) {
              varRef.set(node);
            }
            super.visitJSVariable(node);
          }
        });

        if (!varRef.isNull()) {
          result = DebuggerSupportUtils.calcSourcePosition(varRef.get());
        }
      }
    }
    else if (myValueType == ValueType.Parameter) {
      final PsiElement contextElement =
        XDebuggerUtil.getInstance().findContextElement(mySourcePosition.getFile(), mySourcePosition.getOffset(), project, true);
      final JSFunction jsFunction = PsiTreeUtil.getParentOfType(contextElement, JSFunction.class);
      final JSParameter[] parameters = jsFunction == null ? JSParameter.EMPTY_ARRAY : jsFunction.getParameterVariables();
      for (final JSParameter parameter : parameters) {
        if (myName.equals(parameter.getName())) {
          result = DebuggerSupportUtils.calcSourcePosition(parameter);
          break;
        }
      }
    }
    else if (myValueType == ValueType.Field && myParentResult != null) {
      final String typeFromFlexValueResult = getTypeAndAdditionalInfo(myParentResult).first;
      final JSClass jsClass =
        findJSClass(project, ModuleUtilCore.findModuleForFile(mySourcePosition.getFile(), project), typeFromFlexValueResult);

      if (jsClass != null) {
        final Ref<PsiElement> fieldRef = new Ref<>();
        fieldRef.set(JSInheritanceUtil.findMember(myName, jsClass, JSInheritanceUtil.SearchedMemberType.FieldsAndMethods,
                                                  JSFunction.FunctionKind.GETTER, true));

        if (fieldRef.isNull() && jsClass instanceof MxmlJSClass) {
          final PsiFile file = jsClass.getContainingFile();
          final XmlFile xmlFile = file instanceof XmlFile ? (XmlFile)file : null;
          final XmlTag rootTag = xmlFile == null ? null : xmlFile.getRootTag();
          if (rootTag != null) {
            NodeClassInfo.processSubtagsRecursively(rootTag, tag -> {
              final XmlAttribute idAttr = tag.getAttribute("id");
              final String id = idAttr == null ? null : idAttr.getValue();
              if (id != null && id.equals(myName)) {
                fieldRef.set(idAttr);
              }
              return !MxmlJSClass.isTagThatAllowsAnyXmlContent(tag);
            });
          }
        }

        result = DebuggerSupportUtils.calcSourcePosition(fieldRef.get());
      }
    }
    navigatable.setSourcePosition(result);
  }

  private static void addValueCheckingDuplicates(final FlexValue flexValue,
                                                 final LinkedHashMap<String, FlexValue> fieldNameToFlexValueMap) {
    final String name = flexValue.myName;
    FlexValue existingValue;

    if ((existingValue = fieldNameToFlexValueMap.get("_" + name)) != null &&
        existingValue.getResult().equals(flexValue.getResult())) {
      fieldNameToFlexValueMap.remove("_" + name);
    }
    else if (name.startsWith("_") &&
             name.length() > 1 &&
             (existingValue = fieldNameToFlexValueMap.get(name.substring(1))) != null &&
             existingValue.getResult().equals(flexValue.getResult())) {
      return;
    }

    fieldNameToFlexValueMap.put(name, flexValue);
  }

  private static void addChildren(final XCompositeNode node,
                                  final LinkedHashMap<String, FlexValue> fieldNameToFlexValueMap,
                                  final @Nullable NodeClassInfo nodeClassInfo) {
    final List<FlexValue> elementsOfCollection = new LinkedList<>();
    final XValueChildrenList ownStaticFields = new XValueChildrenList();
    final XValueChildrenList ownStaticProperties = new XValueChildrenList();
    final XValueChildrenList ownFields = new XValueChildrenList();
    final XValueChildrenList ownProperties = new XValueChildrenList();
    final XValueChildrenList inheritedStaticFields = new XValueChildrenList();
    final XValueChildrenList inheritedStaticProperties = new XValueChildrenList();
    final XValueChildrenList inheritedFields = new XValueChildrenList();
    final XValueChildrenList inheritedProperties = new XValueChildrenList();

    for (final Map.Entry<String, FlexValue> entry : fieldNameToFlexValueMap.entrySet()) {
      final String name = entry.getKey();
      final FlexValue flexValue = entry.getValue();

      if (isInteger(name)) {
        elementsOfCollection.add(flexValue);
        continue;
      }

      if (nodeClassInfo == null) {
        ownFields.add(name, flexValue);
      }
      else {
        if (updateIconAndAddToListIfMatches(name, flexValue, nodeClassInfo.myOwnStaticFields, ownStaticFields) ||
            updateIconAndAddToListIfMatches(name, flexValue, nodeClassInfo.myOwnStaticProperties, ownStaticProperties) ||
            updateIconAndAddToListIfMatches(name, flexValue, nodeClassInfo.myOwnFields, ownFields) ||
            updateIconAndAddToListIfMatches(name, flexValue, nodeClassInfo.myOwnProperties, ownProperties) ||
            updateIconAndAddToListIfMatches(name, flexValue, nodeClassInfo.myInheritedStaticFields, inheritedStaticFields) ||
            updateIconAndAddToListIfMatches(name, flexValue, nodeClassInfo.myInheritedStaticProperties, inheritedStaticProperties) ||
            updateIconAndAddToListIfMatches(name, flexValue, nodeClassInfo.myInheritedFields, inheritedFields) ||
            updateIconAndAddToListIfMatches(name, flexValue, nodeClassInfo.myInheritedProperties, inheritedProperties)) {
          continue;
        }

        (nodeClassInfo.myIsDynamic ? ownFields : inheritedFields).add(name, flexValue);
      }
    }

    elementsOfCollection.sort(ourArrayElementsComparator);

    XValueChildrenList inheritedNodeSingletonList = XValueChildrenList.EMPTY;
    if (inheritedStaticFields.size() + inheritedStaticProperties.size() + inheritedFields.size() + inheritedProperties.size() > 0) {
      if (inheritedStaticFields.size() + inheritedStaticProperties.size() > 0) {
        final XValueChildrenList inheritedStatics =
          createWrappingGroupList("Static members", inheritedStaticFields, inheritedStaticProperties);
        inheritedNodeSingletonList = createWrappingGroupList("Inherited members", inheritedStatics, inheritedFields, inheritedProperties);
      }
      else {
        inheritedNodeSingletonList = createWrappingGroupList("Inherited members", inheritedStaticFields, inheritedStaticProperties,
                                                             inheritedFields, inheritedProperties);
      }
    }

    if (nodeClassInfo != null && isCollectionWithDirectContent(nodeClassInfo.myFqn)) {
      final XValueChildrenList fieldsAndPropertiesSingletonList =
        createWrappingGroupList("Fields and properties", inheritedNodeSingletonList, ownStaticFields, ownStaticProperties, ownFields,
                                ownProperties);
      node.addChildren(fieldsAndPropertiesSingletonList, false);
    }
    else {
      node.addChildren(inheritedNodeSingletonList, false);
      if (ownStaticFields.size() + ownStaticProperties.size() > 0) {
        node.addChildren(createWrappingGroupList("Static members", ownStaticFields, ownStaticProperties), false);
      }
      node.addChildren(ownFields, false);
      node.addChildren(ownProperties, false);
    }

    final XValueChildrenList elementsOfCollectionList = new XValueChildrenList();
    for (final FlexValue flexValue : elementsOfCollection) {
      elementsOfCollectionList.add(flexValue.myName, flexValue);
    }
    node.addChildren(elementsOfCollectionList, true);
  }

  private static XValueChildrenList createWrappingGroupList(final String groupName, final XValueChildrenList... listsToWrap) {
    final XValueGroup group = new XValueGroup(groupName) {
      @Override
      public void computeChildren(@NotNull final XCompositeNode node) {
        for (final XValueChildrenList list : listsToWrap) {
          node.addChildren(list, false);
        }
        node.addChildren(XValueChildrenList.EMPTY, true);
      }
    };

    final XValueChildrenList inheritedSingleNodeList = new XValueChildrenList();
    inheritedSingleNodeList.addTopGroup(group);
    return inheritedSingleNodeList;
  }

  private static boolean updateIconAndAddToListIfMatches(final String name,
                                                         final FlexValue flexValue,
                                                         final Map<String, Icon> nameToIconMap,
                                                         final XValueChildrenList list) {
    final Icon icon = nameToIconMap.get(name);
    if (icon != null) {
      flexValue.setPreferredIcon(icon);
      list.add(flexValue.myName, flexValue);
      return true;
    }
    return false;
  }

  private static boolean isInteger(final String s) {
    try {
      Integer.parseInt(s);
      return true;
    }
    catch (NumberFormatException ignored) {
      return false;
    }
  }

  private String referenceObjectBase(int i, String marker) {
    // expression may have incorrect syntax like x.dict1.-1. (see examples in http://youtrack.jetbrains.net/issue/IDEA-56653)
    // so it is more reliable to use objectId

    //if (myDebugProcess.isDebuggerFromSdk4()) {
    //  return expression + ".";
    //}

    return "#" + getObjectId(myResult, i, marker) + ".";
  }

  private static String getObjectId(String result, int i, String marker) {
    String s = result.substring(i + marker.length(), result.indexOf(','));
    return FlexStackFrame.validObjectId(s);
  }

  /**
   * Examples of type ({@code getTypeAndAdditionalInfo().first}) :
   * <ul>
   * <li>{@code null}</li>
   * <li>{@code flash.events::MouseEvent}</li>
   * <li>{@code Main$/staticFunction}</li>
   * <li>{@code XML}</li>
   * <li>{@code pack.SomeClass$}</li>
   * <li>{@code Vector.<String></String>}</li>
   * </ul>
   */
  private static Pair<String, String> getTypeAndAdditionalInfo(final @Nullable String fdbText) {
    if (fdbText == null) return Pair.create(null, null);

    // [Object 52571545, class='flash.events::MouseEvent']
    // [Object 52571545, class='Main$/staticFunction']
    // [Object 62823129, class='XML@3be9ad9 element <abc/>']
    String type = null;
    String additionalInfo = null;

    final int classIndex = fdbText.indexOf(FlexStackFrame.CLASS_MARKER);
    final int lastQuoteIndex = fdbText.lastIndexOf("'");

    if (classIndex != -1 && lastQuoteIndex > classIndex) {
      int typeStart = classIndex + FlexStackFrame.CLASS_MARKER.length();
      final String inQuotes = fdbText.substring(typeStart, lastQuoteIndex);
      final int atIndex = inQuotes.indexOf("@");
      if (atIndex > 0) {
        type = inQuotes.substring(0, atIndex);
        final int spaceIndex = inQuotes.indexOf(" ", atIndex);
        if (spaceIndex != -1) {
          additionalInfo = inQuotes.substring(spaceIndex);
        }
      }
      else {
        type = inQuotes;
      }
    }

    if ("[]".equals(type)) {
      type = "Array";
    }

    if (type != null) {
      type = StringUtil.replace(type, VECTOR_PREFIX, "");
    }

    return Pair.create(type, additionalInfo);
  }

  /**
   * Returned result can contain extra <b>$</b> after real class FQN in case of static context. For example {@code pack.Main$}
   * Also it may contain vector type, e.g. {@code Vector.<int>}
   */
  @Nullable
  private static String getType(final String typeFromFlexValueResult) {
    if (typeFromFlexValueResult != null && !typeFromFlexValueResult.contains("/")) {
      return typeFromFlexValueResult.replace("::", ".");
    }
    return null;
  }

  private static boolean isGenericVector(final String type) {
    return type.startsWith(GENERIC_VECTOR_PREFIX);
  }

  @Nullable
  private static JSClass findJSClass(final Project project, final @Nullable Module module, final String typeFromFlexValueResult) {
    String type = getType(typeFromFlexValueResult);
    if (type != null) {
      if (isGenericVector(type)) {
        type = VECTOR;
      }

      final JavaScriptIndex jsIndex = JavaScriptIndex.getInstance(project);
      PsiElement jsClass = ActionScriptClassResolver.findClassByQName(type, jsIndex, module);

      if (!(jsClass instanceof JSClass) && type.endsWith("$")) { // fdb adds '$' to class name in case of static context
        jsClass = ActionScriptClassResolver.findClassByQName(type.substring(0, type.length() - 1), jsIndex, module);
      }

      if (!(jsClass instanceof JSClass) && module != null) {
        // probably this class came from dynamically loaded module that is not in moduleWithDependenciesAndLibrariesScope(module)
        final GlobalSearchScope scope = ProjectScope.getAllScope(project);
        jsClass = ActionScriptClassResolver.findClassByQNameStatic(type, scope);

        if (!(jsClass instanceof JSClass) && type.endsWith("$")) {
          jsClass = ActionScriptClassResolver.findClassByQNameStatic(type.substring(0, type.length() - 1), scope);
        }
      }

      return jsClass instanceof JSClass ? (JSClass)jsClass : null;
    }

    return null;
  }

  /**
   * Looks for IDEA-ESCAPE-START and IDEA-ESCAPE-END markers in input string and unescapes symbols inside these markers. Markers are removed.
   */
  private static String unescape(String str, final boolean flexSdk_4_12plus_IdeMode) {
    if (flexSdk_4_12plus_IdeMode) {
      return StringUtil.unescapeStringCharacters(str);
    }

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
}
