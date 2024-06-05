package br.com.wferreiracosta.louis.controllers.impl;

import br.com.wferreiracosta.louis.controllers.TransactionController;
import br.com.wferreiracosta.louis.models.dtos.TransactionDTO;
import br.com.wferreiracosta.louis.models.parameters.TransactionParameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionControllerImpl implements TransactionController {

    @Override
    public TransactionDTO transfer(@Valid @RequestBody final TransactionParameter parameter) {
        return null;
    }

}
