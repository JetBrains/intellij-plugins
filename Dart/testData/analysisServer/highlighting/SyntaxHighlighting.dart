<info textAttributesKey="DART_KEYWORD">library</info> <info textAttributesKey="DART_LIBRARY_NAME">foo.bar</info>;

<info textAttributesKey="DART_KEYWORD">import</info> "dart:core";
<info textAttributesKey="DART_KEYWORD">import</info> "dart:html"
<info textAttributesKey="DART_KEYWORD">as</info> <info textAttributesKey="DART_IMPORT_PREFIX">html</info>
    <info textAttributesKey="DART_KEYWORD">show</info> <info textAttributesKey="DART_CLASS">HtmlElement</info>,
    <weak_warning descr="The name document is shown, but not used." textAttributesKey="DART_HINT"><info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">document</info></weak_warning>,
    <weak_warning descr="The name EventListener is shown, but not used." textAttributesKey="DART_HINT"><info textAttributesKey="DART_FUNCTION_TYPE_ALIAS">EventListener</info></weak_warning>,
    <warning descr="'convertNativeObjectToDartMap' is deprecated" textAttributesKey="DEPRECATED_ATTRIBUTES"><weak_warning descr="The name convertNativeObjectToDartMap is shown, but not used." textAttributesKey="DART_HINT"><info textAttributesKey="DART_TOP_LEVEL_FUNCTION_REFERENCE">convertNativeObjectToDartMap</info></weak_warning></warning>,
    <weak_warning descr="The library 'dart:html' doesn't export a member with the shown name 'incorrect'" textAttributesKey="DART_HINT"><weak_warning descr="The name incorrect is shown, but not used." textAttributesKey="DART_HINT"><info textAttributesKey="DART_IDENTIFIER">incorrect</info></weak_warning></weak_warning>;

<info textAttributesKey="DART_KEYWORD">get</info> <info textAttributesKey="DART_TOP_LEVEL_GETTER_DECLARATION">topLevelGetter</info> {
  return <info textAttributesKey="DART_TOP_LEVEL_GETTER_REFERENCE">topLevelGetter</info>;
}

<info textAttributesKey="DART_KEYWORD">set</info> <info textAttributesKey="DART_TOP_LEVEL_SETTER_DECLARATION">topLevelSetter</info>(<info textAttributesKey="DART_CLASS">bool</info> <info textAttributesKey="DART_PARAMETER_DECLARATION">param</info>) {
  <info textAttributesKey="DART_TOP_LEVEL_SETTER_REFERENCE">topLevelSetter</info> = true;
}

<info textAttributesKey="DART_CLASS">Object</info> <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">topLevelVariable</info>;

void <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">topLevelFunction</info>(<info textAttributesKey="DART_DYNAMIC_PARAMETER_DECLARATION">param</info>) {
  <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_REFERENCE">topLevelFunction</info>(1);
  <info textAttributesKey="DART_TOP_LEVEL_GETTER_REFERENCE">topLevelVariable</info>;
  <info textAttributesKey="DART_DYNAMIC_PARAMETER_REFERENCE">param</info>;
  <info textAttributesKey="DART_LOCAL_FUNCTION_DECLARATION">innerFunction</info>() {}
  <info textAttributesKey="DART_LOCAL_FUNCTION_REFERENCE">innerFunction</info>();
  <info textAttributesKey="DART_IMPORT_PREFIX">html</info>.<info textAttributesKey="DART_CLASS">HtmlElement</info>;

  <info textAttributesKey="DART_LABEL">label</info>:
  while (true) {
    break <info textAttributesKey="DART_LABEL">label</info>;
  }

  new <info textAttributesKey="DART_CLASS">Foo</info>();
  new <info textAttributesKey="DART_CLASS">Foo</info>.<info textAttributesKey="DART_CONSTRUCTOR">from</info>();
  new <info textAttributesKey="DART_CLASS">Foo</info>.<info textAttributesKey="DART_CONSTRUCTOR">redirect</info>();
  new <info textAttributesKey="DART_CLASS">Foo</info>.<info textAttributesKey="DART_CONSTRUCTOR">factory</info>();
  new <info textAttributesKey="DART_CLASS">Foo2</info>(1);
  const <info textAttributesKey="DART_CLASS">Foo2</info>(1);
  <info textAttributesKey="DART_ENUM">Enum</info>.<info textAttributesKey="DART_ENUM_CONSTANT">EnumConstant</info>;
  var <info textAttributesKey="DART_DYNAMIC_LOCAL_VARIABLE_DECLARATION">dynamicLocalVar</info>;
  <info textAttributesKey="DART_DYNAMIC_LOCAL_VARIABLE_REFERENCE">dynamicLocalVar</info>;
}

enum <info textAttributesKey="DART_ENUM">Enum</info> {
  <info textAttributesKey="DART_ENUM_CONSTANT">EnumConstant</info>
}

