import { Component} from '@angular/core';
import { PercentPipe } from '@angular/common';

@Component({
  selector: 'app-test',
  imports: [<error descr="Pipe PercentPipe is never used in a component template">PercentPipe</error>],
  template: `
      <main class="main">
        <div [title]="1 + 96.5 + 12 | <error descr="Unresolved pipe pearcent">pearcent</error> : '4' : 'pl' "></div>
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