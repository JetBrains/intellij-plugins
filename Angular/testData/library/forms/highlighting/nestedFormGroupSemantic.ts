import {<symbolName descr="identifiers//exported function">Component</symbolName>} <info descr="null">from</info> '@angular/core';
import {<symbolName descr="identifiers//exported variable">FormControl</symbolName>, <symbolName descr="classes//exported class">FormGroup</symbolName>, <symbolName descr="classes//exported class">Validators</symbolName>} <info descr="null">from</info> '@angular/forms';

<info descr="decorator">@</info><info descr="decorator">Component</info>({
   <symbolName descr="instance field">selector</symbolName>: '<symbolName descr="HTML_TAG_NAME">nested-form-groups</symbolName>',
   <symbolName descr="instance field">template</symbolName>: `<inject descr="null">
        <form [formGroup]="<symbolName descr="instance field">form</symbolName>">
            <div formGroupName="<symbolName descr="form control">name</symbolName>">
                <input formControlName="<symbolName descr="form control">first</symbolName>" placeholder="First name"/>
                <div formGroupName="<symbolName descr="form control">foo</symbolName>">
                    <input formControlName="<symbolName descr="form control">bar</symbolName>" placeholder="Last name"/>
                </div>
                <div formGroupName="<symbolName descr="form control">bar</symbolName>">
                    <input formControlName="<symbolName descr="form control">baz</symbolName>" placeholder="Last name"/>
                </div>
            </div>
            <input formControlName="<symbolName descr="form control">email</symbolName>" placeholder="Email"/>
        </form>
    </inject>`,
  <symbolName descr="instance field">standalone</symbolName>: false,
})
export class <symbolName descr="classes//exported class">NestedFormGroupComp</symbolName> {
  <symbolName descr="instance field">form</symbolName> = new <symbolName descr="classes//exported class">FormGroup</symbolName>({
     <symbolName descr="instance field">name</symbolName>: new <symbolName descr="classes//exported class">FormGroup</symbolName>({
       <symbolName descr="instance field">first</symbolName>: new <symbolName descr="identifiers//exported variable">FormControl</symbolName>('Foo'),
       <symbolName descr="instance field">last</symbolName>: new <symbolName descr="identifiers//exported variable">FormControl</symbolName>('Bar'),
       <symbolName descr="instance field">foo</symbolName>: new <symbolName descr="classes//exported class">FormGroup</symbolName>({
         <symbolName descr="instance field">bar</symbolName>: new <symbolName descr="identifiers//exported variable">FormControl</symbolName>('Baz')
       })
     }),
     <symbolName descr="instance field">gender</symbolName>: new <symbolName descr="identifiers//exported variable">FormControl</symbolName>(),
     <symbolName descr="instance field">address</symbolName>: new <symbolName descr="classes//exported class">FormGroup</symbolName>({}),
     <symbolName descr="instance field">email</symbolName>: new <symbolName descr="identifiers//exported variable">FormControl</symbolName>(),
  });

  <symbolName descr="instance field">foo</symbolName>!: <symbolName descr="classes//exported class">FormGroup</symbolName>

  <symbolName descr="instance method">check</symbolName>() {
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>(['<symbolName descr="form control">name</symbolName>', '<symbolName descr="form control">foo</symbolName>', '<symbolName descr="form control">bar</symbolName>'])
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>(['<symbolName descr="form control">email</symbolName>', '<symbolName descr="form control">first</symbolName>'])
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>('<symbolName descr="form control">name</symbolName>');
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>('<symbolName descr="form control">name</symbolName><info descr="dot">.</info><symbolName descr="form control">first</symbolName>');
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>('<symbolName descr="form control">name</symbolName><info descr="dot">.</info><symbolName descr="form control">first</symbolName>');
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>('<symbolName descr="form control">name</symbolName><info descr="dot">.</info><symbolName descr="form control">foo</symbolName><info descr="dot">.</info><symbolName descr="form control">bar</symbolName>');
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>('<symbolName descr="form control">name</symbolName><info descr="dot">.</info><symbolName descr="form control">bar</symbolName>');

    this.<symbolName descr="instance field">foo</symbolName>.<symbolName descr="instance method">get</symbolName>('name');
  }
}
