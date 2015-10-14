package {
public interface NamespaceInInterface {
  <error descr="Interface members cannot have namespace attributes">MyNs</error> function foo();
}
}