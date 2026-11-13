import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  template: `
    @let titleLength = title.length;
    <div>{{ titleLe<caret>ngth }}</div>
  `,
})
export class AppComponent {
  title = 'app';
}
