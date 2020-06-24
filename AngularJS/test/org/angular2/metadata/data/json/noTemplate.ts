import {Directive} from "@angular/core"

@Directive({
    selector: '[myHoverList]',
    host:{
        '(mouseenter)': 'hello()'
    }
})
export class HoverListDirective{
    @Output() testing : EventEmitter = new EventEmitter();
    @Input() testOne : string = "HELLO";
    @Input() testTwo : string;

    constructor(el : ElementRef, renderer : Renderer){}
}
