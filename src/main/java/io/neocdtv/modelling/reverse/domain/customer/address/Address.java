package io.neocdtv.modelling.reverse.domain.customer.address;

import io.neocdtv.modelling.reverse.domain.customer.Person;

/**
 * @author xix
 */
public class Address extends Object {
  private int street;
  private int city;
  private Person person;
  private AddressType addressType;

  public int getStreet() {
    return street;
  }

  public void setStreet(int street) {
    this.street = street;
  }

  public int getCity() {
    return city;
  }

  public void setCity(int city) {
    this.city = city;
  }

  public Person getPerson() {
    return person;
  }

  public void setPerson(Person person) {
    this.person = person;
  }

  public AddressType getAddressType() {
    return addressType;
  }

  public void setAddressType(AddressType addressType) {
    this.addressType = addressType;
  }
}
