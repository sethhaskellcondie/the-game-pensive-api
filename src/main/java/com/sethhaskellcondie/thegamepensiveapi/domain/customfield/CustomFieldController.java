package com.sethhaskellcondie.thegamepensiveapi.domain.customfield;

import com.sethhaskellcondie.thegamepensiveapi.api.FormattedResponseBody;
import com.sethhaskellcondie.thegamepensiveapi.exceptions.ExceptionFailedDbValidation;
import com.sethhaskellcondie.thegamepensiveapi.exceptions.ExceptionResourceNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("v1/custom_fields")
public class CustomFieldController {
    private final CustomFieldRepository repository;

    public CustomFieldController(CustomFieldRepository repository) {
        this.repository = repository;
    }

    @ResponseBody
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, CustomField> createNewCustomField(@RequestBody Map<String, CustomFieldRequestDto> requestBody) throws ExceptionFailedDbValidation {
        final CustomFieldRequestDto newCustomField = requestBody.get("custom_field");
        final CustomField savedCustomField = repository.insertCustomField(newCustomField);
        final FormattedResponseBody<CustomField> body = new FormattedResponseBody<>(savedCustomField);
        return body.formatData();
    }

    @ResponseBody
    @GetMapping("")
    public Map<String, List<CustomField>> getAllCustomFields() {
        final List<CustomField> allCustomFields = repository.getAllCustomFields();
        final FormattedResponseBody<List<CustomField>> body = new FormattedResponseBody<>(allCustomFields);
        return body.formatData();
    }

    @ResponseBody
    @PatchMapping("/{id}")
    public Map<String, CustomField> patchName(@PathVariable int id, @RequestBody Map<String, String> requestBody) throws ExceptionResourceNotFound {
        final String newName = requestBody.get("name");
        final CustomField updatedCustomField = repository.updateName(id, newName);
        final FormattedResponseBody<CustomField> body = new FormattedResponseBody<>(updatedCustomField);
        return body.formatData();
    }

    @ResponseBody
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Map<String, String> deleteExistingCustomField(@PathVariable int id) throws ExceptionResourceNotFound {
        repository.deleteById(id);
        FormattedResponseBody<String> body = new FormattedResponseBody<>("");
        return body.formatData();
    }

    @ExceptionHandler({NoSuchElementException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, List<String>> handleException(NoSuchElementException exception) {
        FormattedResponseBody<List<String>> body = new FormattedResponseBody<>(List.of("Problem getting count from the database."));
        return body.formatError();
    }
}
