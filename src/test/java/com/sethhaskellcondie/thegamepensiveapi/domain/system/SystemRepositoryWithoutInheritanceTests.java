package com.sethhaskellcondie.thegamepensiveapi.domain.system;

import com.sethhaskellcondie.thegamepensiveapi.domain.entity.system.System;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.system.SystemRepository;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.system.SystemRequestDto;
import com.sethhaskellcondie.thegamepensiveapi.domain.filter.Filter;
import com.sethhaskellcondie.thegamepensiveapi.domain.exceptions.ExceptionFailedDbValidation;
import com.sethhaskellcondie.thegamepensiveapi.domain.exceptions.ExceptionInvalidFilter;
import com.sethhaskellcondie.thegamepensiveapi.domain.exceptions.ExceptionMalformedEntity;
import com.sethhaskellcondie.thegamepensiveapi.domain.exceptions.ExceptionResourceNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This it the "before" for unit tests looked like before the
 * "after" when I wrote the abstract tests EntityRepositoryTests
 */
@Deprecated
@JdbcTest
@ActiveProfiles("test-container")
public class SystemRepositoryWithoutInheritanceTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private SystemRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SystemRepository(jdbcTemplate);
    }

    @Test
    void insertRequestDto_Success_ReturnEntity() throws ExceptionFailedDbValidation {
        final String name = "NES";
        final int generation = 3;
        final boolean handheld = false;
        final SystemRequestDto expected = new SystemRequestDto(name, generation, handheld, new ArrayList<>());

        final System actual = repository.insert(expected);

        assertNotNull(actual.getId());
        assertEquals(expected.name(), actual.getName());
        assertEquals(expected.generation(), actual.getGeneration());
        assertEquals(expected.handheld(), actual.isHandheld());
    }

    @Test
    void insertRequestDto_FailsEntityValidation_ThrowExceptionMalformedEntity() {
        final int generation = 3;
        final boolean handheld = false;
        final SystemRequestDto expected = new SystemRequestDto(null, generation, handheld, new ArrayList<>());

        assertThrows(ExceptionMalformedEntity.class, () -> repository.insert(expected));
    }

    @Test
    void insertEntity_Success_ReturnEntity() throws ExceptionFailedDbValidation {
        final String name = "SNES";
        final int generation = 4;
        final boolean handheld = false;
        final SystemRequestDto requestDto = new SystemRequestDto(name, generation, handheld, new ArrayList<>());
        final System expected = new System().updateFromRequestDto(requestDto);

        System actual = repository.insert(expected);

        assertNotNull(actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getGeneration(), actual.getGeneration());
        assertEquals(expected.isHandheld(), actual.isHandheld());
    }

    @Test
    void getWithFilters_StartsWithFilter_ReturnList() throws ExceptionFailedDbValidation, ExceptionInvalidFilter {
        final String name1 = "Epic Master System";
        final int generation1 = 3;
        final boolean handheld1 = false;
        final SystemRequestDto requestDto1 = new SystemRequestDto(name1, generation1, handheld1, new ArrayList<>());
        final System expected1 = repository.insert(requestDto1);

        final String name2 = "Epic Genesis";
        final int generation2 = 4;
        final boolean handheld2 = false;
        final SystemRequestDto requestDto2 = new SystemRequestDto(name2, generation2, handheld2, new ArrayList<>());
        final System expected2 = repository.insert(requestDto2);

        final Filter filter = new Filter("system", "text", "name", Filter.OPERATOR_STARTS_WITH, "Epic ", false);
        List<System> actual = repository.getWithFilters(List.of(filter));

        assertEquals(2, actual.size());
        assertEquals(expected1, actual.get(0));
        assertEquals(expected2, actual.get(1));
    }

    @Test
    void getWithFilters_NoFilters_ReturnEmptyList() throws ExceptionInvalidFilter {

        final Filter filter = new Filter("system", "text", "name", Filter.OPERATOR_STARTS_WITH, "NoResults", false);
        List<System> actual = repository.getWithFilters(List.of(filter));

        assertEquals(0, actual.size());
    }

    @Test
    void getById_Success_ReturnEntity() throws ExceptionFailedDbValidation, ExceptionResourceNotFound {
        final String name = "Playstation";
        final int generation = 5;
        final boolean handheld = false;
        final SystemRequestDto requestDto = new SystemRequestDto(name, generation, handheld, new ArrayList<>());
        final System expected = repository.insert(requestDto);

        final System actual = repository.getById(expected.getId());

        assertEquals(expected, actual);
    }

    @Test
    void getById_BadId_ThrowExceptionResourceNotFound() {
        assertThrows(ExceptionResourceNotFound.class, () -> repository.getById(-1));
    }

    @Test
    void update_Success_ReturnEntity() throws ExceptionFailedDbValidation, ExceptionInvalidFilter {
        final String name = "Nintendo 64";
        final int generation = 5;
        final boolean handheld = false;
        final SystemRequestDto requestDto = new SystemRequestDto(name, generation, handheld, new ArrayList<>());
        final System expected = repository.insert(requestDto);
        final int expectedId = expected.getId();

        final String updatedName = "Game Boy";
        final int updatedGeneration = 3;
        final boolean updatedHandheld = true;
        final SystemRequestDto updatedRequestDto = new SystemRequestDto(updatedName, updatedGeneration, updatedHandheld, new ArrayList<>());
        expected.updateFromRequestDto(updatedRequestDto);

        final System actual = repository.update(expected);

        assertEquals(expectedId, actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getGeneration(), actual.getGeneration());
        assertEquals(expected.isHandheld(), actual.isHandheld());
    }

    @Test
    void deleteById_Success_NoException() throws ExceptionFailedDbValidation, ExceptionResourceNotFound {
        final String name = "Playstation 2";
        final int generation = 6;
        final boolean handheld = false;
        final SystemRequestDto requestDto = new SystemRequestDto(name, generation, handheld, new ArrayList<>());
        final System expected = repository.insert(requestDto);
        final int expectedId = expected.getId();

        repository.deleteById(expectedId);

        assertThrows(ExceptionResourceNotFound.class, () -> repository.getById(expectedId));
    }

    @Test
    void deleteById_BadId_ThrowException() {
        assertThrows(ExceptionResourceNotFound.class, () -> repository.deleteById(-1));
    }

    // end of default implementation tests
    // below are tests specific to that entity (System)

    @Test
    void insert_duplicateNameFound_ThrowsExceptionFailedDbValidation() throws ExceptionFailedDbValidation {
        final String name = "Playstation 3";
        final int generation = 7;
        final boolean handheld = false;
        final SystemRequestDto requestDto = new SystemRequestDto(name, generation, handheld, new ArrayList<>());
        final System expected = repository.insert(requestDto);

        assertThrows(ExceptionFailedDbValidation.class, () -> repository.insert(expected));
    }

    @Test
    void update_duplicateNameFound_ThrowsExceptionFailedDbValidation() throws ExceptionFailedDbValidation {
        final String name = "Playstation 3";
        final int generation = 7;
        final boolean handheld = false;
        final SystemRequestDto requestDto = new SystemRequestDto(name, generation, handheld, new ArrayList<>());
        final System expected = repository.insert(requestDto);

        assertThrows(ExceptionFailedDbValidation.class, () -> repository.update(expected));
    }
}
