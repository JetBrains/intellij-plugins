import {Component, Directive, Input, Optional, Self, TemplateRef} from "@angular/core";

@Directive({
   selector: '[foo]',
   standalone: true
 })
export class FooDirective {
  public constructor(@Optional() @Self() private readonly <weak_warning descr="TS6138: Property 'template' is declared but its value is never read.">template</weak_warning>: TemplateRef<void> ) {

  }
  @Input() foo!: string;
}

@Directive({
   selector: '[bar]',
   standalone: true
 })
export class BarDirective {
  public constructor(@Self() private readonly <weak_warning descr="TS6138: Property 'template' is declared but its value is never read.">template</weak_warning>: TemplateRef<void> ) {

  }
  @Input() bar!: string;
}

@Component({
   selector: 'app-test',
   standalone: true,
   imports: [FooDirective, BarDirective],
   template: `
        <div <error descr="TS2322: Type 'number' is not assignable to type 'string'.">[foo]</error>="12"></div>
        <div <error descr="TS2322: Type 'number' is not assignable to type 'string'.">*foo</error>="12"></div>
        <div <error descr="Property bar is not provided by any applicable directives nor by <div> element">[bar]</error>="12"></div>
        <div <error descr="TS2322: Type 'number' is not assignable to type 'string'.">*bar</error>="12"></div>
    `,
 })
export class TestComponent {
}