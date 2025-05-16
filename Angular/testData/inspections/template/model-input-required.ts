import {Component} from "@angular/core"

@Component({
  selector: 'app-child-input-test',
  template: ``,
})
export class ChildComponent {}

@Component({
  selector: 'app-input-test',
  imports: [ChildComponent],
  template: `
    <app-child-input-test <weak_warning descr="Property test is not provided by any applicable directives nor by <app-child-input-test> element">[test]</weak_warning>="test"/>
  `,
})
export class ParentComponent {
  protected test = "string";
}