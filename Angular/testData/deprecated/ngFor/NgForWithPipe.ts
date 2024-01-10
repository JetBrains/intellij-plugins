import {Component} from "@angular/core";

@Component({
  selector: 'app-root',
  templateUrl: './NgForWithPipeHTML.html',
})
export class AppComponent {
  record: Record<string, any> = {"person": {"name": "John", "age": "20"}};
}

