import {Directive, Input} from '@angular/core';

@Directive({
    selector: '[appClicks]',
    standalone: true,
})
export class AppClicksDirective {
    @Input()
    appClicksParam!: string

    @Input()
    appClicks!: boolean
}