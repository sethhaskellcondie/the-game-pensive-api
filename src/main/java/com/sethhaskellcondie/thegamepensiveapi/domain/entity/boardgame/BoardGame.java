package com.sethhaskellcondie.thegamepensiveapi.domain.entity.boardgame;

import com.sethhaskellcondie.thegamepensiveapi.domain.Keychain;
import com.sethhaskellcondie.thegamepensiveapi.domain.customfield.CustomFieldValue;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.Entity;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.boardgamebox.BoardGameBox;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.boardgamebox.BoardGameBoxResponseDto;
import com.sethhaskellcondie.thegamepensiveapi.domain.exceptions.ExceptionMalformedEntity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BoardGame extends Entity<BoardGameRequestDto, BoardGameResponseDto> {

    private String title;
    private List<Integer> boardGameBoxIds; //The object is initially created with these ids, they can be validated and hydrated through the service.
    private List<BoardGameBox> boardGameBoxes;

    public BoardGame() {
        super();
        this.boardGameBoxIds = new ArrayList<>();
        this.boardGameBoxes = new ArrayList<>();
    }

    public BoardGame(Integer id, String title, Timestamp createdAt, Timestamp updatedAt, Timestamp deletedAt, List<CustomFieldValue> customFieldValues) {
        super(id, createdAt, updatedAt, deletedAt, customFieldValues);
        this.title = title;
        this.boardGameBoxIds = new ArrayList<>();
        this.boardGameBoxes = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public List<Integer> getBoardGameBoxIds() {
        return boardGameBoxIds;
    }

    public void setBoardGameBoxIds(List<Integer> boardGameBoxIds) {
        if (null == boardGameBoxIds) {
            this.boardGameBoxIds = new ArrayList<>();
            return;
        }
        this.boardGameBoxIds = boardGameBoxIds;
    }

    public List<BoardGameBox> getBoardGamesBoxes() {
        return boardGameBoxes;
    }

    public void setBoardGameBoxes(List<BoardGameBox> boardGameBoxes) {
        if (null == boardGameBoxes) {
            this.boardGameBoxes = new ArrayList<>();
            return;
        }
        this.boardGameBoxes = boardGameBoxes;
    }

    public boolean isBoardGameBoxesValid() {
        return !boardGameBoxes.isEmpty();
    }

    @Override
    protected BoardGame updateFromRequestDto(BoardGameRequestDto requestDto) {
        this.title = requestDto.title();
        this.setBoardGameBoxIds(requestDto.boardGameBoxIds());
        this.boardGameBoxes = new ArrayList<>();
        this.setCustomFieldValues(requestDto.customFieldValues());
        this.validate();
        return this;
    }

    @Override
    protected BoardGameRequestDto convertToRequestDto() {
        return new BoardGameRequestDto(this.title, this.boardGameBoxIds, this.customFieldValues);
    }

    @Override
    public BoardGameResponseDto convertToResponseDto() {
        List<BoardGameBoxResponseDto> boardGameBoxResponseDtos = new ArrayList<>();
        for (BoardGameBox boardGameBox : this.boardGameBoxes) {
            boardGameBoxResponseDtos.add(boardGameBox.convertToResponseDto());
        }
        return new BoardGameResponseDto(this.getKey(), this.id, this.title, boardGameBoxResponseDtos, this.created_at, this.updated_at, this.deleted_at, this.customFieldValues);
    }

    @Override
    public String getKey() {
        return Keychain.BOARD_GAME_KEY;
    }

    private void validate() throws ExceptionMalformedEntity {
        if (null == this.title || this.title.isBlank()) {
            throw new ExceptionMalformedEntity("Board Game object error, title cannot be blank");
        }
    }
}
