import {Component, Directive, ElementRef, EventEmitter, HostListener, Input, OnInit, Output} from '@angular/core';

// @ts-ignore
@Directive({
    selector: '[appBold]',
    standalone: true,
    exportAs: "boldDir"
})
export class BoldDirective {
    @Output() hover = new EventEmitter()

    constructor(private hostElement: ElementRef<HTMLElement>) {
    }

    @HostListener('mouseenter')
    onMouseEnter() {
        this.hostElement.nativeElement.style.fontWeight = 'bold';
        this.hover.emit();
    }

    @HostListener('mouseleave')
    onMouseLeave() {
        this.hostElement.nativeElement.style.fontWeight = 'normal';
    }
}

@Directive({
    selector: '[appUnderline]',
    standalone: true
})
export class UnderlineDirective {
    @Input() color = 'black';

    constructor(private hostElement: ElementRef<HTMLElement>) {
    }

    @HostListener('mouseenter')
    onMouseEnter() {
        this.hostElement.nativeElement.style.textDecoration = 'underline dotted';
        this.hostElement.nativeElement.style.textDecorationColor = this.color;
    }

    @HostListener('mouseleave')
    onMouseLeave() {
        this.hostElement.nativeElement.style.textDecoration = 'none';
        this.hostElement.nativeElement.style.textDecorationColor = 'none';
    }
}

@Directive({
    selector: '[appMouseenter]',
    standalone: true,
    exportAs: "boldDir,mouseDir",
    hostDirectives: [{
        directive: BoldDirective,
        outputs: ['hover']
    }, {
        directive: UnderlineDirective,
        inputs: ['color: underlineColor']
    }]
})
export class MouseenterDirective {

    title = "mouse-title"
    constructor() {
    }
}

@Component({
    standalone: true,
    selector: 'resolved',
    template: "",
    styleUrls: ['./test.component.css'],
    /* priority goes to the host directive exportAs*/
    exportAs: "boldDir,fooDir",
    hostDirectives: [MouseenterDirective]
})
export class ResolvedComponent implements OnInit {

    title = "source-title"

    constructor() {
    }

    ngOnInit(): void {
    }

}

@Component({
    standalone: true,
    selector: 'unresolved',
    template: "",
    styleUrls: ['./test.component.css'],
    hostDirectives: [MouseenterDirective, Foo]
})
export class UnResolvedComponent implements OnInit {


    constructor() {
    }

    ngOnInit(): void {
    }

}

@Component({
    standalone: true,
    selector: 'app-test',
    templateUrl: './hostDirectives.component.html',
    styleUrls: ['./test.component.css'],
    imports: [
        ResolvedComponent,
        UnResolvedComponent,
        BoldDirective,
        UnderlineDirective
    ]
})
export class TestComponent implements OnInit {


    constructor() {
    }

    ngOnInit(): void {
    }

    testDirective(a: string) {

    }
}
