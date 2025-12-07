package com.learnfirebase.ecommerce.product.adapter.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.product.application.command.CreateProductReportCommand;
import com.learnfirebase.ecommerce.product.application.dto.ProductReportDto;
import com.learnfirebase.ecommerce.product.application.port.in.ManageProductReportUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ProductReportController {
    private final ManageProductReportUseCase manageProductReportUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createReport(@RequestBody CreateProductReportCommand command, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : "anonymous";
        command.setUserId(userId);
        manageProductReportUseCase.createReport(command);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductReportDto> listReports() {
        return manageProductReportUseCase.listReports();
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public void resolveReport(@PathVariable String id) {
        manageProductReportUseCase.resolveReport(id);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public void rejectReport(@PathVariable String id) {
        manageProductReportUseCase.rejectReport(id);
    }
}
