import {Component, Directive, EventEmitter, Input, Output} from "@angular/core";

@Directive({
             standalone: true,
             selector: "[my-input]"
           })
export class MyDirective {
  /**
   * the input
   **/
  @Input("my-input")
  myInput: String

  /**
   * the output
   * @foo bar
   **/
  @Output("my-inputChange")
  myInputChange: EventEmitter<String>
}

@Component({
             standalone: true,
             template: "<div [(my<caret>-input)]=''></div>",
             imports: [
               MyDirective
             ]
           })
class MyComponent {
}
