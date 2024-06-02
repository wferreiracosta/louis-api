package br.com.wferreiracosta.louis.configs.properties;

public record SecretsManagerProperties(

        String username,
        String password,
        String engine,
        String host,
        Integer port,
        String dbInstanceIdentifier,
        String driverClassName,
        String database

) {
}
