// The flat-config equivalent of an eslintrc that `extends` a sibling file: import the local base
// config and spread it in.
import base from "./eslint.config.base.mjs";

export default [base];
