import {Component, Input} from "@angular/core";

@Component({
  inputs: [
    { name: "obj1", transform: (value: { foo1: number }): boolean => Boolean(value) },
  ],
  selector: 'app-test3',
  standalone: true,
  template: ``
})
export class TestComponent3 {
  @Input({transform: (value: { foo2: number }): boolean => Boolean(value)})
  obj2!: boolean;

  @Input()
  obj3!: { foo3: number };
}