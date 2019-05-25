package io.neocdtv.modelling.reverse.domain.customer;

/**
 * @author xix
 */
public class Customer implements ICustomer {
  protected int age;

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  @Override
  public int compareTo(Object o) {
    return 0;
  }
}
