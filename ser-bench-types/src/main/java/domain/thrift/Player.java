/**
 * Autogenerated by Thrift Compiler (0.9.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package domain.thrift;


import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;

public enum Player implements org.apache.thrift.TEnum {
  JAVA(0),
  FLASH(1);

  private final int value;

  private Player(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static Player findByValue(int value) { 
    switch (value) {
      case 0:
        return JAVA;
      case 1:
        return FLASH;
      default:
        return null;
    }
  }
}