import StandardLabel from "./StandardLabel.vue"

const OtherGlobalComponentsPlugin = {
  install(app) {
    app.component("MyLabel<caret>FromPlugin", StandardLabel)
  }
}

export default OtherGlobalComponentsPlugin
