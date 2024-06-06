package br.com.wferreiracosta.louis.clients;

import br.com.wferreiracosta.louis.models.responses.AuthorizeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "authorizeclient", url = "${client.transaction.authorize}")
public interface AuthorizeClient {

    @GetMapping
    AuthorizeResponse authorize();

}
