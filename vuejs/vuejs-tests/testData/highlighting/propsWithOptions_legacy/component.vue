<template lang="pug">
  div

</template>
<script>
  @Component({
               props: {
                 reg_bool1: {
                   type: Boolean,
                   required: true
                 },
                 reg_bool2: [Boolean, String],
                 reg_bool3: {
                   type: [String, Boolean, Array],
                   required: true
                 },
                 reg_bool4: Boolean
               }
             })
  export default class {
    @Prop({type: Boolean, required: false}) dec_bool1
    @Prop({type: [Boolean, String], required: true}) dec_bool2
    @Prop(Boolean) dec_bool3
    @Prop([Boolean, String]) dec_bool4

    @Prop(String) dec_string1
    @Prop({type: String, required: true}) dec_string2

    @PropSync("synced_string1", {type: String, required: true}) syncedProp_string1
    @PropSync("synced_bool1", Boolean) syncedProp_bool1

    @Model('event_string1', {type: [String, Array], required: true}) model_string1
  }
</script>
