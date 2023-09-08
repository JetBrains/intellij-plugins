<info textAttributesKey="DART_KEYWORD">library</info> <info textAttributesKey="DART_LIBRARY_NAME">foo.bar</info>;

<info textAttributesKey="DART_KEYWORD">import</info> "dart:core";
<info textAttributesKey="DART_KEYWORD">import</info> "dart:html"
<info textAttributesKey="DART_KEYWORD">as</info> <info textAttributesKey="DART_IMPORT_PREFIX">html</info>
    <info textAttributesKey="DART_KEYWORD">show</info> <info textAttributesKey="DART_CLASS">HtmlElement</info>,
    <warning descr="The name document is shown, but isn't used." textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES"><info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">document</info></warning>,
    <warning descr="The name EventListener is shown, but isn't used." textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES"><info textAttributesKey="DART_FUNCTION_TYPE_ALIAS">EventListener</info></warning>,
    <warning descr="'CDataSection' is deprecated and shouldn't be used." textAttributesKey="DEPRECATED_ATTRIBUTES"><warning descr="The name CDataSection is shown, but isn't used." textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES"><info textAttributesKey="DART_CLASS">CDataSection</info></warning></warning>,
    <warning descr="The library 'dart:html' doesn't export a member with the shown name 'incorrect'." textAttributesKey="DART_WARNING"><info textAttributesKey="DART_IDENTIFIER">incorrect</info></warning>;

<info textAttributesKey="DART_KEYWORD">get</info> <info textAttributesKey="DART_TOP_LEVEL_GETTER_DECLARATION">topLevelGetter</info> {
  <info textAttributesKey="DART_KEYWORD">return</info> <info textAttributesKey="DART_TOP_LEVEL_GETTER_REFERENCE">topLevelGetter</info>;
}

<info textAttributesKey="DART_KEYWORD">set</info> <info textAttributesKey="DART_TOP_LEVEL_SETTER_DECLARATION">topLevelSetter</info>(<info textAttributesKey="DART_CLASS">bool</info> <info textAttributesKey="DART_PARAMETER_DECLARATION">param</info>) {
  <info textAttributesKey="DART_TOP_LEVEL_SETTER_REFERENCE">topLevelSetter</info> = <info textAttributesKey="DART_KEYWORD">true</info>;
}

<info textAttributesKey="DART_CLASS">Object</info> <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">topLevelVariable</info> = 0;

<info textAttributesKey="DART_KEYWORD">void</info> <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">topLevelFunction</info>(<info textAttributesKey="DART_DYNAMIC_PARAMETER_DECLARATION">param</info>) {
  <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_REFERENCE">topLevelFunction</info>(1);
  <info textAttributesKey="DART_TOP_LEVEL_GETTER_REFERENCE">topLevelVariable</info>;
  <info textAttributesKey="DART_DYNAMIC_PARAMETER_REFERENCE">param</info>;
  <info textAttributesKey="DART_LOCAL_FUNCTION_DECLARATION">innerFunction</info>() {}
  <info textAttributesKey="DART_LOCAL_FUNCTION_REFERENCE">innerFunction</info>();
  <info textAttributesKey="DART_IMPORT_PREFIX">html</info>.<info textAttributesKey="DART_CLASS">HtmlElement</info>;

  <info textAttributesKey="DART_LABEL">label</info>:
  <info textAttributesKey="DART_KEYWORD">while</info> (<info textAttributesKey="DART_KEYWORD">true</info>) {
    <info textAttributesKey="DART_KEYWORD">break</info> <info textAttributesKey="DART_LABEL">label</info>;
  }

  <info textAttributesKey="DART_KEYWORD">new</info> <info textAttributesKey="DART_CONSTRUCTOR">Foo</info>();
  <info textAttributesKey="DART_KEYWORD">new</info> <info textAttributesKey="DART_CONSTRUCTOR">Foo</info>.<info textAttributesKey="DART_CONSTRUCTOR">from</info>();
  <info textAttributesKey="DART_KEYWORD">new</info> <info textAttributesKey="DART_CONSTRUCTOR">Foo</info>.<info textAttributesKey="DART_CONSTRUCTOR">redirect</info>();
  <info textAttributesKey="DART_KEYWORD">new</info> <info textAttributesKey="DART_CONSTRUCTOR">Foo</info>.<info textAttributesKey="DART_CONSTRUCTOR">factory</info>();
  <info textAttributesKey="DART_KEYWORD">new</info> <info textAttributesKey="DART_CONSTRUCTOR">Foo2</info>(1);
  <info textAttributesKey="DART_KEYWORD">const</info> <info textAttributesKey="DART_CONSTRUCTOR">Foo2</info>(1);
  <info textAttributesKey="DART_ENUM">Enum</info>.<info textAttributesKey="DART_ENUM_CONSTANT">EnumConstant</info>;
  <info textAttributesKey="DART_KEYWORD">var</info> <info textAttributesKey="DART_DYNAMIC_LOCAL_VARIABLE_DECLARATION">dynamicLocalVar</info>;
  <info textAttributesKey="DART_DYNAMIC_LOCAL_VARIABLE_REFERENCE">dynamicLocalVar</info>;
}

