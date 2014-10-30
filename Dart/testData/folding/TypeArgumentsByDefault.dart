library foo;
class A<T>{}
// verify the type arguments in class declarations aren't folded
class B<T> extends A<T> {}

List<String> list1 = new List<fold text='<~>' expand='false'><String></fold>();

f() <fold text='{...}' expand='true'>{
A<String> a = new A<fold text='<~>' expand='false'><String></fold>();
}</fold>
