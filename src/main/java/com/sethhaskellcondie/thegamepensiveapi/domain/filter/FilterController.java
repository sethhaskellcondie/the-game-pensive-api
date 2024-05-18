package com.sethhaskellcondie.thegamepensiveapi.domain.filter;

import com.sethhaskellcondie.thegamepensiveapi.api.FormattedResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("filters")
public class FilterController {
    private final FilterGateway gateway;

    public FilterController(FilterGateway gateway) {
        this.gateway = gateway;
    }

    @ResponseBody
    @GetMapping("/{key}")
    public Map<String, FilterResponseDto> getFiltersByKey(@PathVariable String key) {
        final FilterResponseDto responseDto = gateway.getFiltersByKey(key);
        final FormattedResponseBody<FilterResponseDto> body = new FormattedResponseBody<>(responseDto);
        return body.formatData();
    }
}
