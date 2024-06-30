package com.sethhaskellcondie.thegamepensiveapi.domain.entity.system;

import com.sethhaskellcondie.thegamepensiveapi.domain.entity.EntityRepositoryTests;
import com.sethhaskellcondie.thegamepensiveapi.domain.filter.Filter;
import com.sethhaskellcondie.thegamepensiveapi.domain.exceptions.ExceptionFailedDbValidation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.sethhaskellcondie.thegamepensiveapi.domain.entity.EntityFactory.Generate.ANOTHER_VALID;
import static com.sethhaskellcondie.thegamepensiveapi.domain.entity.EntityFactory.Generate.VALID;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;

//Deprecated: This project is going to use full SpringBootTest Integration tests to test entity functionality
@Deprecated
public class SystemRepositoryTests extends EntityRepositoryTests<System, SystemRequestDto, SystemResponseDto> {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private String startsWith = "SuperConsole";

    @Override
    protected void setupRepositoryAndEntityName() {
        entityName = System.class.getSimpleName();
        repository = new SystemRepository(jdbcTemplate);
    }

    @Override
    protected void setupFactory() {
        factory = new SystemFactory(startsWith);
    }

    @Override
    protected Filter startsWithFilter() {
        return new Filter("system", "text", "name", Filter.OPERATOR_STARTS_WITH, startsWith, false);
    }

    @Override
    protected void validateReturnedObject(System expected, System actual) {
        assertAll(
            "These " + entityName + " objects are invalid.",
            () -> assertNotNull(actual.getId()),
            () -> assertEquals(expected.getName(), actual.getName()),
            () -> assertEquals(expected.getGeneration(), actual.getGeneration()),
            () -> assertEquals(expected.isHandheld(), actual.isHandheld()),
            () -> assertNotNull(actual.getCreatedAt()),
            () -> assertNotNull(actual.getUpdatedAt()),
            () -> assertNull(actual.getDeletedAt())
        );
    }

    @Override
    protected void validateReturnedObject(SystemRequestDto expected, System actual) {
        assertAll(
            "These " + entityName + " objects are invalid.",
            () -> assertNotNull(actual.getId()),
            () -> assertEquals(expected.name(), actual.getName()),
            () -> assertEquals(expected.generation(), actual.getGeneration()),
            () -> assertEquals(expected.handheld(), actual.isHandheld()),
            () -> assertNotNull(actual.getCreatedAt()),
            () -> assertNotNull(actual.getUpdatedAt()),
            () -> assertNull(actual.getDeletedAt())
        );
    }

    //End the tests for the default implementation, below are tests specific for that entity in this case System

    @Test
    void insert_duplicateNameFound_ThrowsExceptionFailedDbValidation() throws ExceptionFailedDbValidation {
        final SystemRequestDto requestDto = factory.generateRequestDto(VALID);
        final System expected = repository.insert(requestDto);

        assertThrows(ExceptionFailedDbValidation.class, () -> repository.insert(expected),
                "The " + entityName + " repository allowed an insert of an object with a duplicate name when it shouldn't have.");
    }

    @Test
    void update_duplicateNameFound_ThrowsExceptionFailedDbValidation() throws ExceptionFailedDbValidation {
        final SystemRequestDto requestDto = factory.generateRequestDto(VALID);
        final System existing = repository.insert(requestDto);
        final SystemRequestDto anotherRequestDto = factory.generateRequestDto(ANOTHER_VALID);
        final System alsoExisting = repository.insert(anotherRequestDto);
        final System systemToUpdateWithDuplicateName = new System(
            alsoExisting.getId(),
            existing.getName(), //use the name from existing
            alsoExisting.getGeneration(),
            alsoExisting.isHandheld(),
            alsoExisting.getCreatedAt(),
            alsoExisting.getUpdatedAt(),
            alsoExisting.getDeletedAt(),
            alsoExisting.getCustomFieldValues()
        );

        assertThrows(ExceptionFailedDbValidation.class, () -> repository.update(systemToUpdateWithDuplicateName),
                "The " + entityName + " repository allowed an update of an object with a duplicate name when it shouldn't have.");
    }
}