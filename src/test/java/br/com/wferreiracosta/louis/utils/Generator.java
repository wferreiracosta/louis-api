package br.com.wferreiracosta.louis.utils;

import static java.lang.String.valueOf;

public class Generator {

    public static String cpf() {
        final var min = 10000000000L;
        final var max = 99999999999L;
        return valueOf(min + (long) (Math.random() * (max - min + 1)));
    }

    public static String cnpj() {
        final var min = 10000000000000L;
        final var max = 99999999999999L;
        return valueOf(min + (long) (Math.random() * (max - min + 1)));
    }

}
