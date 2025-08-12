import StandardLabel from "./StandardLabel.vue"

const OtherGlobalComponentsPlugin = {
  install(app) {
    app.component("OtherLabelFromPlugin", StandardLabel)
  }
}

export default OtherGlobalComponentsPlugin
