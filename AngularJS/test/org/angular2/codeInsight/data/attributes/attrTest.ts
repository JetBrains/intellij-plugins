// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, Input, Output, View} from 'angular2/angular2';
import {EventEmitter} from "events";

@Component({
    selector: 'my-comp',
    template: `<div <caret>`,
})
export class HeroAsyncMessageComponent<T> {

}
