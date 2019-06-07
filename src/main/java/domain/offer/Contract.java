package domain.offer;

import domain.SalesStep;

/**
 * @author xix
 */
public class Contract extends SalesStep{
  private Offer offer;
  private String status;

  public Offer getOffer() {
    return offer;
  }

  public void setOffer(Offer offer) {
    this.offer = offer;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }


}
