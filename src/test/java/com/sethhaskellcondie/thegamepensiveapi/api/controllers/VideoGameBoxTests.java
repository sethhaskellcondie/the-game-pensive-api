package com.sethhaskellcondie.thegamepensiveapi.api.controllers;

import com.sethhaskellcondie.thegamepensiveapi.TestFactory;
import com.sethhaskellcondie.thegamepensiveapi.domain.Keychain;
import com.sethhaskellcondie.thegamepensiveapi.domain.customfield.CustomFieldValue;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.system.SystemResponseDto;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.videogame.SlimVideoGame;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.videogame.VideoGameRequestDto;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.videogamebox.VideoGameBoxResponseDto;
import com.sethhaskellcondie.thegamepensiveapi.domain.filter.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Video game boxes represent how the games appear in a collection, either on a shelf physically or in a launcher on a digital space.
 * Boxes can be physical, or digital, they can also be a collection of other games.
 * Because of this video games are created and deleted through the video game boxes endpoints.
 * Video game boxes have all the basic CRUD functions.
 * Video game boxes must have a title, system, and at least one video game.
 * This test suite will focus on the video game boxes, the video game functionality will be tested in the VideoGameTests suite.
 */

@SpringBootTest
@ActiveProfiles("test-container")
@AutoConfigureMockMvc
public class VideoGameBoxTests {

    @Autowired
    private MockMvc mockMvc;
    private TestFactory factory;
    private final String baseUrl = "/v1/videoGameBoxes";
    private final String baseUrlSlash = "/v1/videoGameBoxes/";


    @BeforeEach
    void setUp() {
        factory = new TestFactory(mockMvc);
    }

    @Test
    void postVideoGameBoxWithCustomFieldValues_ValidPayload_VideoGameBoxCreatedAndReturned() throws Exception {
        final String expectedTitle = "Princess Peach Showtime";
        final SystemResponseDto relatedSystem = factory.postSystem();
        final VideoGameRequestDto relatedVideoGame = new VideoGameRequestDto(expectedTitle, relatedSystem.id(), new ArrayList<>());
        final List<SlimVideoGame> expectedVideoGameResults = List.of(convertToExpectedSlimVideoGameResponse(relatedVideoGame, relatedSystem));
        final boolean isPhysical = true;
        final boolean isCollection = false;
        final List<CustomFieldValue> expectedCustomFieldValues = List.of(
                new CustomFieldValue(0, "Includes Manual", "boolean", "true"),
                new CustomFieldValue(0, "Max Players", "number", "1"),
                new CustomFieldValue(0, "Developer", "text", "Nintendo")
        );

        final ResultActions result = factory.postVideoGameBoxReturnResult(expectedTitle, relatedSystem.id(), new ArrayList<>(), List.of(relatedVideoGame), isPhysical, expectedCustomFieldValues);

        factory.validateVideoGameBoxResponseBody(result, expectedTitle, relatedSystem, expectedVideoGameResults, isPhysical, isCollection, expectedCustomFieldValues);

        final VideoGameBoxResponseDto responseDto = factory.resultToVideoGameBoxResponseDto(result);
        updateExistingVideoGameBox_UpdateVideoGameBoxAndCustomFieldValue_ReturnOk(responseDto, responseDto.customFieldValues());
    }

    void updateExistingVideoGameBox_UpdateVideoGameBoxAndCustomFieldValue_ReturnOk(VideoGameBoxResponseDto existingVideoGameBox, List<CustomFieldValue> existingCustomFieldValue) throws Exception {
        final String updatedTitle = "Super Princess Peach";
        final SystemResponseDto newRelatedSystem = factory.postSystem();
        List<Integer> existingVideoGameIds = new ArrayList<>();
        for (SlimVideoGame existingVideoGame : existingVideoGameBox.videoGames()) {
            existingVideoGameIds.add(existingVideoGame.id());
        }
        final boolean newPhysical = false;
        final boolean newCollection = false;
        final CustomFieldValue customFieldValueToUpdate = existingCustomFieldValue.get(0);
        existingCustomFieldValue.remove(0);
        final CustomFieldValue updatedValue = new CustomFieldValue(
                customFieldValueToUpdate.getCustomFieldId(),
                "Updated" + customFieldValueToUpdate.getCustomFieldName(),
                customFieldValueToUpdate.getCustomFieldType(),
                "false"
        );
        existingCustomFieldValue.add(updatedValue);

        final String jsonContent = factory.formatVideoGameBoxPayload(updatedTitle, newRelatedSystem.id(), existingVideoGameIds, new ArrayList<>(), newPhysical, existingCustomFieldValue);
        final ResultActions result = mockMvc.perform(
                put(baseUrlSlash + existingVideoGameBox.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
        );

        result.andExpect(status().isOk());
        factory.validateVideoGameBoxResponseBody(result, updatedTitle, newRelatedSystem, existingVideoGameBox.videoGames(), newPhysical, newCollection, existingCustomFieldValue);
    }


    @Test
    void postVideoGameBox_TitleBlankInvalidSystemIdMissingVideoGames_ReturnBadRequest() throws Exception {
        // Two errors total:
        // 1) The title cannot be blank.
        // 2) The systemId must be a valid int greater than zero.
        // TODO test for this 3? Both video games lists are blank.
        final String jsonContent = factory.formatVideoGameBoxPayload("", -1, List.of(), List.of(), false, null);

        final ResultActions result = mockMvc.perform(
                post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
        );

        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.data").isEmpty(),
                jsonPath("$.errors.size()").value(2)
        );
    }

