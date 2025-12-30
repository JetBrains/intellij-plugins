import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  template: `
    <div>
      {{ <error descr="Untagged template syntax is supported only in Angular 19.2 and above.">\`supported \${ bar }\`</error> }}
      @if ( title === <error descr="Untagged template syntax is supported only in Angular 19.2 and above.">\`it-\${ <error descr="TS2339: Property 'foo' does not exist on type 'AppComponent'.">foo</error> }\`</error>) {
        do this!
      }
    </div>`,
})
export class AppComponent {
  title = 'ng192';
  bar = 12;
}
