package io.neocdtv.modelling.reverse.domain.offer;

import io.neocdtv.modelling.reverse.domain.customer.ICustomer;
import io.neocdtv.modelling.reverse.domain.vehicle.Car;

import java.util.Date;
import java.util.Set;

/**
 * @author xix
 */
public class Offer {
  private Date date;
  private Set<ICustomer> keepers;
  private ICustomer owner;
  private Car car;
  private boolean aBoolean;
  private int anInt;
  private Set<Price> prices;
  private ICustomer contactPerson;
  private ICustomer conditionProvider;
  private Payment payment;
}
