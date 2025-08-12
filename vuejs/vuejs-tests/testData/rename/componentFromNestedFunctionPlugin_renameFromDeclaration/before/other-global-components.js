import StandardLabel from "./StandardLabel.vue"

export default function (app) {
  app.component("MyLabel<caret>FromPlugin", StandardLabel)
}
