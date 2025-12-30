import { Component } from '@angular/core';
import {CdkVirtualScrollViewport} from "@angular/cdk/scrolling";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    imports: [
        CdkVirtualScrollViewport
    ],
    standalone: true
})
export class AppComponent {
  title = 'host';
}
