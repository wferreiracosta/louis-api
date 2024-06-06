package br.com.wferreiracosta.louis.services.impl;

import br.com.wferreiracosta.louis.clients.AuthorizeClient;
import br.com.wferreiracosta.louis.services.AuthorizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizeServiceImpl implements AuthorizeService {

    private final AuthorizeClient client;

    @Override
    public boolean authorize() {
        final var response = client.authorize();
        return response.data().authorization();
    }

}
