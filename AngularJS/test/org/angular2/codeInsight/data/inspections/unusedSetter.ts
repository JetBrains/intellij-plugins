// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, View} from '@angular/core';

@Component({
    selector: 'home',
    templateUrl: './unusedSetter.html'
})
export class Home {

    private title: string;

    set getSetUsedProperty(value: string) {
        this.title = value;
    }

    get getSetUsedProperty(): string {
        return this.title;
    }

    set onlySetUsedProperty(value: any) {
        this.title = value;
    }

    get <warning descr="Unused property onlySetUsedProperty">onlySetUsedProperty</warning>(): any {
        return this.title;
    }

    set <warning descr="Unused property onlyGetUsedProperty">onlyGetUsedProperty</warning>(value: any) {
        this.title = value;
    }

    get onlyGetUsedProperty(): any {
        return this.title;
    }
}