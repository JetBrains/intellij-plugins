// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

@Component({
    selector: '[myHoverList]',
    host: {
        '(mouseenter)': 'hello()'
    },
    template: `
        {{ <caret> }}
    `
})
export class HoverListDirective {
    @Output() testing: EventEmitter = new EventEmitter();
    @Input() testOne: string = "HELLO";
    @Input() testTwo: string;

    constructor(el: ElementRef, renderer: Renderer) {
    }

    public ngOnChanges() {
    }

    public ngOnInit() {
    }

    public ngDoCheck() {
    }

    public ngOnDestroy() {
    }

    public ngAfterContentInit() {
    }

    public ngAfterContentChecked() {
    }

    public ngAfterViewInit() {
    }

    public ngAfterViewChecked() {
    }
}
