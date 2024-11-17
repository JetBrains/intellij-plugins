package com.intellij.tsr;

import java.util.List;
import java.util.Map;

public final class Product {
  private final ProductType productType;
  private final String name;
  private final Double price;
  private final int count;

  public Product(ProductType productType, String name, Double price, int count) {
    this.productType = productType;
    this.name = name;
    this.price = price;
    this.count = count;
  }

  public String getName() {
    return name;
  }

  public Double getPrice() {
    return price;
  }

  public int getCount() {
    return count;
  }

  public ProductType getProductType() {
    return productType;
  }

  @Override
  public String toString() {
    return "Product{" +
        "productType=" + productType +
        ", name='" + name + '\'' +
        ", price=" + price +
        ", count=" + count +
        '}';
  }

  public static void main(String[] args) {
    Product p = new Product(ProductType.USED, "Some", 10.0, 100);
    System.out.println(List.of(p));
    System.out.println(Map.of("product", p));
    System.out.println(Map.of(new UserRef(), p));
  }
}

class UserRef {

}