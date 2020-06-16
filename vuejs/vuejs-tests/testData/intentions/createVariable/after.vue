<!-- "Create field 'name2'" "true" -->
<script lang="ts">
    class MyClass {
        name: String;
      private name2: String;
        Foo() {
            this.name = this.name2;
        }
    }
</script>
