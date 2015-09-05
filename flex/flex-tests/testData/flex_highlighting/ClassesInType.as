[Event(type="<error>ourClass</error>")]
[Event(type="aaa.xxx.<error>ClassesInType</error>")]
[Event(type="aaa.xxx.MyEvent")]
[Event(type=<error>"aaa.xxx"</error>)]
[Style(name="horizontalGridLineColor", type="int", arrayType="uint", format="Color", inherit="yes")]
[Style(type="Array", arrayType="Boolean")]
[Style(type="Class", arrayType="Function")]
[Style(type="Number", arrayType="XML")]
[Style(type="XMLList", arrayType="RegExp")]
[Style(name="headerColors", type="<error>Array2</error>", arrayType="<error>uint2</error>", format="Color", inherit="yes")]
[Style(name="type", type="Class")]
[HostComponent("spark.<error>components2</error>.<error>Button</error>")]
[HostComponent("spark.components.<error>Button2</error>")]
[HostComponent("spark.components.Button")]
[HostComponent("<error>Object</error>")]
[ArrayElementType("Object")]
[ArrayElementType("<error>foo</error>.<error>Bar</error>")]
[ArrayElementType(<error>"aaa"</error>)]
[ArrayElementType(<error>""</error>)]
[Inspectable(type="Vector")]
[Inspectable(arrayType="<error>Foo</error>")]
[SkinPart(type="String")]
[SkinPart(type="<error>Foo</error>")]

var c;

package <error>aaa.xxx</error> {
  import flash.events.Event;

  class <error>ClassesInType</error> {}
  class <error>MyEvent</error> extends Event{}
}