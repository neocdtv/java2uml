package io.neocdtv.modelling.reverse.domain.customer;

/**
 * @author xix
 */
public class Customer implements ICustomer {
  protected int attribute;

  public int getAttribute() {
    return attribute;
  }

  public void setAttribute(int attribute) {
    this.attribute = attribute;
  }

  @Override
  public int compareTo(Object o) {
    return 0;
  }
}
