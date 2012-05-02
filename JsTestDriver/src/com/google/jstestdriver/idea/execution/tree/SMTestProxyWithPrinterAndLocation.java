package com.google.jstestdriver.idea.execution.tree;

import com.intellij.execution.Location;
import com.intellij.execution.testframework.Printer;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
class SMTestProxyWithPrinterAndLocation extends SMTestProxy {

  private final LocationProvider myLocationProvider;
  private Printer myWiredPrinter;
  @Nullable
  private String myFixedName;

  public SMTestProxyWithPrinterAndLocation(String testName, boolean isSuite, @NotNull LocationProvider locationProvider) {
    super(testName, isSuite, null);
    myLocationProvider = locationProvider;
  }

  @Override
  public Location getLocation(Project project) {
    return myLocationProvider.provideLocation(project);
  }

  private Printer getPrinter(Printer printer) {
    return printer == null || myWiredPrinter == null ? printer : myWiredPrinter;
  }

  public void setPrinter(Printer printer) {
    super.setPrinter(getPrinter(printer));
  }

  public Printer getWiredPrinter() {
    return myWiredPrinter;
  }

  public void wirePrinter(Printer printer) {
    myWiredPrinter = printer;
    setPrinter(printer);
  }

  @Override
  public void printOn(Printer printer) {
    super.printOn(getPrinter(printer));
  }

  @Override
  public void addChild(SMTestProxy child) {
    super.addChild(child);
    if (child instanceof SMTestProxyWithPrinterAndLocation) {
      SMTestProxyWithPrinterAndLocation printerLocationChild = (SMTestProxyWithPrinterAndLocation) child;
      printerLocationChild.wirePrinter(myWiredPrinter);
    }
  }

  public void setFixedName(@NotNull String fixedName) {
    myFixedName = fixedName;
  }

  @Override
  public String getName() {
    if (myFixedName != null) {
      return myFixedName;
    }
    return super.getName();
  }

}
