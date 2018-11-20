package io.neocdtv.modelling.reverse.domain.customer;

import io.neocdtv.modelling.reverse.domain.customer.address.Address;
import io.neocdtv.modelling.reverse.domain.customer.organisation.OrgType;

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
