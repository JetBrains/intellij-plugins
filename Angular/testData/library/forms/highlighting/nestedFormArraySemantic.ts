import {<symbolName descr="identifiers//exported function">Component</symbolName>} <info descr="null">from</info> '@angular/core';
import {<symbolName descr="identifiers//exported variable">FormControl</symbolName>, <symbolName descr="classes//exported class">FormGroup</symbolName>, <symbolName descr="classes//exported class">FormArray</symbolName>} <info descr="null">from</info> '@angular/forms';

<info descr="decorator">@</info><info descr="decorator">Component</info>({
   <symbolName descr="instance field">selector</symbolName>: '<symbolName descr="HTML_TAG_NAME">nested-form-groups</symbolName>',
   <symbolName descr="instance field">template</symbolName>: `<inject descr="null">
        <form [formGroup]="<symbolName descr="instance field">form</symbolName>">
            <div formGroupName="<symbolName descr="form control">first</symbolName>">
                <input formControlName="<symbolName descr="form control">first</symbolName>" placeholder="First name"/>
            </div>
            <div formArrayName="<symbolName descr="form control">first</symbolName>">
                <input formControlName="<symbolName descr="form control">first</symbolName>" placeholder="First name"/>
                <input formControlName="<symbolName descr="form array control">12</symbolName>" placeholder="First name"/>
                <div formGroupName="<symbolName descr="form array control">12</symbolName>"></div>
                <div formArrayName="<symbolName descr="form array control">12</symbolName>"></div>
            </div>
            <form formGroupName="<symbolName descr="form control">group</symbolName>">
                <input formControlName="<symbolName descr="form control">email</symbolName>" placeholder="Email"/>
                <div formArrayName="<symbolName descr="form control">members</symbolName>">
                    <input formControlName="<symbolName descr="form control">email</symbolName>" placeholder="Email"/>
                    <input formControlName="<symbolName descr="form array control">23</symbolName>" placeholder="Email"/>
                </div>
            </form>
        </form>
    </inject>`,
  <symbolName descr="instance field">standalone</symbolName>: false,
})
export class <symbolName descr="classes//exported class">NestedFormGroupComp</symbolName> {
  <symbolName descr="instance field">form</symbolName> = new <symbolName descr="classes//exported class">FormGroup</symbolName>({
     <symbolName descr="instance field">first</symbolName>: new <symbolName descr="classes//exported class">FormArray</symbolName>([new <symbolName descr="identifiers//exported variable">FormControl</symbolName>('Foo'), new <symbolName descr="identifiers//exported variable">FormControl</symbolName>('Foo')]),
     <symbolName descr="instance field">group</symbolName>: new <symbolName descr="classes//exported class">FormGroup</symbolName>({
       <symbolName descr="instance field">name</symbolName>: new <symbolName descr="identifiers//exported variable">FormControl</symbolName>(),
       <symbolName descr="instance field">members</symbolName>: new <symbolName descr="classes//exported class">FormArray</symbolName>([new <symbolName descr="identifiers//exported variable">FormControl</symbolName>('Foo'), new <symbolName descr="identifiers//exported variable">FormControl</symbolName>('Foo')])
     }),
  });

  <symbolName descr="instance method">check</symbolName>() {
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>(['<symbolName descr="form control">first</symbolName>', '<symbolName descr="form array control">1</symbolName>'])
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>(['<symbolName descr="form control">first</symbolName>', '<symbolName descr="form control">foo</symbolName>'])
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>(['<symbolName descr="form control">group</symbolName>', '<symbolName descr="form control">members</symbolName>', '<symbolName descr="form array control">23</symbolName><info descr="dot">.</info><symbolName descr="form array control">4</symbolName>'])
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>(['<symbolName descr="form control">first</symbolName>', '<symbolName descr="form array control">1</symbolName>'])
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>(['<symbolName descr="form control">first</symbolName>', '<symbolName descr="form control">foo</symbolName>', '<symbolName descr="form control">bar</symbolName>'])
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>(['<symbolName descr="form control">group</symbolName>', '<symbolName descr="form control">members</symbolName>', '<symbolName descr="form array control">23</symbolName><info descr="dot">.</info><symbolName descr="form array control">4</symbolName>'])
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>('<symbolName descr="form control">first</symbolName><info descr="dot">.</info><symbolName descr="form array control">1</symbolName>')
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>('<symbolName descr="form control">first</symbolName><info descr="dot">.</info><symbolName descr="form control">foo</symbolName>')
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>('<symbolName descr="form control">group</symbolName><info descr="dot">.</info><symbolName descr="form control">members</symbolName><info descr="dot">.</info><symbolName descr="form array control">23</symbolName><info descr="dot">.</info><symbolName descr="form array control">4</symbolName>')
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>('<symbolName descr="form control">first</symbolName><info descr="dot">.</info><symbolName descr="form array control">1</symbolName>')
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>('<symbolName descr="form control">first</symbolName><info descr="dot">.</info><symbolName descr="form control">foo</symbolName><info descr="dot">.</info><symbolName descr="form control">bar</symbolName>')
    this.<symbolName descr="instance field">form</symbolName>.<symbolName descr="instance method">get</symbolName>('<symbolName descr="form control">group</symbolName><info descr="dot">.</info><symbolName descr="form control">members</symbolName><info descr="dot">.</info><symbolName descr="form array control">23</symbolName><info descr="dot">.</info><symbolName descr="form array control">4</symbolName>')
  }
}
