import {Component} from '@angular/core';

@Component({
  selector: 'app-inline',
  template: `
    <span>{{ title }} app is running!</span>
    <img src="assets/icon_small.png" alt="img">
    <img src="/assets/icons/menu.svg" alt="img">
    <img src="favicon.ico" alt="img">
    <img src="assets/<warning descr="Cannot resolve file 'missing.png'">missing.png</warning>" alt="img">
    <img src="<warning descr="Cannot resolve file 'not-an-asset.png'">not-an-asset.png</warning>" alt="img">
  `,
})
export class InlineComponent {
  title = 'untitled3';
}
