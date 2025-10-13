import {<symbolName descr="identifiers//exported function">Component</symbolName>, <symbolName descr="identifiers//exported function">signal</symbolName>} <info descr="null">from</info> '@angular/core';

<symbolName descr="decorator">@</symbolName><symbolName descr="decorator">Component</symbolName>({
  <symbolName descr="instance field">selector</symbolName>: '<symbolName descr="HTML_TAG_NAME">app-enter</symbolName>',
  <symbolName descr="instance field">template</symbolName>: `<inject descr="null">
        <h2><code>animate.enter</code> Example</h2>
        <button type="button" (click)="<symbolName descr="instance method">toggle</symbolName>()">Toggle Element</button>
        @if (<symbolName descr="ng-signal">isShown</symbolName>()) {
            <div class="enter-container" animate.enter="<symbolName descr="CSS.CLASS_NAME">enter-animation</symbolName> <symbolName descr="CSS.CLASS_NAME">foo-bar</symbolName>">
                <p>The box is entering.</p>
            </div>
            <div class="enter-container" animate.leave="<symbolName descr="CSS.CLASS_NAME">leave-animation</symbolName>">
                <p>The box is entering.</p>
            </div>
        }
    </inject>`,
  <symbolName descr="instance field">styles</symbolName>: [
    `<inject descr="null">
      .<info descr="null">enter-animation</info> {

      }
    </inject>`
  ]
})
export class <symbolName descr="classes//exported class">Enter</symbolName> {
  <symbolName descr="ng-signal">isShown</symbolName> = <symbolName descr="identifiers//exported function">signal</symbolName>(false);

  <symbolName descr="instance method">toggle</symbolName>() {
    this.<symbolName descr="ng-signal">isShown</symbolName>.<symbolName descr="instance method">update</symbolName>((<symbolName descr="identifiers//parameter">isShown</symbolName>) => !<symbolName descr="identifiers//parameter">isShown</symbolName>);
  }
}