import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  template: `
    <div>
      {{ \`supported \${ bar }\` }}
      @if ( title === \`it-\${ foo }\`) {
        do this!
      }
    </div>`,
})
export class AppComponent {
  title = 'ng192';
  bar = 12;
}
