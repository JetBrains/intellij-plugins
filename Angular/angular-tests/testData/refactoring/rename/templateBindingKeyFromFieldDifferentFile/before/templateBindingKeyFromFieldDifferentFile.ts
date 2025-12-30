import {Directive, Input} from '@angular/core';

@Directive({
    selector: '[appClicks]',
    standalone: true,
})
export class AppClicksDirective {
    @Input()
    appClic<caret>ksParam!: string

    @Input()
    appClicks!: boolean
}