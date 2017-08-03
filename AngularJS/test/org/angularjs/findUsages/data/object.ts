import {Component, HostListener} from '@angular/core';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    @HostListener("click")
    then() {
        console.log("then");
    }
}

new AppComponent()
