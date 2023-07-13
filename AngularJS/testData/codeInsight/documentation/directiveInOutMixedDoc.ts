import {Component, Directive, EventEmitter, Input, Output} from "@angular/core";

/**
 * the directive
 * @foo bar
 **/
@Directive({
             standalone: true,
             selector: "[my-input]"
           })
export class MyDirective {
  /**
   * My input doc
   */
  @Input("my-input")
  myInput: String

  @Output("my-inputChange")
  myInputChange: EventEmitter<String>
}

@Component({
             standalone: true,
             template: "<div [(my-<caret>input)]=''></div>",
             imports: [
               MyDirective
             ]
           })
class MyComponent {
}
