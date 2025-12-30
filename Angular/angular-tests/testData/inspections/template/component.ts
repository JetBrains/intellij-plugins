// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, ElementRef, Input, Pipe, PipeTransform} from '@angular/core';
import {CommonModule} from "@angular/common";

@Component({
    selector: 'my-' + 'comp',
    template: `<div></div>`,
    exportAs: 'foo,bar'
})
export class MyComponent {
    @Input()
    onFoo: string;
}

@Component({
    selector: '[foo-comp]',
    template: `<div></div>`,
    exportAs: 'foo,bar'
})
export class MyComponent2 {
}

@Component({
    selector: '[fooComp2]',
    template: `<div></div>`,
})
export class MyComponent3 {
}

@Component({
    selector: 'my-comp2',
    template: `<div></div>`,
})
export class MyComponent4 {
}

@Directive({
    selector: '[foo-dir]',
    exportAs: 'foo'
})
export class MyDirective {

}

@Directive({
    selector: '[myTemplate]',
    inputs: [
        'myTemplate',
        'myTemplateFor',
        'myTemplateBar'
    ],
    outputs: [
        'templateEvent'
    ]
})
export class MyTemplate {

}

@Directive({
    selector: '[myTemplateFor]',
    outputs: [
        'otherEvent'
    ]
})
export class MyTemplateEvent {

}

@Component({
  selector: 'app-standalone',
  standalone: true,
  imports: [CommonModule],
  template: `<p>standalone works!</p>`,
})
export class StandaloneComponent {
}

@Directive({
  selector: '[appStandaloneDirective]',
  standalone: true
})
export class StandaloneDirective {
  constructor(private el: ElementRef) {
    this.el.nativeElement.style.backgroundColor = 'yellow';
  }
}

@Pipe({
  name: 'standalonePipe',
  standalone: true
})
export class StandalonePipePipe implements PipeTransform {
  transform(value: number, exponent = 1): number {
    return Math.pow(value, exponent);
  }
}

@Component({
  selector: 'app-standalone-not-imported',
  standalone: true,
  imports: [CommonModule],
  template: `<p>shouldn't work!</p>`,
})
export class StandaloneNotImportedComponent {
}

@Directive({
  selector: '[appStandaloneNotImportedDirective]',
  standalone: true,
})
export class StandaloneNotImportedDirective {
  constructor(private el: ElementRef) {
    this.el.nativeElement.style.backgroundColor = 'yellow';
  }
}

@Pipe({
  name: 'standaloneNotImportedPipe',
  standalone: true,
})
export class StandaloneNotImportedPipe implements PipeTransform {
  transform(value: number, exponent = 1): number {
    return Math.pow(value, exponent);
  }
}
