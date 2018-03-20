function customDecorator() {
    return function (ignored) {
    }
}

@customDecorator()
@Component({selector: 'foo-bar'})
export class FooBar<caret>Component {
}