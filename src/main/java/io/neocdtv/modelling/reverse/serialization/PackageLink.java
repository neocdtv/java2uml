package io.neocdtv.modelling.reverse.serialization;

/**
 * @author xix
 * @since 20.11.18
 */
public class PackageLink {
  private final String marker = "PACKAGE_LINK";
  private String name;
  private String type;
  private String packageName;
  private String upperBound;

  public String getMarker() {
    return marker;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getUpperBound() {
    return upperBound;
  }

  public void setUpperBound(String upperBound) {
    this.upperBound = upperBound;
  }
}
