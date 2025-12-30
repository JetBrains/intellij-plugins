import {Component, Input, Directive, Output, EventEmitter, input} from '@angular/core';

@Directive({standalone: true, selector: '[foo]'})
export class TestDir {
    @Input()
    protected protectedField!: string;
    @Input()
    private <weak_warning descr="TS6133: 'privateField' is declared but its value is never read.">privateField</weak_warning>!: string;
    @Input()
    readonly readonlyField!: string;
    readonly readonlySignalField = input<string>();

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
           <error descr="TS2540: Cannot assign to 'readonlyField' because it is a read-only property.">[readonlyField]</error>="value"
           [readonlySignalField]="value"
           <error descr="TS2445: Property 'protectedField' is protected and only accessible within class 'TestDir' and its subclasses.">[protectedField]</error>="value"
           <error descr="TS2341: Property 'privateField' is private and only accessible within class 'TestDir'.">[privateField]</error>="value"

           (readonlyEvent)="foo()"
           (protectedEvent)="foo()"
           (privateEvent)="foo()"
      ></div>
      <cmp
              <error descr="TS2540: Cannot assign to 'readonlyField' because it is a read-only property.">[readonlyField]</error>="readonlyField"
              [protectedField]="protectedField"
              <error descr="TS2341: Property 'privateField' is private and only accessible within class 'TestComponent'.">[privateField]</error>="<error descr="TS2341: Property 'privateField' is private and only accessible within class 'TestComponent'.">privateField</error>"

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
