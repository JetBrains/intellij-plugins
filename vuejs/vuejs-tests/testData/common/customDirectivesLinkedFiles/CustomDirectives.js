import importedDirective from './importedDirective'

let someOtherDirective = {
  render: function() {

  }
};
export default {
  name: "client-comp",
  directives: {
    localDirective: {
      // directive definition
      inserted: function (el) {
        el.focus()
      }
    },
    someOtherDirective,
    importedDirective
  }
}
