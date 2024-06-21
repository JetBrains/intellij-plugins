// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import {Component} from '@angular/core';

@Component({
    selector: 'home',
    templateUrl: './unused.html'
})
export class Home {
    publicUsedField: string;
    public readonly myObservable$: string; //WEB-34352
    testFn = testFn; //WEB-33907
    <warning descr="Unused field publicUnusedField">publicUnusedField</warning>: string;

    private <warning descr="Unused field privateUnusedField"><weak_warning descr="TS6133: 'privateUnusedField' is declared but its value is never read.">privateUnusedField</weak_warning></warning>: string;
    private privateUsedField: string;

    constructor(
        public publicUsedConstructorField: string,
        public <warning descr="Unused field publicUnusedConstructorField">publicUnusedConstructorField</warning>: string,
        private privateUsedConstructorField: string,
        private <warning descr="Unused field privateUnusedConstructorField"><weak_warning descr="TS6138: Property 'privateUnusedConstructorField' is declared but its value is never read.">privateUnusedConstructorField</weak_warning></warning>: string,) {
    }

    public publicUsedMethod() {

    }

    public <warning descr="Unused method publicUnusedMethod">publicUnusedMethod</warning>() {

    }

    private privateUsedMethod() {

    }

    private <warning descr="Unused method privateUnusedMethod"><weak_warning descr="TS6133: 'privateUnusedMethod' is declared but its value is never read.">privateUnusedMethod</weak_warning></warning>() {

    }

}

export function testFn() {
}