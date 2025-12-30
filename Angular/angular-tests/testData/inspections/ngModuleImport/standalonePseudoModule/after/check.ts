import {Component} from '@angular/core';
import {FOO_COMPONENT_EXPORT_DECLARE_CONST_READ_ONLY} from "./foo";

@Component({
   selector: 'app-root',
   standalone: true,
    imports: [
        FOO_COMPONENT_EXPORT_DECLARE_CONST_READ_ONLY
    ],
   templateUrl: './check.html',
})
export class AppComponent {
}
