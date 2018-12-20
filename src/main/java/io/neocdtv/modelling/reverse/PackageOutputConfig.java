package io.neocdtv.modelling.reverse;

/**
 * @author xix
 * @since 19.12.18
 */
public class PackageOutputConfig {
  private String packageName;

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getRelativePath() {
    return relativePath;
  }

  public void setRelativePath(String relativePath) {
    this.relativePath = relativePath;
  }

  private String relativePath;


}
