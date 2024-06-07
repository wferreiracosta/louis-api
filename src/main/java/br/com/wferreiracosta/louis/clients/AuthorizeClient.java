package br.com.wferreiracosta.louis.clients;

import br.com.wferreiracosta.louis.models.responses.AuthorizeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "authorizeclient")
public interface AuthorizeClient {

    @GetMapping(value = "${client.transaction.authorize}")
    AuthorizeResponse authorize();

}
