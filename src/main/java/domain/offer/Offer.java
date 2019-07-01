package domain.offer;

import domain.SalesStep;
import domain.customer.ICustomer;
import domain.vehicle.Car;

import java.util.Date;
import java.util.Set;

/**
 * @author xix
 */
public class Offer extends SalesStep{
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
  // TODO: private int[] externalOfferIds;
  // causes nullpointer on Java2EclipseUml2.isPrimitiveType
}
