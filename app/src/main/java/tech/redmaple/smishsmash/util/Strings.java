package tech.redmaple.smishsmash.util;

public class Strings {
    public static String stringArrayToCSVString(String[] input) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < input.length; i++) {

            if (i != 0) {
                sb.append(", ");
            }

            sb.append(input[i]);
        }

        return sb.toString();
    }
}
