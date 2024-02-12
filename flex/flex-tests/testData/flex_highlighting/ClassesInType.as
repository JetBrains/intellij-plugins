[Event(type="<error descr="Unresolved variable or type 'ourClass'">ourClass</error>")]
[Event(type="aaa.xxx.<error descr="Expected class flash.events.Event or descendant">ClassesInType</error>")]
[Event(type="aaa.xxx.MyEvent")]
[Event(type=<error descr="Qualified class name expected">"aaa.xxx"</error>)]
[Style(name="horizontalGridLineColor", type="int", arrayType="uint", format="Color", inherit="yes")]
[Style(type="Array", arrayType="Boolean")]
[Style(type="Class", arrayType="Function")]
[Style(type="Number", arrayType="XML")]
[Style(type="XMLList", arrayType="RegExp")]
[Style(name="headerColors", type="<error descr="Unresolved variable or type 'Array2'">Array2</error>", arrayType="<error descr="Unresolved variable or type 'uint2'">uint2</error>", format="Color", inherit="yes")]
[Style(name="type", type="Class")]
[HostComponent("spark.<error descr="Unresolved variable or type 'components2'">components2</error>.<error descr="Unresolved variable or type 'Button'">Button</error>")]
[HostComponent("spark.components.<error descr="Unresolved variable or type 'Button2'">Button2</error>")]
[HostComponent("spark.components.Button")]
[HostComponent("<error descr="Expected class spark.components.supportClasses.SkinnableComponent or descendant">Object</error>")]
[ArrayElementType("Object")]
[ArrayElementType("<error descr="Unresolved variable or type 'foo'">foo</error>.<error descr="Unresolved variable or type 'Bar'">Bar</error>")]
[ArrayElementType(<error descr="Qualified class name expected">"aaa"</error>)]
[ArrayElementType(<error descr="Qualified class name expected">""</error>)]
[Inspectable(type="Vector")]
[Inspectable(arrayType="<error descr="Unresolved variable or type 'Foo'">Foo</error>")]
[SkinPart(type="String")]
[SkinPart(type="<error descr="Unresolved variable or type 'Foo'">Foo</error>")]

var c;

<error descr="Package should be first statement in file">package</error> <error descr="Package name 'aaa.xxx' does not correspond to file path ''">aaa.xxx</error> {
  import flash.events.Event;

  class <error descr="More than one externally visible symbol defined in file">ClassesInType</error> {}
  class <error descr="Class 'MyEvent' should be defined in file 'MyEvent.as'"><error descr="More than one externally visible symbol defined in file">MyEvent</error></error> extends Event{}
}