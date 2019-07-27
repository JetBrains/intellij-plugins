// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, NgModule, Type} from "@angular/core";


@Component({
})
export class MyComp2 {

}

class Foo {

}

function createComponent(): Type<MyComp2> {
    return null;
}

const LIST1 = [
    {
        ngModule: true
    }
]

const LIST3 = [
    Foo,
    MyComp2
]
const LIST2 = [
    MyComp2
]

@Component({
    entryComponents: [
        MyComp1,
        <error descr="Class MyDir1 is not an Angular component">MyDir1</error>,
        <error descr="Class Foo is not an Angular component">Foo</error>,
        LIST1,
        <error descr="Class Foo is not an Angular component">LIST3</error>,
        LIST2,
        [
            MyComp1,
            createComponent()
        ]
    ],
    bootstrap: [
        MyDir1
    ]
})
export class MyComp1 {

}

@NgModule({
    entryComponents: [
        MyComp1,
        <error descr="Class MyDir1 is not an Angular component">MyDir1</error>,
            <error descr="Class Foo is not an Angular component">Foo</error>,
        LIST1,
        <error descr="Class Foo is not an Angular component">LIST3</error>,
        LIST2,
        {
            ngModule: true
        },
    ],
    bootstrap: [
        <error descr="Class MyDir1 is not an Angular component">MyDir1</error>,
        MyComp1,
        <error descr="Expression does not resolve to an array of class types or a class type">LIST1</error>,
        LIST2,
        [
            MyComp1,
            createComponent()
        ]
    ]
})
export class MyComp2 {

}

@Directive({
    entryComponents: [
        LIST1
    ],
    bootstrap: [
        LIST1
    ]
})
export class MyDir1 {

}