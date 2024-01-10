import {Component, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {CdkTableModule} from '@angular/cdk/table';

@Component({
             selector: 'app-root',
             templateUrl: './cdkDirectives.html',
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
              CdkTableModule,
            ],
            providers: [],
            bootstrap: [AppComponent]
          })
export class AppModule {
}
