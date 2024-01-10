import {Directive, Input, TemplateRef, ViewContainerRef} from '@angular/core';

@Directive({
    selector: '[appUnless], [appIf]'
})
export class UnlessDirective {
    constructor(
        private templateRef: TemplateRef<any>,
        private viewContainer: ViewContainerRef) { }

    @Input() set appUnless(condition: boolean) {

    }
    @Input() set appIf(condition: boolean) {

    }
}
