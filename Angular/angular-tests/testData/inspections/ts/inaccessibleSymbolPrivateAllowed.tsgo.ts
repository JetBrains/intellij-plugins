import {Component, Directive, EventEmitter, Input, Output} from '@angular/core';

export class Model {
  private a = 1;
  b = 2;
}

@Directive({selector: '[foo]'})
export class TestDir {
  @Input()
  private privateField!: string;
  @Output()
  private privateEvent!: EventEmitter<string>;
}

@Component({
  selector: 'cmp',
  template: `
    {{ privateField }} {{ protectedField }} {{ publicField }}
    {{ this.privateField }}
    {{ privateMethod() }}
    {{ privateModel.<error descr="TS2341: Property 'a' is private and only accessible within class 'Model'.">a</error> }}
    {{ privateModel.b }}
    <div (click)="privateSetter = 12"></div>
    <div foo
         <error descr="TS2341: Property 'privateField' is private and only accessible within class 'TestDir'.">[privateField]</error>="privateField"
         (privateEvent)="privateMethod()"
    ></div>
  `,
  host: {
    '[id]': 'privateField',
    '(click)': 'privateMethod()',
  },
  imports: [TestDir],
})
export class TestComponent {
  private readonly privateField = '1';
  protected readonly protectedField = '2';
  public readonly publicField = '3';
  private readonly privateModel = new Model();

  private privateMethod() {
    return 1;
  }

  private set privateSetter(value: number) {
    console.log(value);
  }
}
