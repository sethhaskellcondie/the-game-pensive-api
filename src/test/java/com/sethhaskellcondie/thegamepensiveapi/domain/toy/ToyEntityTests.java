package com.sethhaskellcondie.thegamepensiveapi.domain.toy;

import com.sethhaskellcondie.thegamepensiveapi.exceptions.ExceptionMalformedEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ToyEntityTests {

    @Test
    public void updateFromRequestDto_ValidDto_ToyUpdated() {
        final String name = "name";
        final String set = "set";

        final ToyRequestDto requestDto = new ToyRequestDto(name, set);
        final Toy toy = new Toy();

        toy.updateFromRequestDto(requestDto);

        assertEquals(name, toy.getName());
        assertEquals(set, toy.getSet());
    }

    @Test
    public void updateFromRequestDto_FieldsNull_ThrowExceptionMalformedEntity() {
        final int numberOfErrors = 1;

        final ToyRequestDto requestDto = new ToyRequestDto(null, null);
        final Toy toy = new Toy();

        try {
            toy.updateFromRequestDto(requestDto);
        } catch (ExceptionMalformedEntity e) {
            assertEquals(numberOfErrors, e.getMessages().size());
        }
    }

    @Test
    public void updateFromRequestDto_FieldsIncorrect_ThrowExceptionMalformedEntity() {
        final String name = ""; //name cannot be blank
        final String set = null;
        final int numberOfErrors = 1;

        final ToyRequestDto requestDto = new ToyRequestDto(name, set);
        final Toy toy = new Toy();

        try {
            toy.updateFromRequestDto(requestDto);
        } catch (ExceptionMalformedEntity e) {
            assertEquals(numberOfErrors, e.getMessages().size());
        }
    }

    @Test
    public void convertToResponseDto_HappyPath_DtoCreated() {
        final int id = 99;
        final String name = "Super Mario";
        final String set = "Amiibo";

        final Toy toy = new Toy(id, name, set);

        final ToyResponseDto responseDto = toy.convertToResponseDto();

        assertEquals("toy", responseDto.type());
        assertEquals(id, responseDto.id());
        assertEquals(name, responseDto.name());
    }
}
