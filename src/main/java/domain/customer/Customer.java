package domain.customer;

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
  public String getFirstName() {
    return null;
  }
}
