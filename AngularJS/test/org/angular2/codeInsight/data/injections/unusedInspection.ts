// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

/// <reference path="../../typings/tsd.d.ts" />

import {Component, View} from '@angular/core';

@Component({
    selector: 'home',
    templateUrl: './unusedInspection.html'
})
export class Home {
    publicUsedField: string;
    public readonly myObservable$: string; //WEB-34352
    testFn = testFn; //WEB-33907
    <warning descr="Unused field publicUnusedField">publicUnusedField</warning>: string;

    private <warning descr="Unused field privateUnusedField">privateUnusedField</warning>: string;
    private <warning descr="Unused field privateUsedField">privateUsedField</warning>: string;

    constructor(
        public publicUsedConstructorField: string,
        public <warning descr="Unused field publicUnusedConstructorField">publicUnusedConstructorField</warning>: string,
        private <warning descr="Unused field privateUnusedConstructorField">privateUnusedConstructorField</warning>: string,
        private <warning descr="Unused field privateUsedConstructorField">privateUsedConstructorField</warning>: string) {
    }

    public publicUsedMethod() {

    }

    public <warning descr="Unused method publicUnusedMethod">publicUnusedMethod</warning>() {

    }

    private <warning descr="Unused method privateUsedMethod">privateUsedMethod</warning>() {

    }

    private <warning descr="Unused method privateUnusedMethod">privateUnusedMethod</warning>() {

    }

}

export function testFn() {
}