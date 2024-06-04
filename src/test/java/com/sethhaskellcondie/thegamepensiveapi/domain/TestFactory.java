package com.sethhaskellcondie.thegamepensiveapi.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sethhaskellcondie.thegamepensiveapi.domain.customfield.CustomField;
import com.sethhaskellcondie.thegamepensiveapi.domain.customfield.CustomFieldValue;
import com.sethhaskellcondie.thegamepensiveapi.domain.filter.Filter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestFactory {

    private final MockMvc mockMvc;

    public TestFactory(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private String randomString(int length) {
        return RandomStringUtils.random(length, true, true);
    }

    public void validateCustomFieldValues(ResultActions result, List<CustomFieldValue> expectedValues) throws Exception {
        result.andExpect(jsonPath("$.data.customFieldValues").isArray());

        for (int i = 0; i < expectedValues.size(); i++) {
            CustomFieldValue expectedValue = expectedValues.get(i);
            if (expectedValue.getCustomFieldId() == 0) {
                result.andExpectAll(
                        jsonPath("$.data.customFieldValues[" + i + "].customFieldId").isNotEmpty(),
                        jsonPath("$.data.customFieldValues[" + i + "].customFieldName").value(expectedValue.getCustomFieldName()),
                        jsonPath("$.data.customFieldValues[" + i + "].customFieldType").value(expectedValue.getCustomFieldType()),
                        jsonPath("$.data.customFieldValues[" + i + "].value").value(expectedValue.getValue())
                );
            } else {
                result.andExpectAll(
                        jsonPath("$.data.customFieldValues[" + i + "].customFieldId").value(expectedValue.getCustomFieldId()),
                        jsonPath("$.data.customFieldValues[" + i + "].customFieldName").value(expectedValue.getCustomFieldName()),
                        jsonPath("$.data.customFieldValues[" + i + "].customFieldType").value(expectedValue.getCustomFieldType()),
                        jsonPath("$.data.customFieldValues[" + i + "].value").value(expectedValue.getValue())
                );
            }
        }
    }

    public String formatFiltersPayload(Filter filter) {
        final String json = """
                {
                  "filters": [
                    {
                      "key": "%s",
                      "field": "%s",
                      "operator": "%s",
                      "operand": "%s"
                    }
                  ]
                }
                """;
        return String.format(json, filter.getKey(), filter.getField(), filter.getOperator(), filter.getOperand());
    }

    public int postCustomFieldReturnId(String name, String type, String entityKey) throws Exception {
        ResultActions result = postCustomCustomField(name, type, entityKey);
        final MvcResult mvcResult = result.andReturn();
        final String responseString = mvcResult.getResponse().getContentAsString();
        final Map<String, CustomField> body = new ObjectMapper().readValue(responseString, new TypeReference<>() { });
        return body.get("data").id();
    }

    public ResultActions postCustomField() throws Exception {
        final String name = "TestCustomField-" + randomString(6);
        final String type = "text";
        final String entityKey = "toy";
        return postCustomCustomField(name, type, entityKey);
    }

    public ResultActions postCustomCustomField(String name, String type, String entityKey) throws Exception {
        final String formattedJson = formatCustomFieldPayload(name, type, entityKey);

        final ResultActions result = mockMvc.perform(
                post("/v1/custom_fields")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(formattedJson)
        );

        result.andExpect(status().isCreated());
        return result;
    }

    public String formatCustomFieldPayload(String name, String type, String entityKey) {
        final String json = """
                {
                	"custom_field": {
                	    "name": "%s",
                	    "type": "%s",
                	    "entityKey": "%s"
                	    }
                }
                """;
        return String.format(json, name, type, entityKey);
    }

    public String formatCustomFieldValues(List<CustomFieldValue> customFieldValues) {
        if (null == customFieldValues || customFieldValues.isEmpty()) {
            return "[]";
        }
        String customFieldValuesArray = """
                [
                    %s
                ]
                """;
        List<String> customFieldsStrings = new ArrayList<>();
        for (int i = 0; i < customFieldValues.size(); i++) {
            customFieldsStrings.add(formatCustomField(customFieldValues.get(i), i == (customFieldValues.size() - 1)));
        }
        return String.format(customFieldValuesArray, String.join("\n", customFieldsStrings));
    }

    private String formatCustomField(CustomFieldValue value, boolean last) {
        String customFieldString;
        if (last) {
            customFieldString = """
                    {
                        "customFieldId": %d,
                        "customFieldName": "%s",
                        "customFieldType": "%s",
                        "value": "%s"
                    }
                """;
        } else {
            customFieldString = """
                    {
                        "customFieldId": %d,
                        "customFieldName": "%s",
                        "customFieldType": "%s",
                        "value": "%s"
                    },
                """;
        }
        return String.format(customFieldString, value.getCustomFieldId(), value.getCustomFieldName(), value.getCustomFieldType(), value.getValue());
    }

    public ResultActions postSystem() throws Exception {
        final String name = "TestSystem-" + randomString(8);
        final int generation = 1;
        final boolean handheld = false;

        return postCustomSystem(name, generation, handheld, null);
    }

    public ResultActions postCustomSystem(String name, int generation, boolean handheld, List<CustomFieldValue> customFieldValues) throws Exception {
        final String customFieldValuesString = formatCustomFieldValues(customFieldValues);
        final String json = """
                {
                  "system": {
                    "name": "%s",
                    "generation": %d,
                    "handheld": %b,
                    "customFieldValues": %s
                  }
                }
                """;
        final String formattedJson = String.format(json, name, generation, handheld, customFieldValuesString);

        final ResultActions result = mockMvc.perform(
                post("/v1/systems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(formattedJson)
        );

        result.andExpect(status().isCreated());
        return result;
    }

    public String formatSystemPayload(String name, Integer generation, Boolean handheld, List<CustomFieldValue> customFieldValues) {
        final String customFieldValuesString = formatCustomFieldValues(customFieldValues);
        final String json = """
                {
                    "system": {
                        "name": "%s",
                        "generation": %d,
                        "handheld": %b,
                        "customFieldValues": %s
                    }
                }
                """;
        return String.format(json, name, generation, handheld, customFieldValuesString);
    }

    public ResultActions postToy() throws Exception {
        final String name = "TestToy-" + randomString(4);
        final String set = "TestSet-" + randomString(4);
        return postCustomToy(name, set, null);
    }

    public ResultActions postCustomToy(String name, String set, List<CustomFieldValue> customFieldValues) throws Exception {
        final String formattedJson = formatToyPayload(name, set, customFieldValues);

        final ResultActions result = mockMvc.perform(
                post("/v1/toys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(formattedJson)
        );

        result.andExpect(status().isCreated());
        return result;
    }

    public String formatToyPayload(String name, String set, List<CustomFieldValue> customFieldValues) {
        final String customFieldValuesString = formatCustomFieldValues(customFieldValues);
        final String json = """
                {
                	"toy": {
                	    "name": "%s",
                	    "set": "%s",
                        "customFieldValues": %s
                	    }
                }
                """;
        return String.format(json, name, set, customFieldValuesString);
    }
}
