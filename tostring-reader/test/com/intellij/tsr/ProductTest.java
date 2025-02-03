package com.intellij.tsr;

import org.junit.Test;

public class ProductTest {
  @Test
  public void run() {
    Product p1 = new Product(ProductType.REFURBISHED, "Some", 10.0, 100);
    Product p2 = new Product(ProductType.USED, "Some", 20.0, 100);

    // assertEquals(p1, p2);
  }
}
