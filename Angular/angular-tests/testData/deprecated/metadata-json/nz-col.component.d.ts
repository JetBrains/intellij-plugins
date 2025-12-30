// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {ElementRef, OnChanges, OnInit, Renderer2, SimpleChange} from '@angular/core';
import {NzUpdateHostClassService} from '../core/services/update-host-class.service';
import {NzRowComponent} from './nz-row.component';
import {NzRowDirective} from './nz-row.directive';

export interface EmbeddedProperty {
    span: number;
    pull: number;
    push: number;
    offset: number;
    order: number;
}
export declare class NzColComponent implements OnInit, OnChanges {
    private nzUpdateHostClassService;
    private elementRef;
    nzRowComponent: NzRowComponent;
    nzRowDirective: NzRowDirective;
    private renderer;
    private el;
    private prefixCls;
    readonly paddingLeft: number;
    readonly paddingRight: number;
    nzSpan: number;
    nzOrder: number;
    nzOffset: number;
    nzPush: number;
    nzPull: number;
    nzXs: number | EmbeddedProperty;
    nzSm: number | EmbeddedProperty;
    nzMd: number | EmbeddedProperty;
    nzLg: number | EmbeddedProperty;
    nzXl: number | EmbeddedProperty;
    nzXXl: number | EmbeddedProperty;
    /** temp solution since no method add classMap to host https://github.com/angular/angular/issues/7289*/
    setClassMap(): void;
    generateClass(): object;
    readonly nzRow: NzRowComponent;
    ngOnChanges(changes: {
        [propertyName: string]: SimpleChange;
    }): void;
    constructor(nzUpdateHostClassService: NzUpdateHostClassService, elementRef: ElementRef, nzRowComponent: NzRowComponent, nzRowDirective: NzRowDirective, renderer: Renderer2);
    ngOnInit(): void;
}
