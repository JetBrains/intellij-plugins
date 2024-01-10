import {Directive, ElementRef, HostListener, Input} from '@angular/core';

@Directive({
  selector: '[appUnderline]'
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

