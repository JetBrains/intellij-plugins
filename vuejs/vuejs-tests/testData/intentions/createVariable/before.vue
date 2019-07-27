<!-- "Create field 'name2'" "true" -->
<script lang="ts">
    class MyClass {
        name: String;
        Foo() {
            this.name = this.na<caret>me2;
        }
    }
</script>
