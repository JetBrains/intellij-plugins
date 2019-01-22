// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Directive, NgModule} from '@angular/core';
import {RouterModule, RouterOutlet} from "./router";

@Directive({
    selector: "[attr]"
})
export class MyDirective {

}

class Test {

    static getThis(): Test {
        return new Test();
    };

}

@NgModule({
    imports: [
        RouterModule.forRoot({path: 'heroes', component: undefined}),
    ],
    declarations: [
        MyDirective
    ]
})
export class AppRoutingModule {
}

@NgModule({
    imports: [
        RouterModule.forRoot({path: 'heroes', component: undefined}),
        Test.getThis()
    ],
    declarations: [
        MyDirective
    ],
    exports: [
        RouterOutlet
    ]
})
export class AppRoutingModuleNotFullyResolved {
}