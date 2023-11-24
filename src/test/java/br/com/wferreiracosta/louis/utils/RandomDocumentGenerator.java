package br.com.wferreiracosta.louis.utils;

public class RandomDocumentGenerator {

    public static String generateRandomCPF() {
        long min = 10000000000L;
        long max = 99999999999L;
        long randomLong = min + (long) (Math.random() * (max - min + 1));
        return String.valueOf(randomLong);
    }

    public static String generateRandomCNPJ() {
        long min = 10000000000000L;
        long max = 99999999999999L;
        long randomLong = min + (long) (Math.random() * (max - min + 1));
        return String.valueOf(randomLong);
    }

}
