package com.klaudia.transactionsimporter.imports;

import com.klaudia.transactionsimporter.imports.dto.ImportJobResponse;
import com.klaudia.transactionsimporter.imports.dto.ImportJobStatusResponse;
import com.klaudia.transactionsimporter.imports.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportJobResponse> importFile(@RequestParam("file") MultipartFile file) {
        ImportJobResponse importJobResponse = importService.importCsv(file);
        return ResponseEntity.accepted().body(importJobResponse);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<ImportJobStatusResponse> getStatus(@PathVariable String id) {
        ImportJobStatusResponse importJobStatusResponse = importService.checkImportStatus(id);
        return ResponseEntity.ok(importJobStatusResponse);
    }
}