<info textAttributesKey="DART_KEYWORD">enum</info> <info textAttributesKey="DART_ENUM">Enum</info> {
  <info textAttributesKey="DART_ENUM_CONSTANT">EnumConstant</info>
}

<info textAttributesKey="DART_KEYWORD">class</info> <info textAttributesKey="DART_CLASS">Foo</info> {
  <info textAttributesKey="DART_CLASS">Foo</info>(){}
  <info textAttributesKey="DART_CLASS">Foo</info>.<info textAttributesKey="DART_CONSTRUCTOR">from</info>(){}
  <info textAttributesKey="DART_CLASS">Foo</info>.<info textAttributesKey="DART_CONSTRUCTOR">redirect</info>() : this.<info textAttributesKey="DART_CONSTRUCTOR">from</info>();
<info textAttributesKey="DART_KEYWORD">factory</info> <error descr="The body might complete normally, causing 'null' to be returned, but the return type, 'Foo', is a potentially non-nullable type." textAttributesKey="DART_ERROR"><info textAttributesKey="DART_CLASS">Foo</info>.factory</error>() {}
}

/// [<info textAttributesKey="DART_CLASS">Foo1</info>] is good []
<info textAttributesKey="DART_KEYWORD">class</info> <info textAttributesKey="DART_CLASS">Foo1</info> {}

<info textAttributesKey="DART_KEYWORD">class</info> <info textAttributesKey="DART_CLASS">Foo2</info><<info textAttributesKey="DART_TYPE_PARAMETER">Generic</info>> {
  <info textAttributesKey="DART_KEYWORD">final</info> <info textAttributesKey="DART_TYPE_PARAMETER">Generic</info> <info textAttributesKey="DART_INSTANCE_FIELD_DECLARATION">x</info>;
  <info textAttributesKey="DART_KEYWORD">const</info> <info textAttributesKey="DART_CLASS">Foo2</info>(<info textAttributesKey="DART_KEYWORD">this</info>.<info textAttributesKey="DART_INSTANCE_FIELD_REFERENCE">x</info>);
}

