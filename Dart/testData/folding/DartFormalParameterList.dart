// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
library foo;

class A <fold text='{...}' expand='true'>{
int x, y, z;
A(this.x, this.y, this.z);
}</fold>

class B <fold text='{...}' expand='true'>{
int a, b, c, d, e;
B<fold text='(...)' expand='true'>(
this.a,
this.b,
this.c,
this.d,
this.e
)</fold>;
}</fold>

class C <fold text='{...}' expand='true'>{
int a, b, c, d, e;
B<fold text='(...)' expand='true'>({
this.a,
this.b,
this.c,
this.d,
this.e
})</fold>;
}</fold>

class D <fold text='{...}' expand='true'>{
int a, b, c, d, e;
B<fold text='(...)' expand='true'>([
this.a,
this.b,
this.c,
this.d,
this.e
])</fold>;
}</fold>
