import { Component} from '@angular/core';
import { PercentPipe } from '@angular/common';

@Component({
  selector: 'app-test',
  imports: [PercentPipe],
  template: `
      <main class="main">
        <div [title]="1 + 96.5 + 12 | percent : '4' : 'pl' "></div>
        {{ <error descr="TS2339: Property 'foo' does not exist on type 'TestComponent'.">foo</error> }}
      </main>
  `,
  standalone: true,
})
export class TestComponent {

  <warning descr="Unused method test">test</warning>() {
    <error descr="TS2304: Cannot find name 'foo'.">foo</error>()
  }
}