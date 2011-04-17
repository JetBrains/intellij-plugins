package org.flyti.swf.emit;

public class PackageBuilder {
  private final String packageName;

  public PackageBuilder(String packageName) {
    this.packageName = packageName;
  }

  public ClassBuilder defineClass(String name, String superClassName) {
			//var fullName:String = packageName + MultinameUtil.PERIOD + name;
	ClassBuilder classBuilder = new ClassBuilder();
				//cb.name = name;
				//cb.packageName = packageName;
				//cb.superClassName = superClassName;
        //
        //cb.constantPool = _constantPool;
        //_classBuilders[_classBuilders.length] = cb;

			return classBuilder;
		}
}
