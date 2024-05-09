var _pipe1: MyPipe = null!;
const _ctor1: <T = any>(init: Pick<TestIf<T>, "ngIf" | "ngIfThen">) => TestIf<T> = null!;
const _ctor2: <T extends number = any>(init: Pick<TestFoo<T>, "foo" | "foo2">) => TestFoo<T> = null!;
var _t1 = _ctor1({ "ngIf": this.title != 'foo', "ngIfThen": null as any });
_t1.ngIf = this.title != 'foo';
var _t2: any = null!;
if (TestIf.ngTemplateContextGuard(_t1, _t2) && this.title != 'foo') {
var _t3 = _t2.ngIf;
var _t5: RouterOutlet = null!;
var _t4 = _t5;
var _t6 = _ctor2({ "foo": {
    "a": _t4
  }, "foo2": null as any });
_t6.foo = {
  "a": _t4
};
var _t7 = _ctor2({ "foo": {
    "a": _t4
  }, "foo2": _pipe1.transform(12) });
_t7.foo = ({
  "a": _t4
});
_t7.foo2 = _pipe1.transform(12);
}
"" + this.minutes;
"" + this.minutes;