import {Directive} from '@angular/core';

@Directive({
    selector: '[appBold]',
    standalone: true,
    exportAs: "bolder"
})
export class BoldDirective {
}
