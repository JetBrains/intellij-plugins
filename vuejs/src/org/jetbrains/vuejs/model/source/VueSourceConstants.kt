// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

const val VUE_NAMESPACE: String = "Vue"

const val EXTEND_FUN: String = "extend"
const val USE_FUN: String = "use"
const val COMPONENT_FUN: String = "component"
const val MIXIN_FUN: String = "mixin"
const val DIRECTIVE_FUN: String = "directive"
const val FILTER_FUN: String = "filter"
const val DEFINE_COMPONENT_FUN: String = "defineComponent"
const val DEFINE_NUXT_COMPONENT_FUN: String = "defineNuxtComponent" // so far, the IDE treats it just as an alias for defineComponent
const val DEFINE_PROPS_FUN: String = "defineProps"
const val DEFINE_EMITS_FUN: String = "defineEmits"
const val DEFINE_SLOTS_FUN: String = "defineSlots"
const val DEFINE_EXPOSE_FUN: String = "defineExpose"
const val DEFINE_MODEL_FUN: String = "defineModel"
const val DEFINE_OPTIONS_FUN: String = "defineOptions"
const val WITH_DEFAULTS_FUN: String = "withDefaults"
const val CREATE_APP_FUN: String = "createApp"
const val MOUNT_FUN: String = "mount"
const val PROVIDE_FUN: String = "provide"
const val INJECT_FUN: String = "inject"

const val MIXINS_PROP: String = "mixins"
const val EXTENDS_PROP: String = "extends"
const val DIRECTIVES_PROP: String = "directives"
const val COMPONENTS_PROP: String = "components"
const val FILTERS_PROP: String = "filters"
const val SETUP_METHOD: String = "setup"
const val NAME_PROP: String = "name"
const val TEMPLATE_PROP: String = "template"
const val METHODS_PROP: String = "methods"
const val EMITS_PROP: String = "emits"
const val SLOTS_PROP: String = "slots"
const val INJECT_PROP: String = "inject"
const val INJECT_FROM: String = "from"
const val PROVIDE_PROP: String = "provide"
const val COMPUTED_PROP: String = "computed"
const val WATCH_PROP: String = "watch"
const val DATA_PROP: String = "data"
const val MODEL_PROP: String = "model"
const val MODEL_PROP_PROP: String = "prop"
const val MODEL_EVENT_PROP: String = "event"
const val MODEL_LOCAL_PROP: String = "local"
const val MODEL_VALUE_PROP: String = "modelValue"
const val DELIMITERS_PROP: String = "delimiters"
const val PROPS_PROP: String = "props"
const val PROPS_TYPE_PROP: String = "type"
const val PROPS_REQUIRED_PROP: String = "required"
const val PROPS_DEFAULT_PROP: String = "default"
const val EL_PROP: String = "el"

const val INSTANCE_PROPS_PROP: String = "\$props"
const val INSTANCE_REFS_PROP: String = "\$refs"
const val INSTANCE_SLOTS_PROP: String = "\$slots"
const val INSTANCE_EMIT_METHOD: String = "\$emit"
const val INSTANCE_DATA_PROP: String = "\$data"
const val INSTANCE_OPTIONS_PROP: String = "\$options"

const val TYPENAME_PROP_TYPE: String = "PropType"
const val TYPENAME_PROP_OPTIONS: String = "PropOptions"
const val TYPENAME_PROP: String = "Prop"

val PROPS_CONTAINER_TYPES: Set<String> = setOf(
  TYPENAME_PROP_TYPE,
  TYPENAME_PROP_OPTIONS,
  TYPENAME_PROP,
)
