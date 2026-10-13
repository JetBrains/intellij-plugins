import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  template: `<div>my component</div>`,
  styles: `
    @use '@angular/material' as mat;
    @use 'theme';
    @use '<error descr="Cannot resolve file 'foo'">foo</error>';

    div {
      @include mat.elevation(3);
      @include theme.theme-color();
    }
  `,
})
export class AppComponent {
}
