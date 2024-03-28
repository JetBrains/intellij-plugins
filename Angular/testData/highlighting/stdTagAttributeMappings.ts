import {Component, Input} from "@angular/core";

@Component({
  selector: 'field-error',
  standalone: true,
  template: ``,
})
export class FieldError {
  @Input({required: true}) public for = "";
}

@Component({
  selector: 'dynamic-form-question',
  standalone: true,
  imports: [FieldError],
  template: `
    <div <error descr="Property htmlFor is not provided by any applicable directives nor by <div> element">[htmlFor]</error>="'foo'" <error descr="Property for is not provided by any applicable directives nor by <div> element">[for]</error>="'foo'"></div>
    <label [htmlFor]="'foo'" [for]="'foo'"></label>
    <field-error <error descr="Property htmlFor is not provided by any applicable directives nor by <field-error> element">[htmlFor]</error>="'foo'" [for]="'foo'"></field-error>
  `
})
export class DynamicFormQuestion {
}
