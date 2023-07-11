import {Component, Directive, OnInit, ElementRef, EventEmitter, HostListener, Output, Input} from '@angular/core';

// @ts-ignore
@Directive({
  selector: '[appBold]',
  standalone: true
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

  constructor(private hostElement: ElementRef<HTMLElement>) {}

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
    hostDirectives: [{
        directive: BoldDirective,
        outputs: ['hover']
    }, {
        directive: UnderlineDirective,
        inputs: ['color: underlineColor']
    }]
})
export class MouseenterDirective {
    constructor() {
    }
}

@Component({
    standalone: true,
    selector: 'resolved',
    template: "",
    styleUrls: ['./test.component.css'],
    hostDirectives: [MouseenterDirective]
})
export class ResolvedComponent implements OnInit {


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
        UnResolvedComponent
    ]
})
export class TestComponent implements OnInit {


    constructor() {
    }

    ngOnInit(): void {
    }

}
