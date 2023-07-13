// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {AfterViewInit, ElementRef, OnDestroy} from '@angular/core';
import {DomHandler} from '../dom/domhandler';

export declare class Button implements AfterViewInit, OnDestroy {
    el: ElementRef;
    domHandler: DomHandler;
    iconPos: string;
    cornerStyleClass: string;
    _label: string;
    _icon: string;
    initialized: boolean;
    constructor(el: ElementRef, domHandler: DomHandler);
    ngAfterViewInit(): void;
    getStyleClass(): string;
    label: string;
    icon: string;
    ngOnDestroy(): void;
}
export declare class ButtonModule {
}
