import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  template: `
    <div>
      {{ \`supported \${ bar }\` }}
      @if ( title === \`it-\${ <error descr="TS2339: Property 'foo' does not exist on type 'AppComponent'.">foo</error> }\`) {
        do this!
      }
    </div>`,
})
export class AppComponent {
  title = 'ng192';
  bar = 12;
}
