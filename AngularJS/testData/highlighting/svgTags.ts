// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgForOf } from '@angular/common';

@Component({
    selector: 'g[some-attr]',
    template: ``
})
export class GComponent implements OnInit, OnDestroy {
}

@Component({
    selector: 'f[some-attr]',
    template: ``
})
export class FComponent implements OnInit, OnDestroy {
}

@Component({
 selector: 'ngx-room-selector',
 imports: [NgForOf],
 standalone: true,
 template: `
    <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
         [attr.viewBox]="viewBox" preserveAspectRatio="xMidYMid">
      <g>
        <path class="room-border" [attr.d]="border.d" *ngFor="let border of roomSvg.borders" />
      </g>
      <<error descr="Component or directive matching f element is out of scope of the current template">f</error>></f>
    </svg>`,
})
export class RoomSelectorComponent implements OnInit, OnDestroy {
    viewBox = '-20 -20 618.88 407.99';
    roomSvg = {
        borders: [{
            d: 'M186.21,130.05H216.37V160H186.21Z',
        }],
    }
}