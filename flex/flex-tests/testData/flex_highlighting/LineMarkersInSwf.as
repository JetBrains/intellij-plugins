
<error descr="Access modifier allowed for class members only">public</error> interface <lineMarker descr="Has implementations">IFoo</lineMarker>
{
  native function abc():*;
}


[ExcludeClass]
<error descr="Access modifier allowed for class members only">public</error> class _5fbf1214009f16e9465fe2a742dcebb16563a1691d43c9dfb9c86d82676bc740_flash_display_Sprite extends flash.display.Sprite
{
  native public function _5fbf1214009f16e9465fe2a742dcebb16563a1691d43c9dfb9c86d82676bc740_flash_display_Sprite():*;
  native public function allowDomainInRSL(... rest):void;
  native public function allowInsecureDomainInRSL(... rest):void;
}


<error descr="Access modifier allowed for class members only">public</error> interface <lineMarker descr="Has implementations">IFooEx</lineMarker>
  extends IFoo
{

  native function zzz():*;
}


<error descr="Access modifier allowed for class members only">public</error> class <lineMarker descr="Has subclasses">Foo</lineMarker> extends Object
  implements IFooEx
{
  native public function Foo():*;
  native public function abc():*;
  native public function zzz():*;
}


<error descr="Access modifier allowed for class members only">public</error> class FooEx extends Foo
{
  native public function FooEx():*;

  native public override function abc():*;
  native public override function zzz():*;
}

