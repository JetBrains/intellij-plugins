import {Component, model} from "@angular/core"

@Component({
  selector: 'app-child-input-test',
  template: ``,
})
export class ChildComponent {
    test = model<{ key: string }>(undefined);
}

@Component({
  selector: 'app-input-test',
  imports: [ChildComponent],
  template: `
    <app-child-input-test [test]="test"/>
  `,
})
export class ParentComponent {
  protected test = {key: "value"};
}