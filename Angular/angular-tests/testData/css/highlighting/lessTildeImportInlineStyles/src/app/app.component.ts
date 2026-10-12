import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  template: `<div class="cls">my component</div>`,
  styles: `
    @import '~node_modules/my-lib/styles/variables';
    @import 'my-lib/styles/variables';

    .cls {
      color: @my-color;
    }
  `,
})
export class AppComponent {
}
