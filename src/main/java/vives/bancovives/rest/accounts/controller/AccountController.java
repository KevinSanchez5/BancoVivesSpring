package vives.bancovives.rest.accounts.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import vives.bancovives.rest.accounts.dto.input.InputAccount;
import vives.bancovives.rest.accounts.dto.output.OutputAccount;
import vives.bancovives.rest.accounts.mapper.AccountMapper;
import vives.bancovives.rest.accounts.service.AccountService;
import vives.bancovives.rest.clients.dto.output.ClientResponseDto;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("${api.version}/accounts")
public class AccountController {

    private final AccountService accountService;
    private final PaginationLinksUtils paginationLinksUtils;

    @Autowired
    public AccountController(
            AccountService accountService,
            PaginationLinksUtils paginationLinksUtils
    ) {
        this.accountService = accountService;
        this.paginationLinksUtils = paginationLinksUtils;
    }

    @GetMapping
    @PreAuthorize( "hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PageResponse<OutputAccount>> getAccounts(
            @RequestParam(required = false) Optional<String> iban,
            @RequestParam(required = false) Optional<String> clientDni,
            @RequestParam(required = false) Optional<String> accountTypeName,
            @RequestParam(required = false) Optional<Boolean> isDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            HttpServletRequest request
    ){
        log.info("Buscando todas las cuentas");

        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ?Sort.by(sortBy).ascending() :Sort.by(sortBy).descending();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString());

        Page<OutputAccount> pageResult = accountService.findAll(iban, clientDni, accountTypeName, isDeleted, PageRequest.of(page,size,sort))
                .map(AccountMapper::toOutputAccount);
        return ResponseEntity.ok()
                .header("link ", paginationLinksUtils.createLinkHeader(pageResult,uriBuilder))
                .body(PageResponse.of(pageResult,sortBy,direction));
    }

    @GetMapping("/{id}")
    @PreAuthorize(" hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<OutputAccount> getAccountById(@PathVariable String id){
        log.info("Buscando cuenta con id {}", id);
        return ResponseEntity.ok(
                AccountMapper.toOutputAccount(
                        accountService.findById(id)
                )
        );
    }


    @PostMapping
    @PreAuthorize( "hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<OutputAccount> createAccount(@RequestBody @Valid InputAccount inputAccount){
        log.info("Creando cuenta");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        AccountMapper.toOutputAccount(
                                accountService.save(inputAccount)
                        )
        );
    }

    @GetMapping("/iban/{iban}")
    @PreAuthorize( "hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<OutputAccount> getAccountByIban(@PathVariable String iban) {
        log.info("Buscando cuenta con iban {}", iban);
        return ResponseEntity.ok(
                AccountMapper.toOutputAccount(
                        accountService.findByIban(iban)
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize( "hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<OutputAccount>updateAccount(@PathVariable String id , @RequestBody @Valid InputAccount inputAccount){
        log.info("Actualizando una cuenta com id {}", id);
        return ResponseEntity.ok(
                AccountMapper.toOutputAccount(
                        accountService.updateById(id, inputAccount)
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize( "hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<OutputAccount> deleteAccount(@PathVariable String id){
        log.info("Eliminando cuenta con id {}", id);
        return ResponseEntity.ok(
                AccountMapper.toOutputAccount(
                        accountService.deleteById(id)
                )
        );
    }

    @GetMapping("/myAccounts")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<OutputAccount>> findMyAccounts(Principal principal){
        log.info("Buscando su informacion");
        return ResponseEntity.ok(accountService.findMyAccounts(principal).stream().map(AccountMapper::toOutputAccount).toList());
    }




}
