import { Component} from '@angular/core';
import { PercentPipe } from '@angular/common';

@Component({
  selector: 'app-test',
  imports: [<error descr="Pipe PercentPipe is never used in a component template">PercentPipe</error>],
  template: `
      <main class="main">
        <div [title]="1 + 96.5 + 12 | <error descr="Unresolved pipe pearcent">pearcent</error> : '4' : 'pl' "></div>
        {{ <error descr="Unresolved variable or type foo">foo</error> }}
      </main>
  `,
  standalone: true,
})
export class TestComponent {

  <warning descr="Unused method test">test</warning>() {
    <error descr="Unresolved function or method foo()">foo</error>()
  }
}