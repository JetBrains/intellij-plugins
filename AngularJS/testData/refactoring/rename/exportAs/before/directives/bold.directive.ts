import {Directive} from '@angular/core';

@Directive({
    selector: '[appBold]',
    standalone: true,
    exportAs: "  bo<caret>ld  ,  bar "
})
export class BoldDirective {
}