    @Test
    void postVideoGameBox_SystemIdInvalid_ReturnBadRequest() throws Exception {
        //This test is a little different from the last one, in this one we are passing a valid int for the systemId
        //but there is not a matching system in the database for that id, so the error message will be different.
        final VideoGameRequestDto newVideoGame = new VideoGameRequestDto("title", Integer.MAX_VALUE, new ArrayList<>());
        final String jsonContent = factory.formatVideoGameBoxPayload("Valid Title", Integer.MAX_VALUE, List.of(), List.of(newVideoGame), false, null);

        final ResultActions result = mockMvc.perform(
                post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
        );

        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.data").isEmpty(),
                jsonPath("$.errors.size()").value(1)
        );
    }

    @Test
    void postVideoGameBox_VideoGameIdInvalid_ReturnBadRequest() throws Exception {
        //This test is a little different from the last one, in this one we are passing in a valid int for the systemId
        //but one of the video game ids that is passed it is invalid.
        final SystemResponseDto existingSystem = factory.postSystem();
        final String jsonContent = factory.formatVideoGameBoxPayload("Valid Title", existingSystem.id(), List.of(Integer.MAX_VALUE), List.of(), false, null);

        final ResultActions result = mockMvc.perform(
                post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
        );

        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.data").isEmpty(),
                jsonPath("$.errors.size()").value(1)
        );
    }

    @Test
    void getOneVideoGameBox_GameBoxExists_VideoGameBoxSerializedCorrectly() throws Exception {
        final String title = "Super Mario Bros. 3";
        final SystemResponseDto relatedSystem = factory.postSystem();
        final VideoGameRequestDto newVideoGame = new VideoGameRequestDto(title, relatedSystem.id(), new ArrayList<>());
        final boolean physical = true;
        final boolean collection = false;
        final List<SlimVideoGame> expectedVideoGames = List.of(convertToExpectedSlimVideoGameResponse(newVideoGame, relatedSystem));
        final List<CustomFieldValue> customFieldValues = List.of(new CustomFieldValue(0, "customFieldName", "text", "value"));
        ResultActions postResult = factory.postVideoGameBoxReturnResult(title, relatedSystem.id(), new ArrayList<>(), List.of(newVideoGame), physical, customFieldValues);
        final VideoGameBoxResponseDto expectedDto = factory.resultToVideoGameBoxResponseDto(postResult);

        final ResultActions result = mockMvc.perform(get(baseUrlSlash + expectedDto.id()));

        result.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON)
        );
        factory.validateVideoGameBoxResponseBody(result, expectedDto.title(), relatedSystem, expectedVideoGames, physical, collection, customFieldValues);
    }

    @Test
    void getOneVideoGameBox_VideoGameBoxMissing_NotFoundReturned() throws Exception {
        final ResultActions result = mockMvc.perform(get(baseUrl + "/-1"));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.data").isEmpty(),
                jsonPath("$.errors.size()").value(1)
        );
    }

    @Test
    void getAllVideoGameBoxes_StartsWithFilter_VideoGameBoxListReturned() throws Exception {
        //This is used in the following test
        final String customFieldName = "Custom";
        final String customFieldType = "number";
        final String customFieldKey = Keychain.VIDEO_GAME_BOX_KEY;
        final int customFieldId = factory.postCustomFieldReturnId(customFieldName, customFieldType, customFieldKey);

        final String title1 = "NES Mega Man";
        final SystemResponseDto relatedSystem1 = factory.postSystem();
        final VideoGameRequestDto relatedGame1 = new VideoGameRequestDto(title1, relatedSystem1.id(), new ArrayList<>());
        final List<CustomFieldValue> CustomFieldValues1 = List.of(new CustomFieldValue(customFieldId, customFieldName, customFieldType, "1"));
        final ResultActions result1 = factory.postVideoGameBoxReturnResult(title1, relatedSystem1.id(), List.of(), List.of(relatedGame1), false, CustomFieldValues1);
        final VideoGameBoxResponseDto gameBoxDto1 = factory.resultToVideoGameBoxResponseDto(result1);

        final String title2 = "NES Mega Man 2";
        final SystemResponseDto relatedSystem2 = factory.postSystem();
        final VideoGameRequestDto relatedGame2 = new VideoGameRequestDto(title2, relatedSystem2.id(), new ArrayList<>());
        final List<CustomFieldValue> CustomFieldValues2 = List.of(new CustomFieldValue(customFieldId, customFieldName, customFieldType, "2"));
        final ResultActions result2 = factory.postVideoGameBoxReturnResult(title2, relatedSystem2.id(), List.of(), List.of(relatedGame2), false, CustomFieldValues2);
        final VideoGameBoxResponseDto gameBoxDto2 = factory.resultToVideoGameBoxResponseDto(result2);

        final String title3 = "SNES Mega Man 7";
        final SystemResponseDto relatedSystem3 = factory.postSystem();
        final VideoGameRequestDto relatedGame3 = new VideoGameRequestDto(title3, relatedSystem3.id(), new ArrayList<>());
        final List<CustomFieldValue> CustomFieldValues3 = List.of(new CustomFieldValue(customFieldId, customFieldName, customFieldType, "3"));
        final ResultActions result3 = factory.postVideoGameBoxReturnResult(title3, relatedSystem3.id(), List.of(), List.of(relatedGame3), false, CustomFieldValues3);
        final VideoGameBoxResponseDto gameBoxDto3 = factory.resultToVideoGameBoxResponseDto(result3);

        final Filter filter = new Filter(Keychain.VIDEO_GAME_BOX_KEY, "text", "title", Filter.OPERATOR_STARTS_WITH, "NES ", false);
        final String formattedJson = factory.formatFiltersPayload(filter);

        final ResultActions result = mockMvc.perform(post(baseUrl + "/function/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(formattedJson)
        );

        result.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON)
        );
        factory.validateVideoGameBoxResponseBody(result, List.of(gameBoxDto1, gameBoxDto2));

        final Filter customFilter = new Filter(customFieldKey, customFieldType, customFieldName, Filter.OPERATOR_GREATER_THAN, "2", true);
        getWithFilters_GreaterThanCustomFilter_VideoGameBoxListReturned(customFilter, List.of(gameBoxDto3));
    }

    void getWithFilters_GreaterThanCustomFilter_VideoGameBoxListReturned(Filter filter, List<VideoGameBoxResponseDto> expectedGames) throws Exception {

        final String jsonContent = factory.formatFiltersPayload(filter);

        final ResultActions result = mockMvc.perform(post(baseUrl + "/function/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
        );

        result.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON)
        );
        factory.validateVideoGameBoxResponseBody(result, expectedGames);
    }

    @Test
    void getAllVideoGameBoxes_NoResultFilter_EmptyArrayReturned() throws Exception {
        final Filter filter = new Filter(Keychain.VIDEO_GAME_BOX_KEY, "text", "title", Filter.OPERATOR_STARTS_WITH, "NoResults", false);
        final String formattedJson = factory.formatFiltersPayload(filter);
        final ResultActions result = mockMvc.perform(post(baseUrl + "/function/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(formattedJson)
        );

        result.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.data").value(new ArrayList<>()),
                jsonPath("$.errors").isEmpty()
        );
    }

    @Test
    void updateExistingVideoGameBox_InvalidId_ReturnNotFound() throws Exception {

        final String jsonContent = factory.formatVideoGameBoxPayload("invalidId", 1, List.of(), List.of(), false, null);
        final ResultActions result = mockMvc.perform(
                put(baseUrl + "/-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
        );

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.data").isEmpty(),
                jsonPath("$.errors.size()").value(1)
        );
    }

    @Test
    void deleteExistingVideoGameBox_VideoGameBoxExists_ReturnNoContent() throws Exception {
        final VideoGameBoxResponseDto responseDto = factory.postVideoGameBox();

        final ResultActions result = mockMvc.perform(
                delete(baseUrl + "/" + responseDto.id())
        );

        result.andExpectAll(
                status().isNoContent(),
                jsonPath("$.data").isEmpty(),
                jsonPath("$.errors").isEmpty()
        );
    }

    @Test
    void deleteExistingVideoGameBox_InvalidId_ReturnNotFound() throws Exception {
        final ResultActions result = mockMvc.perform(
                delete(baseUrl + "/-1")
        );

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.data").isEmpty(),
                jsonPath("$.errors.size()").value(1)
        );
    }

    private SlimVideoGame convertToExpectedSlimVideoGameResponse(VideoGameRequestDto requestDto, SystemResponseDto expectedSystem) {
        return new SlimVideoGame(0, requestDto.title(), expectedSystem, null, null, null, requestDto.customFieldValues());
    }
}
