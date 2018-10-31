package io.neocdtv.modelling.reverse.domain.offer;

/**
 * @author xix
 */
public class Contract {
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
