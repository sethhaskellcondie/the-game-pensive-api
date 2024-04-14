package com.sethhaskellcondie.thegamepensiveapi.domain.system;

import com.sethhaskellcondie.thegamepensiveapi.api.FormattedResponseBody;
import com.sethhaskellcondie.thegamepensiveapi.domain.filter.Filter;
import com.sethhaskellcondie.thegamepensiveapi.exceptions.ExceptionFailedDbValidation;
import com.sethhaskellcondie.thegamepensiveapi.exceptions.ExceptionResourceNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * A Controller has three responsibilities, first process requests, deserialize and validate input and
 * then pass it to the appropriate gateway. Second expose domain functionality through endpoints.
 * Third process responses this is mostly changing exceptions into the appropriate errors, this is done
 * with the @ControllerAdvice classes. (like ExceptionHandler.java)
 * <p>
 * When a controller processes a request, it will validate the input, just the input. The input is usually
 * in the form of a requestDto validation for that is found in the constructor for that object.
 * <p>
 * Every entity will have the same base CRUD functionality in the domain. If a CRUD function shouldn't
 * be used through the api then that function will not have an endpoint. (Don't make a PUT endpoint if api
 * users shouldn't edit an entity.)
 * <p>
 * Controllers only interact with Gateways, but reference the shape of Dto objects that are defined
 * in the entity. Gateways will always take a RequestDto and will return either an appropriate
 * ResponseDto or an error, the controller will then format the response.
 */
@RestController
@RequestMapping("systems")
public class SystemController {
    private final SystemGateway gateway;

    public SystemController(SystemGateway gateway) {
        this.gateway = gateway;
    }

    @ResponseBody
    @GetMapping("/{id}")
    public Map<String, SystemResponseDto> getOneSystem(@PathVariable int id) throws ExceptionResourceNotFound {
        final SystemResponseDto responseDto = gateway.getById(id);
        final FormattedResponseBody<SystemResponseDto> body = new FormattedResponseBody<>(responseDto);
        return body.formatData();
    }

    /**
     * The "Get All" endpoint is a POST endpoint instead of a GET endpoint.
     * This will allow the consumer to pass the filters as an object in the request body
     * instead of through many query parameters in a get request.
     */
    @ResponseBody
    @PostMapping("/search")
    public Map<String, List<SystemResponseDto>> getAllSystems(@RequestBody List<Filter> filters) {
        final List<SystemResponseDto> data = gateway.getWithFilters(filters);
        final FormattedResponseBody<List<SystemResponseDto>> body = new FormattedResponseBody<>(data);
        return body.formatData();
    }

    @ResponseBody
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, SystemResponseDto> createNewSystem(@RequestBody SystemRequestDto system) throws ExceptionFailedDbValidation {
        final SystemResponseDto responseDto = gateway.createNew(system);
        final FormattedResponseBody<SystemResponseDto> body = new FormattedResponseBody<>(responseDto);
        return body.formatData();
    }

    @ResponseBody
    @PutMapping("/{id}")
    public Map<String, SystemResponseDto> updateExistingSystem(@PathVariable int id, @RequestBody SystemRequestDto system) throws ExceptionFailedDbValidation, ExceptionResourceNotFound {
        final SystemResponseDto responseDto = gateway.updateExisting(id, system);
        final FormattedResponseBody<SystemResponseDto> body = new FormattedResponseBody<>(responseDto);
        return body.formatData();
    }

    @ResponseBody
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Map<String, String> deleteExistingSystem(@PathVariable int id) throws ExceptionResourceNotFound {
        gateway.deleteById(id);
        FormattedResponseBody<String> body = new FormattedResponseBody<>("");
        return body.formatData();
    }
}
