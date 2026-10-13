import { Component } from '@angular/core';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [DatePipe],
  templateUrl: './app.html',
})
export class App {
  protected readonly today = new Date();
}