<info textAttributesKey="DART_KEYWORD">abstract</info> <info textAttributesKey="DART_KEYWORD">class</info> <info textAttributesKey="DART_CLASS">Bar</info>
    <info textAttributesKey="DART_KEYWORD">extends</info> <info textAttributesKey="DART_CLASS">Object</info>
    <info textAttributesKey="DART_KEYWORD">implements</info> <info textAttributesKey="DART_CLASS">Foo</info> {

  <info textAttributesKey="DART_KEYWORD">static</info> <info textAttributesKey="DART_KEYWORD">const</info> <info textAttributesKey="DART_STATIC_FIELD_DECLARATION">staticConst</info> = 1;
  <info textAttributesKey="DART_KEYWORD">static</info> <info textAttributesKey="DART_KEYWORD">var</info> <info textAttributesKey="DART_STATIC_FIELD_DECLARATION">staticField</info>;
  <info textAttributesKey="DART_KEYWORD">var</info> <info textAttributesKey="DART_INSTANCE_FIELD_DECLARATION">instanceVar</info>;

  <info textAttributesKey="DART_KEYWORD">static</info> <info textAttributesKey="DART_STATIC_METHOD_DECLARATION">staticMethod</info>() {
    <info textAttributesKey="DART_STATIC_GETTER_REFERENCE">staticConst</info> +
        <info textAttributesKey="DART_STATIC_GETTER_REFERENCE">staticField</info>;
    <info textAttributesKey="DART_STATIC_METHOD_REFERENCE">staticMethod</info>();
  }

  <info textAttributesKey="DART_INSTANCE_METHOD_DECLARATION">instanceMethod</info>() {
    <info textAttributesKey="DART_INSTANCE_GETTER_REFERENCE">instanceVar</info> +
        <info textAttributesKey="DART_INSTANCE_METHOD_REFERENCE">instanceMethod</info>();
  }

  <info textAttributesKey="DART_KEYWORD">static</info> <info textAttributesKey="DART_KEYWORD">get</info> <info textAttributesKey="DART_STATIC_GETTER_DECLARATION">staticGetter</info> {
    <info textAttributesKey="DART_KEYWORD">return</info> <info textAttributesKey="DART_STATIC_GETTER_REFERENCE">staticGetter</info>;
  }

  <info textAttributesKey="DART_KEYWORD">static</info> <info textAttributesKey="DART_KEYWORD">set</info> <info textAttributesKey="DART_STATIC_SETTER_DECLARATION">staticSetter</info>(<info textAttributesKey="DART_CLASS">num</info> <info textAttributesKey="DART_PARAMETER_DECLARATION">param</info>) {
    <info textAttributesKey="DART_PARAMETER_REFERENCE">param</info>;
    <info textAttributesKey="DART_STATIC_SETTER_REFERENCE">staticSetter</info> = 1;
  }

  <info textAttributesKey="DART_KEYWORD">get</info> <info textAttributesKey="DART_INSTANCE_GETTER_DECLARATION">instanceGetter</info> {
    <info textAttributesKey="DART_KEYWORD">return</info> <info textAttributesKey="DART_INSTANCE_GETTER_REFERENCE">instanceGetter</info>;
  }

  <info textAttributesKey="DART_KEYWORD">set</info> <info textAttributesKey="DART_INSTANCE_SETTER_DECLARATION">instanceSetter</info>(<info textAttributesKey="DART_DYNAMIC_PARAMETER_DECLARATION">param</info>) {
    <info textAttributesKey="DART_INSTANCE_SETTER_REFERENCE">instanceSetter</info> = 1;
    <info textAttributesKey="DART_FUNCTION_TYPE_ALIAS">Compare</info>;
    "see $<info textAttributesKey="DART_INSTANCE_GETTER_REFERENCE">mapLiteral</info> as well";
    "see ${<info textAttributesKey="DART_INSTANCE_GETTER_REFERENCE">mapLiteral</info> <error descr="The operator '+' isn't defined for the type 'Map<Object, Object>'." textAttributesKey="DART_ERROR">+</error> " $<info textAttributesKey="DART_KEYWORD">this</info> "} as well";
  }

  <info textAttributesKey="DART_TYPE_NAME_DYNAMIC">dynamic</info> <info textAttributesKey="DART_INSTANCE_METHOD_DECLARATION">abstractMethod</info>();

  <error descr="Annotation must be either a const variable reference or const constructor invocation." textAttributesKey="DART_ERROR"><info textAttributesKey="DART_ANNOTATION">@deprecated(</info>"foo")</error>
  <info textAttributesKey="DART_KEYWORD">var</info> <info textAttributesKey="DART_INSTANCE_FIELD_DECLARATION">listLiteral</info> = [1, "", <info textAttributesKey="DART_CLASS">Object</info>];

  <info textAttributesKey="DART_KEYWORD">var</info> <info textAttributesKey="DART_INSTANCE_FIELD_DECLARATION">mapLiteral</info> = {
    1 : "",
    <info textAttributesKey="DART_CLASS">Object</info> : <info textAttributesKey="DART_SYMBOL_LITERAL">#+</info>
  };
}

<info textAttributesKey="DART_KEYWORD">typedef</info> <info textAttributesKey="DART_CLASS">int</info> <info textAttributesKey="DART_FUNCTION_TYPE_ALIAS">Compare</info>(<info textAttributesKey="DART_CLASS">bool</info> <info textAttributesKey="DART_PARAMETER_DECLARATION">x</info>());
<info textAttributesKey="DART_KEYWORD">void</info> <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">f</info>(<info textAttributesKey="DART_KEYWORD">void</info> <info textAttributesKey="DART_KEYWORD">Function</info>() <info textAttributesKey="DART_PARAMETER_DECLARATION">a</info>, <info textAttributesKey="DART_CLASS">Function</info> <info textAttributesKey="DART_PARAMETER_DECLARATION">b</info>) {}
