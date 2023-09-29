import {Component, NgModule, Input, Directive, Output, EventEmitter} from '@angular/core';

@Directive({standalone: true, selector: '[foo]'})
export class TestDir {
    @Input()
    protected protectedField!: string;
    @Input()
    private privateField!: string;
    @Input()
    readonly readonlyField!: string;

    @Output()
    protected protectedEvent!: EventEmitter<string>;
    @Output()
    private privateEvent!: EventEmitter<string>;
    @Output()
    readonly readonlyEvent!: EventEmitter<string>;
}
@Directive({standalone: true, selector: '[foo]'})
export class TestDir2 {
    @Input()
    protectedField!: string;
    @Input()
    privateField!: string;
    @Input()
    readonlyField!: string;
}

@Component({
    selector: 'cmp',
    template: `
      <div foo
           <error descr="Cannot assign to input  readonlyField  because it is a read-only property.">[readonlyField]</error>="value"
           <error descr="Field  protectedField  is  protected  and only accessible within class  TestDir  and its subclasses">[protectedField]</error>="value"
           <error descr="Field  privateField  is  private  and only accessible within class  TestDir ">[privateField]</error>="value"

           (readonlyEvent)="foo()"
           (protectedEvent)="foo()"
           (privateEvent)="foo()"
      ></div>
      <cmp
              <error descr="Cannot assign to input  readonlyField  because it is a read-only property.">[readonlyField]</error>="readonlyField"
              [protectedField]="protectedField"
              <error descr="Field  privateField  is  private  and only accessible within class  TestComponent ">[privateField]</error>="<error descr="Field  privateField  is  private  and only accessible within class  TestComponent ">privateField</error>"

              (readonlyEvent)="foo()"
              (protectedEvent)="foo()"
              (privateEvent)="foo()"
      ></cmp>
    `,
    imports: [
        TestDir, TestDir2
    ],
    standalone: true
})
export class TestComponent {
    value = "value";

    @Input()
    protected protectedField!: string;
    @Input()
    private privateField!: string;
    @Input()
    readonly readonlyField!: string;

    @Output()
    protected protectedEvent!: EventEmitter<string>;
    @Output()
    private privateEvent!: EventEmitter<string>;
    @Output()
    readonly readonlyEvent!: EventEmitter<string>;

    foo() {

    }
}
