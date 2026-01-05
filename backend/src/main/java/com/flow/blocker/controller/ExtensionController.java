package com.flow.blocker.controller;

import com.flow.blocker.dto.CustomExtensionRequest;
import com.flow.blocker.dto.ExtensionResponse;
import com.flow.blocker.service.ExtensionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/extensions")
@RequiredArgsConstructor
public class ExtensionController {

    private final ExtensionService extensionService;

    @GetMapping
    public ResponseEntity<ExtensionResponse> getAllExtensions() {
        return ResponseEntity.ok(extensionService.getAllExtensions());
    }

    @PatchMapping("/fixed/{extension}")
    public ResponseEntity<Void> toggleFixedExtension(@PathVariable String extension) {
        extensionService.toggleFixedExtension(extension);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/custom")
    public ResponseEntity<Void> addCustomExtension(@Valid @RequestBody CustomExtensionRequest request) {
        extensionService.addCustomExtension(request.getExtension());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/custom/{extension}")
    public ResponseEntity<Void> deleteCustomExtension(@PathVariable String extension) {
        extensionService.deleteCustomExtension(extension);
        return ResponseEntity.ok().build();
    }
}
