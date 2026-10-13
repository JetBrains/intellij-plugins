import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  template: `<div class="cls">my component</div>`,
  styles: [`
    @import '~node_modules/bootstrap/scss/functions';
    @import '~node_modules/bootstrap/scss/variables';
    @import '~node_modules/<error descr="Cannot resolve file 'foo'">foo</error>';

    .cls {
      color: $red-100;
    }
  `],
})
export class AppComponent {
}
