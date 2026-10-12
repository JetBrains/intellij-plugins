import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  template: `<div>my component</div>`,
  styles: `
    @use '@angular/material' as mat;
    @use 'theme';

    div {
      @include mat.elevation(3);
      @include theme.theme-color();
    }
  `,
})
export class AppComponent {
}
