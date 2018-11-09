// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, View} from '@angular/core';

@Component({
    selector: 'home',
    templateUrl: './methodCanBeStatic.html'
})
export class Home {

    public usedMethod() {
        console.log("foobar")
    }

    public <warning descr="Method can be static">unusedMethod</warning>() {
        console.log("foobar")
    }

}