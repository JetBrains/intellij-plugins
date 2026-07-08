import { Component} from '@angular/core';
import { PercentPipe } from '@angular/common';

@Component({
  selector: 'app-test',
  imports: [PercentPipe],
  template: `
      <main class="main">
        <div [title]="1 + 96.5 + 12 | percent : '4' : 'pl' "></div>
        {{ foo }}
      </main>
  `,
  standalone: true,
})
export class TestComponent {

  test() {
    foo()
  }
}