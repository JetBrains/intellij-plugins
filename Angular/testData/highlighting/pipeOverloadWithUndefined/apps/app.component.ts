import {Component, OnInit} from '@angular/core';
import {Observable, of} from "rxjs";
import {AsyncPipe, NgIf} from "@angular/common";

@Component({
    standalone: true,
    imports: [NgIf, AsyncPipe],
    selector: 'mobi-root',
    templateUrl: './app.component.html',
})
export class AppComponent implements OnInit {
    public test$?: Observable<string>;

    ngOnInit() {
        this.test$ = of('test');
    }
}
