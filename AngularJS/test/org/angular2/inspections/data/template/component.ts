// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, Input, Output} from '@angular/core';
import {NgModel} from './forms';


@Component({
    selector: 'my-comp',
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