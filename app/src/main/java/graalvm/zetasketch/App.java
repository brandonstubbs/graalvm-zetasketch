/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package graalvm.zetasketch;

import java.util.Arrays;

import com.google.zetasketch.HyperLogLogPlusPlus;


public class App {

    public static void main(String[] args) {
        var hll = new HyperLogLogPlusPlus.Builder()
                .normalPrecision(15)
                .sparsePrecision(20)
                .buildForStrings();

        hll.add("foo");
        hll.add("bar");
        hll.add("baz");

        System.out.println("RESULT1: " + hll.result());
        var data1 = hll.serializeToByteArray();
        System.out.println("DATA1: " + Arrays.toString(data1));

        var hll2 = HyperLogLogPlusPlus.forProto(data1);
        System.out.println("RESULT2: " + hll2.result());
        System.out.println("DATA2: " + Arrays.toString(hll2.serializeToByteArray()));

        System.out.println("Done");
    }
}
