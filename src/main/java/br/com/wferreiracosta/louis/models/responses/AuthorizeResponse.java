package br.com.wferreiracosta.louis.models.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthorizeResponse(

        @JsonProperty("status")
        String status,

        @JsonProperty("data")
        AuthorizeDataResponse data

) {
}
