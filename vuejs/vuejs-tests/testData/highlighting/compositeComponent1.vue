<!-- item template -->
<template>
  <li>
    <div :class="{ bold: isFolder }" @click="toggle" @dblclick="changeType">
      {{ <weak_warning descr="Unresolved variable or type openmodel">openmodel</weak_warning>.name }}
      <span v-if="isFolder">[{{ open ? "-" : "+" }}]</span>
    </div>
    <ul v-if="isFolder" v-show="open">
      <tree-item class="item" v-for="model in model.children" :model="model" :foo="123"></tree-item>
      <<warning descr="Unknown html tag tree-items">tree-items</warning>>{{closed}}</<warning descr="Unknown html tag tree-items">tree-items</warning>>
      <li class="add" @click="addChild" @show="<weak_warning descr="Unresolved variable or type doShow">doShow</weak_warning>">+</li>
    </ul>
  </li>
</template>

<!-- item script -->
<script>
import {reactive, computed, toRefs, defineComponent} from "@vue/composition-api";

export default defineComponent(<weak_warning descr="Argument type {name: string, setup(any): {isFolder: UnwrapRef<{isFolder: ...<...>, open: boolean}>[\"isFolder\"] extends Ref<infer V> ? Ref<V> : Ref<UnwrapRef<{isFolder: ..., open: boolean}>[\"isFolder\"]>, changeType: function(): void, toggle: function(): void, addChild: function(): void, open: UnwrapRef<{isFolder: ...<...>, open: boolean}>[\"open\"] extends Ref<infer V> ? Ref<V> : Ref<UnwrapRef<{isFolder: ..., open: boolean}>[\"open\"]>}, props: {model: ObjectConstructor}} is not assignable to parameter type ComponentOptionsWithoutProps<unknown, unknown>">{</weak_warning>
  name: "TreeItem", // necessary for self-reference
  props: {
    model: Object
  },
  setup(props) {
    const state = reactive({
      open: false,
      isFolder: computed(() => {
        return props.model.children && props.model.children.length;
      })
    });

    function toggle() {
      state.open = !state.open;
    }

    function addChild() {
      props.model.children.push({ name: "new stuff" });
    }

    function changeType() {
      if (!state.isFolder) {
        props.model.children = [];
        addChild();
        state.open = true;
      }
    }

    return {
      ...toRefs(state),
      toggle,
      changeType,
      addChild
    };
  }
});
</script>
