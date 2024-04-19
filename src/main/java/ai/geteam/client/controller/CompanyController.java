package ai.geteam.client.controller;

import ai.geteam.client.dto.CompanyCountryInfoDTO;
import ai.geteam.client.dto.CompanyDTO;
import ai.geteam.client.service.company.CompanyService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@RestController
@RequestMapping("v1/clients/companies")
@RequiredArgsConstructor

public class CompanyController {

    private final CompanyService service;


    @RateLimiter(name = "createClient")
    @PostMapping
    public ResponseEntity<String> create(@RequestBody CompanyDTO companyDTO) {
        String id = service.create(companyDTO);
        return ResponseEntity.created(URI.create("api/v1/clients/companies/" + id)).body(id);
    }


    @RateLimiter(name = "deleteClient")
    @DeleteMapping(path = "{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }


    @PutMapping("/company")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<CompanyDTO> updateCompanyById(@RequestBody CompanyDTO company, @RequestHeader(name = "Authorization") String authorization) {
        return ResponseEntity.status(HttpStatus.OK).body(service.updateCompanyById(company, authorization));
    }

    @GetMapping("/company")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<CompanyCountryInfoDTO> getCompanyInfo(@RequestHeader(name = "Authorization") String authorization) {
        return ResponseEntity.ok(service.getCompanyInfo(authorization));
    }

}
