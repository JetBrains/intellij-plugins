import {Component} from '@angular/core';
import {SHARED_IMPORTS} from './sharedimports';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <test></test>
  `,
})
export class AppComponent {
}
