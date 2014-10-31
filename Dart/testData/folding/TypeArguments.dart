library foo;
class A<T>{}
// verify the type arguments in class declarations aren't folded
class B<T> extends A<T> {}

List<Map<String,Object>> list1 = new List<fold text='<~>' expand='true'><Map<String,Object>></fold>();

f() <fold text='{...}' expand='true'>{
A<String> a = new A<fold text='<~>' expand='true'><String></fold>();
}</fold>
