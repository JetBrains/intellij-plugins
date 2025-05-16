import {Component, input} from "@angular/core"

@Component({
  selector: 'app-child-input-test',
  template: ``,
})
export class ChildComponent {
    test = input(0);
}

@Component({
  selector: 'app-input-test',
  imports: [ChildComponent],
  template: `
    <app-child-input-test [test]="test"/>
  `,
})
export class ParentComponent {
  protected test = 1;
}