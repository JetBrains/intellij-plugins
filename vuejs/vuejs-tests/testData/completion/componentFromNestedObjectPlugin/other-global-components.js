import StandardLabel from "./StandardLabel.vue"

const OtherGlobalComponentsPlugin = {
  install(app) {
    app.component("MyLabelFromPlugin", StandardLabel)
  }
}

export default OtherGlobalComponentsPlugin
