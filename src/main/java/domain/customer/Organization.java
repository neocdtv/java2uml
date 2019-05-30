package domain.customer;

import domain.customer.address.Address;
import domain.customer.organisation.OrgType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xix
 */
public class Organization extends Customer {
  private Set<Address> aSetAddresses;
  private List<Address> aListAddresses;
  private Address[] arrayAddresses;
  private Map<String, Address> mapAddresses;
  private OrgType orgType;
}
