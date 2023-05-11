<script lang="ts">
import {Emit, Component, PropSync, Vue} from 'vue-property-decorator';

@Component()
export default class HelloWorld extends <error descr="No base constructor has the specified number of type arguments">Vue</error> {

  @PropSync('name', {type: String, required: true})
  syncedName

  @PropSync('name2', {type: String, required: true})
  syncedNameTypeDecl: 'a' | 'b'

  @PropSync('name3')
  syncedNameAny

  @Emit()
  private click(param1: string, param2: boolean) {
    console.log(param1, param2);
    return 'foo';
  }

  @Emit()
  private event() {
    return 123;
  }

  @Emit()
  private eventNoArgs() {
  }

  @Emit()
  private promise(): <error descr="Return statement is required for non-void return type">Promise<number></error> {
  }
}
</script>