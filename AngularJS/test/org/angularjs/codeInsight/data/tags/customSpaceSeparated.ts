import {Component} from '@angular/core';

@Component({
    selector: 'dummy-list, dummy-nav-list',
    template: '<ng-content>Dummy</ng-content>',
    styleUrls: ['app/components/dummy/dummy.css'],
    providers: [],
    directives: [],
    pipes: []
})
export class DummyList {
    constructor() {}
    ngOnInit() {
    }
}