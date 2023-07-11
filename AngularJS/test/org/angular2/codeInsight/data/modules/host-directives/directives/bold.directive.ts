import {Directive, ElementRef, EventEmitter, HostListener, Output} from '@angular/core';

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
