package br.com.wferreiracosta.louis.utils;

import java.util.Random;

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

    public static String email() {
        final var domains = new String[]{"@mail.com"};
        final var characters = new String[]{"abcdefghijklmnopqrstuvwxyz1234567890"};
        final var email = new StringBuilder();
        final var random = new Random();

        for (int i = 0; i < 5; i++) {
            email.append(characters[0].charAt(random.nextInt(characters[0].length())));
        }

        email.append(domains[random.nextInt(domains.length)]);

        return email.toString();
    }

}
