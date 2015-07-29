/**
    Add information about class here
*/
function Foo() {
	if (_biInPrototype) return;
	BiEventTarget.call(this);
}
_p = _biExtend(Foo, BiEventTarget, "Foo");

_p.methodName = function () {
	BiLog.out("Foo:methodName: " + ""); // Works here
};

Foo<caret>