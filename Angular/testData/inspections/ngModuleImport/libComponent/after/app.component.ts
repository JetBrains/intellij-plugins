import { Component } from '@angular/core';
import {SharedComponent} from "shared";

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [
        SharedComponent
    ],
    template: `
        <lib-shared></lib-shared>
    `
})
export class AppComponent {
}
