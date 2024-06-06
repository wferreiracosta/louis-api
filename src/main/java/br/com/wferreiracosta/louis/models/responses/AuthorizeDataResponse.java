package br.com.wferreiracosta.louis.models.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthorizeDataResponse(

        @JsonProperty("authorization")
        boolean authorization

) {
}
