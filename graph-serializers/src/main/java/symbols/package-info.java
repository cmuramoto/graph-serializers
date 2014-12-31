/**
 * The sub packages of this package define the necessary symbols for code generation 
 * and are meant to help in organizing references to type names and descriptors.
 * 
 * These packages MUST NOT define anything other interfaces containing constant Strings and Numbers. Strings must really be 
 * constant, that is, one must not call String.intern() or other method that may require runtime resolution, e.g. 
 * String desc = MethodType.methodType(Object.class,String.class,int.class).toMethodDescriptorString(), 
 * is NOT how one should define a method descriptor. Instead, the value should be either hardcoded or concatenated from 
 * other constants, like String desc="("+_String.desc+"I)"+_Object.desc.
 * 
 * 
 * By following this convention, every constant referenced from elsewhere will be inlined by the compiler, which means 
 * that the types defined in this package won't ever be referenced, at runtime, so we can keep the code organized with
 * zero overhead.
 */
/**
 * @author cmuramoto
 *
 */
package symbols;