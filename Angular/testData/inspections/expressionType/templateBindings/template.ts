// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, Input, TemplateRef} from '@angular/core';

@Component({
    selector: 'expr',
    templateUrl: './template.html'
})
export class ExpressionsComponent {

    myNumber: number;

    myArray: string[];

    myList: Array<string>;

    myThen: string;

    myElse: TemplateRef<any>;

    myBadFunction(items: string): string {
        return null;
    }

    myOkFunction(index: number, item: string): void {

    }

    myAnotherFunction(index: number, item: number): void {

    }
}

interface MyContext<T> {
    field: T
}

@Directive({
    selector: "[ngIf]"
})
export class MyCustomTemplate<T> {


    constructor(templateRef: TemplateRef<MyContext<T>>) {
    }

    @Input
    ngIfExtra: T;


    @Input
    ngIfSuper: T;

    @Input
    ngIfNumber: number;

}

@Component({
    selector: "[comp]"
})
export class MyCustomComponent {

}

@Directive({
    selector: "[appDir]",
    exportAs: "dir"
})
export class MyDirective {

}