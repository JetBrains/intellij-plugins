import {Component, ViewChild} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
        @if (mode; as mode) {
            @switch (mode) {
                @case ('one') {
                    <div #component></div>
                }
                @case ('two') {
                    <span #component></span>
                }
            }
        }
    `
})
export class AppComponent {
  mode: 'one' | 'two' = 'one';

  @ViewChild('component') component: any;
  @ViewChild('<warning descr="Unrecognized name">compnent</warning>') compnent: any;
}
