package com.sethhaskellcondie.thegamepensiveapi.domain.system;

import com.sethhaskellcondie.thegamepensiveapi.domain.EntityRepositoryTests;
import com.sethhaskellcondie.thegamepensiveapi.exceptions.ExceptionFailedDbValidation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SystemRepositoryTests extends EntityRepositoryTests<System, SystemRequestDto, SystemResponseDto> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    protected void setupRepositoryAndEntityName() {
        entityName = System.class.getSimpleName();
        repository = new SystemRepository(jdbcTemplate);
    }

    @Override
    protected System generateValidEntity() {
        final String name = "SNES";
        final int generation = 4;
        final boolean handheld = false;
        SystemRequestDto requestDto = new SystemRequestDto(name, generation, handheld);
        return new System().updateFromRequestDto(requestDto);
    }

    @Override
    protected SystemRequestDto generateRequestDto(Generate generate) {
        switch (generate) {
            case VALID -> {
                final String name = "NES";
                final int generation = 3;
                final boolean handheld = false;
                return new SystemRequestDto(name, generation, handheld);
            }
            case VALID_SECOND -> {
                final String name = "Master System";
                final int generation = 3;
                final boolean handheld = false;
                return new SystemRequestDto(name, generation, handheld);
            }
            case INVALID -> {
                final String name = ""; //the name cannot be blank
                final int generation = 3;
                final boolean handheld = false;
                return new SystemRequestDto(name, generation, handheld);
            }
            default -> {
                return new SystemRequestDto(null, null, null);
            }
        }
    }

    @Override
    protected void validateReturnedObject(System expected, System actual) {
        assertAll(
            "These " + entityName + " objects are invalid.",
            () -> assertNotNull(actual.getId()),
            () -> assertEquals(expected.getName(), actual.getName()),
            () -> assertEquals(expected.getGeneration(), actual.getGeneration()),
            () -> assertEquals(expected.isHandheld(), actual.isHandheld())
        );
    }

    @Override
    protected void validateReturnedObject(SystemRequestDto expected, System actual) {
        assertAll(
            "These " + entityName + " objects are invalid.",
            () -> assertNotNull(actual.getId()),
            () -> assertEquals(expected.name(), actual.getName()),
            () -> assertEquals(expected.generation(), actual.getGeneration()),
            () -> assertEquals(expected.handheld(), actual.isHandheld())
        );
    }

    //End the tests for the default implementation, below are tests specific for that entity in this case System

    @Test
    void insert_duplicateNameFound_ThrowsExceptionFailedDbValidation() throws ExceptionFailedDbValidation {
        final SystemRequestDto requestDto = generateRequestDto(Generate.VALID);
        final System expected = repository.insert(requestDto);

        assertThrows(ExceptionFailedDbValidation.class, () -> repository.insert(expected),
                "The " + entityName + " repository allowed an insert of an object with a duplicate name when it shouldn't have.");
    }

    @Test
    void update_duplicateNameFound_ThrowsExceptionFailedDbValidation() throws ExceptionFailedDbValidation {
        final SystemRequestDto requestDto = generateRequestDto(Generate.VALID);
        final System expected = repository.insert(requestDto);

        assertThrows(ExceptionFailedDbValidation.class, () -> repository.update(expected),
                "The " + entityName + " repository allowed an update of an object with a duplicate name when it shouldn't have.");
    }
}