class <info textAttributesKey="DART_CLASS">Foo</info> {
  <info textAttributesKey="DART_CLASS">Foo</info>(){}
  <info textAttributesKey="DART_CLASS">Foo</info>.<info textAttributesKey="DART_CONSTRUCTOR">from</info>(){}
  <info textAttributesKey="DART_CLASS">Foo</info>.<info textAttributesKey="DART_CONSTRUCTOR">redirect</info>() : this.<info textAttributesKey="DART_CONSTRUCTOR">from</info>();
  <weak_warning descr="This function declares a return type of 'Foo', but does not end with a return statement" textAttributesKey="DART_HINT"><info textAttributesKey="DART_KEYWORD">factory</info> <info textAttributesKey="DART_CLASS">Foo</info>.<info textAttributesKey="DART_CONSTRUCTOR">factory</info>() {}</weak_warning>
}

/// [<info textAttributesKey="DART_CLASS">Foo1</info>] is good []
class <info textAttributesKey="DART_CLASS">Foo1</info> {}

class <info textAttributesKey="DART_CLASS">Foo2</info><<info textAttributesKey="DART_TYPE_PARAMETER">Generic</info>> {
  final <info textAttributesKey="DART_TYPE_PARAMETER">Generic</info> <info textAttributesKey="DART_INSTANCE_FIELD_DECLARATION">x</info>;
  const <info textAttributesKey="DART_CLASS">Foo2</info>(this.<info textAttributesKey="DART_INSTANCE_FIELD_REFERENCE">x</info>);
}

<info textAttributesKey="DART_KEYWORD">abstract</info> class <info textAttributesKey="DART_CLASS">Bar</info>
    extends <info textAttributesKey="DART_CLASS">Object</info>
    with <info textAttributesKey="DART_CLASS">Foo1</info>
    <info textAttributesKey="DART_KEYWORD">implements</info> <info textAttributesKey="DART_CLASS">Foo</info> {

  <info textAttributesKey="DART_KEYWORD">static</info> const <info textAttributesKey="DART_STATIC_FIELD_DECLARATION">staticConst</info> = 1;
  <info textAttributesKey="DART_KEYWORD">static</info> var <info textAttributesKey="DART_STATIC_FIELD_DECLARATION">staticField</info>;
  var <info textAttributesKey="DART_INSTANCE_FIELD_DECLARATION">instanceVar</info>;

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
    return <info textAttributesKey="DART_STATIC_GETTER_REFERENCE">staticGetter</info>;
  }

  <info textAttributesKey="DART_KEYWORD">static</info> <info textAttributesKey="DART_KEYWORD">set</info> <info textAttributesKey="DART_STATIC_SETTER_DECLARATION">staticSetter</info>(<info textAttributesKey="DART_CLASS">num</info> <info textAttributesKey="DART_PARAMETER_DECLARATION">param</info>) {
    <info textAttributesKey="DART_PARAMETER_REFERENCE">param</info>;
    <info textAttributesKey="DART_STATIC_SETTER_REFERENCE">staticSetter</info> = 1;
  }

  <info textAttributesKey="DART_KEYWORD">get</info> <info textAttributesKey="DART_INSTANCE_GETTER_DECLARATION">instanceGetter</info> {
    return <info textAttributesKey="DART_INSTANCE_GETTER_REFERENCE">instanceGetter</info>;
  }

  <info textAttributesKey="DART_KEYWORD">set</info> <info textAttributesKey="DART_INSTANCE_SETTER_DECLARATION">instanceSetter</info>(<info textAttributesKey="DART_DYNAMIC_PARAMETER_DECLARATION">param</info>) {
    <info textAttributesKey="DART_INSTANCE_SETTER_REFERENCE">instanceSetter</info> = 1;
    <info textAttributesKey="DART_FUNCTION_TYPE_ALIAS">Compare</info>;
    "see $<info textAttributesKey="DART_INSTANCE_GETTER_REFERENCE">mapLiteral</info> as well";
    "see ${<info textAttributesKey="DART_INSTANCE_GETTER_REFERENCE">mapLiteral</info> + " $this "} as well";
  }

  <info textAttributesKey="DART_TYPE_NAME_DYNAMIC">dynamic</info> <info textAttributesKey="DART_INSTANCE_METHOD_DECLARATION">abstractMethod</info>();

  <info textAttributesKey="DART_ANNOTATION">@<error descr="The name 'deprecated' is not a class" textAttributesKey="DART_ERROR">deprecated</error>(</info>"foo"<info textAttributesKey="DART_ANNOTATION">)</info>
  var <info textAttributesKey="DART_INSTANCE_FIELD_DECLARATION">listLiteral</info> = [1, "", <info textAttributesKey="DART_CLASS">Object</info>];

  var <info textAttributesKey="DART_INSTANCE_FIELD_DECLARATION">mapLiteral</info> = {
    1 : "",
    <info textAttributesKey="DART_CLASS">Object</info> : <info textAttributesKey="DART_SYMBOL_LITERAL">#+</info>
  };
}

<info textAttributesKey="DART_KEYWORD">typedef</info> <info textAttributesKey="DART_CLASS">int</info> <info textAttributesKey="DART_FUNCTION_TYPE_ALIAS">Compare</info>(<info textAttributesKey="DART_CLASS">bool</info> <info textAttributesKey="DART_PARAMETER_DECLARATION">x</info>());