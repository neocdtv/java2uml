package io.neocdtv.modelling.reverse;

/**
 * @author xix
 */
public class CliUtil {

  static String findCommandArgumentByName(final String argToNameForFind, final String[] args) {
    String argValue = null;
    for (String argToCheck : args) {
      final String[] split = argToCheck.split("=");
      if (split[0].equals(argToNameForFind)) {
        argValue = split[1];
      }
    }
    return argValue;
  }

  static boolean isCommandArgumentPresent(final String argToNameForFind, final String[] args) {
    for (String argToCheck : args) {
      if (argToCheck.equals(argToNameForFind)) {
        return true;
      }
    }
    return false;
  }
}
