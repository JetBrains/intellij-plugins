import {Component} from '@angular/core';

@Component({
  standalone: true,
  selector: 'app-test',
  template: `
      <!-- Can use both alias and $-prefixed name -->
      @for (item of items; track index; let index = $index; let total= $count) {
        <p>Item #{{ index }} of {{total}}: {{ item.name }}</p>
      }
      @for (item of items; track $index; let index = $index; let total= $count) {
        <p>Item #{{ $index }} of {{$count}}: {{ item.name }}</p>
        <p>Item #{{ index }} of {{total}}: {{ item.<error descr="TS2551: Property 'nme' does not exist on type 'Item'. Did you mean 'name'?">nme</error> }}</p>
      }
      
      <!-- Cannot track total -->
      @for (item of items; track <error descr="Cannot access total inside of a track expression. Only item, $index, index and properties on the containing component are available to this expression.">total</error>; let index = $index; let total= $count) {
        <p>Item #{{ index }} of {{total}}: {{ item.name }}</p>
      }
      
      <!-- Cannot track $count -->
      @for (item of items; track <error descr="Cannot access $count inside of a track expression. Only item, $index and properties on the containing component are available to this expression.">$count</error>;) {
        <p>Item #{{ $index }}: {{ item.name }}</p>
      }
    `,
})
export class TestComponent {
  items: Item[] = [
    { id: '1', status: 'open', name: 'Task 1' },
    { id: '2', status: 'done', name: 'Task 1' },
    { id: '3', status: 'open', name: 'Task 2' },
  ];
}

type Item = {
  id: string;
  status: string;
  name: string;
};
