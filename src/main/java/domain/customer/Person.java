package domain.customer;

import domain.customer.address.Address;

/**
 * @author xix
 */
public class Person extends Customer implements IPerson {
  private String firstName;
  public String lastName;
  private Salutation salutation;
  private Address firstAddress;
  private Address secondAddress;

  @Override
  public String getFirstName() {
    return firstName;
  }

}
