package
{
import mx.core.UIComponent;

import mx.core.IDeferredInstance;

[DefaultProperty("ff")]
public class Test
{
	public var name:String;

	public var ff:UIComponent;

	private var _tabs:IDeferredInstance;
	[InstanceType("Array")]
    [ArrayElementType("mx.core.UIComponent")]
	public function set tabs(value:IDeferredInstance):void
	{
		_tabs = value;
	}
}
}