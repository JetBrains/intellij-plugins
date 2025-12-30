// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Directive, NgModule} from '@angular/core';
import {RouterModule} from "./router/router_module";
import {RouterOutlet} from "./router/directives/router_outlet";

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

const SPREAD_DECLARATIONS = [
    MyDirective
]

@NgModule({
    imports: [
        RouterModule.forRoot({path: 'heroes', component: undefined}),
    ],
    declarations: [
        ...SPREAD_DECLARATIONS
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
        ...SPREAD_DECLARATIONS
    ],
    exports: [
        RouterOutlet
    ]
})
export class AppRoutingModuleNotFullyResolved {
}