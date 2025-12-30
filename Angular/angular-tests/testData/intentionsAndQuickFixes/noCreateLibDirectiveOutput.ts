import {Component} from '@angular/core';
import {CdkScrollable} from "@angular/cdk/overlay";

@Component({
    selector: 'app-root',
    imports: [CdkScrollable],
    standalone: true,
    template: `
      <div cdk-scrollable (f<caret>oo)="check($event)"></div>`
})
export class AppComponent {

  check(value: {bar: number}) {

  }

}
