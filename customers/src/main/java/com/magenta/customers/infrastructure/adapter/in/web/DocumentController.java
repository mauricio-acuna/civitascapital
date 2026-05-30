package com.magenta.customers.infrastructure.adapter.in.web;

import com.magenta.customers.application.*;
import com.magenta.customers.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{id}/documents")
@Tag(name = "Documents", description = "Documentación del cliente")
@RequiredArgsConstructor
public class DocumentController {

    private final UploadDocumentUseCase uploadDocument;
    private final com.magenta.customers.domain.port.out.DocumentRepository documentRepo;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "UC-C5: Subir documento")
    public DocumentRef upload(
            @PathVariable UUID id,
            @RequestParam DocumentKind kind,
            @RequestPart MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) throws IOException {

        return uploadDocument.execute(new UploadDocumentUseCase.Command(
                id, kind, file.getOriginalFilename(), file.getContentType(),
                file.getSize(), file.getInputStream(), jwt.getSubject()));
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('AGENT') or hasRole('ADMIN')")
    @Operation(summary = "Listar documentos del cliente")
    public List<DocumentRef> list(@PathVariable UUID id) {
        return documentRepo.findByCustomerId(id);
    }
}
