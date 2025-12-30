import {Component, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

@Component({
             selector: 'app-root',
             templateUrl: './cdkNoDataRowNotImported.html',
             styleUrls: ['./app.component.css']
           })
export class AppComponent {
  title = 'angular14';
}

@NgModule({
            declarations: [
              AppComponent
            ],
            imports: [
              BrowserModule,
            ],
            providers: [],
            bootstrap: [AppComponent]
          })
export class AppModule {
}
