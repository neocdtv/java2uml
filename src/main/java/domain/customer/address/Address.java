package domain.customer.address;

/**
 * @author xix
 */
public class Address extends Object {
  private int street;
  private int city;
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

  public AddressType getAddressType() {
    return addressType;
  }

  public void setAddressType(AddressType addressType) {
    this.addressType = addressType;
  }
}
