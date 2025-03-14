import {Component} from '@angular/core';
import {NgIf} from "@angular/common";

@Component({
  selector: 'comp',
  standalone: true,
  template: `
    
  `,
})
export class TestComponentOne {
  foo!: string
}

@Component({
  selector: 'test',
  standalone: true,
  imports: [NgIf, TestComponentOne],
  template: `
    <comp *ngIf="x" #cmp>
        {{cmp.foo}}
        {{cmp.<error descr="TS2339: Property 'bar' does not exist on type 'TestComponentOne'.">bar</error>}}
    </comp>
  `,
})
export class TestComponentTwo {
  x!: boolean;
}
