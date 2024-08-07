package com.sethhaskellcondie.thegamepensiveapi.domain.backupimport;

import com.sethhaskellcondie.thegamepensiveapi.domain.Keychain;
import com.sethhaskellcondie.thegamepensiveapi.domain.customfield.CustomField;
import com.sethhaskellcondie.thegamepensiveapi.domain.customfield.CustomFieldRepository;
import com.sethhaskellcondie.thegamepensiveapi.domain.customfield.CustomFieldRequestDto;
import com.sethhaskellcondie.thegamepensiveapi.domain.customfield.CustomFieldValue;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.boardgame.BoardGame;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.boardgame.BoardGameRequestDto;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.boardgame.BoardGameService;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.boardgamebox.BoardGameBox;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.boardgamebox.BoardGameBoxRequestDto;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.boardgamebox.BoardGameBoxService;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.system.System;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.system.SystemRequestDto;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.system.SystemService;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.toy.Toy;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.toy.ToyRequestDto;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.toy.ToyService;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.videogame.VideoGame;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.videogame.VideoGameRequestDto;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.videogame.VideoGameService;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.videogamebox.VideoGameBox;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.videogamebox.VideoGameBoxRequestDto;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.videogamebox.VideoGameBoxService;
import com.sethhaskellcondie.thegamepensiveapi.domain.exceptions.ExceptionBackupImport;
import com.sethhaskellcondie.thegamepensiveapi.domain.exceptions.ExceptionResourceNotFound;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class BackupImportService {
    private final CustomFieldRepository customFieldRepository;
    private final SystemService systemService;
    private final ToyService toyService;
    private final VideoGameService videoGameService;
    private final VideoGameBoxService videoGameBoxService;
    private final BoardGameService boardGameService;
    private final BoardGameBoxService boardGameBoxService;

    protected BackupImportService(CustomFieldRepository customFieldRepository, SystemService systemService, ToyService toyService, VideoGameService videoGameService,
                                  VideoGameBoxService videoGameBoxService, BoardGameService boardGameService, BoardGameBoxService boardGameBoxService) {
        this.customFieldRepository = customFieldRepository;
        this.systemService = systemService;
        this.toyService = toyService;
        this.videoGameService = videoGameService;
        this.videoGameBoxService = videoGameBoxService;
        this.boardGameService = boardGameService;
        this.boardGameBoxService = boardGameBoxService;
    }

    protected BackupDataDto getBackupData() {
        List<CustomField> customFields = customFieldRepository.getAllCustomFields();
        List<ToyRequestDto> toys = toyService.getWithFilters(new ArrayList<>()).stream().map(Toy::convertToRequestDto).toList();
        List<SystemRequestDto> systems = systemService.getWithFilters(new ArrayList<>()).stream().map(System::convertToRequestDto).toList();
        List<VideoGameRequestDto> videoGames = videoGameService.getWithFilters(new ArrayList<>()).stream().map(VideoGame::convertToRequestDto).toList();
        List<VideoGameBoxRequestDto> videoGameBoxes = videoGameBoxService.getWithFilters(new ArrayList<>()).stream().map(VideoGameBox::convertToRequestDto).toList();
        List<BoardGameRequestDto> boardGames = boardGameService.getWithFilters(new ArrayList<>()).stream().map(BoardGame::convertToRequestDto).toList();
        List<BoardGameBoxRequestDto> boardGameBoxes = boardGameBoxService.getWithFilters(new ArrayList<>()).stream().map(BoardGameBox::convertToRequestDto).toList();

        return new BackupDataDto(customFields, toys, systems, videoGames, videoGameBoxes, boardGames, boardGameBoxes);
    }

    protected ImportResultsDto importBackupData(BackupDataDto backupDataDto) {
        final Map<String, Integer> customFieldIds;
        final ImportCustomFieldsResults customFieldResults;
        customFieldResults = importCustomFields(backupDataDto);
        if (customFieldResults.exceptionBackupImport().getExceptions().size() > 0) {
            ExceptionBackupImport customFieldsException = new ExceptionBackupImport("There were errors importing Custom Fields. No additional data imported.");
            customFieldsException.appendExceptions(customFieldResults.exceptionBackupImport().getExceptions());
            return new ImportResultsDto(customFieldResults.existingCount(), customFieldResults.createdCount(), customFieldsException);
        }

        customFieldIds = customFieldResults.customFieldIds();
        ExceptionBackupImport exceptionBackupImport = new ExceptionBackupImport();

        ImportEntityResults toyResults = importToys(backupDataDto, customFieldIds);
        if (toyResults.exceptionBackupImport().getExceptions().size() > 0) {
            exceptionBackupImport.appendExceptions(toyResults.exceptionBackupImport().getExceptions());
        }

        ImportEntityResults systemResults = importSystems(backupDataDto, customFieldIds);
        if (systemResults.exceptionBackupImport().getExceptions().size() > 0) {
            exceptionBackupImport.appendExceptions(systemResults.exceptionBackupImport().getExceptions());
        }

        ImportEntityResults videoGameResults = importVideoGames(backupDataDto, customFieldIds);
        if (!videoGameResults.exceptionBackupImport().isEmpty()) {
            exceptionBackupImport.appendExceptions(videoGameResults.exceptionBackupImport().getExceptions());
        }

        if (exceptionBackupImport.getExceptions().size() > 0) {
            ExceptionBackupImport importException = new ExceptionBackupImport("There were errors importing Entity data, all valid data was imported, data with errors was skipped.");
            importException.appendExceptions(exceptionBackupImport.getExceptions());
            exceptionBackupImport = importException;
        }

        return new ImportResultsDto(
                customFieldResults.existingCount(), customFieldResults.createdCount(),
                toyResults.existingCount(), toyResults.createdCount(),
                systemResults.existingCount(), systemResults.createdCount(),
                videoGameResults.existingCount(), videoGameResults.createdCount(),
                exceptionBackupImport
        );
    }

    private ImportCustomFieldsResults importCustomFields(BackupDataDto backupDataDto) {
        final ExceptionBackupImport exceptionBackupImport = new ExceptionBackupImport();
        int existingCount = 0;
        int createdCount = 0;

        final List<CustomField> customFields = backupDataDto.customFields();
        final Map<String, Integer> customFieldIds = new HashMap<>(customFields.size());

        for (CustomField customField : customFields) {
            CustomField savedCustomField;
            try {
                savedCustomField = customFieldRepository.getByKeyAndName(customField.entityKey(), customField.name());
                if (!Objects.equals(savedCustomField.type(), customField.type())) {
                    exceptionBackupImport.addException(new Exception("Error Importing Custom Field Data: Provided custom field with name: '"
                            + customField.name() + "' and key: '" + customField.entityKey() + "' had a type mismatch with the existing custom field in the database. Provided type: '"
                            + customField.type() + "' existing (correct) type: '" + savedCustomField.type() + "'"));
                } else {
                    existingCount++;
                }
            } catch (ExceptionResourceNotFound ignored) {
                savedCustomField = null;
            }
            if (null == savedCustomField) {
                try {
                    savedCustomField = customFieldRepository.insertCustomField(new CustomFieldRequestDto(customField.name(), customField.type(), customField.entityKey()));
                    createdCount++;
                } catch (Exception exception) {
                    exceptionBackupImport.addException(new Exception("Error Importing Custom Field Data: Provided custom field with name: '"
                            + customField.name() + "' Message: " + exception.getMessage()));
                }
            }
            if (null != savedCustomField) {
                customFieldIds.put(customFieldComboKey(savedCustomField), savedCustomField.id());
            }
        }
        return new ImportCustomFieldsResults(customFieldIds, existingCount, createdCount, exceptionBackupImport);
    }

    private String customFieldComboKey(CustomField customField) {
        return customField.entityKey() + "-" + customField.name();
    }

    private String customFieldComboKey(String entityKey, CustomFieldValue value) {
        return entityKey + "-" + value.getCustomFieldName();
    }

    private ImportEntityResults importToys(BackupDataDto backupDataDto, Map<String, Integer> customFieldIds) {
        ExceptionBackupImport exceptionBackupImport = new ExceptionBackupImport();
        int existingCount = 0;
        int createdCount = 0;

        List<ToyRequestDto> toyRequestsToBeUpdated = backupDataDto.toys();
        if (null == toyRequestsToBeUpdated) {
            return new ImportEntityResults(existingCount, createdCount, exceptionBackupImport);
        }
        List<ToyRequestDto> toyRequestsReady = new ArrayList<>(toyRequestsToBeUpdated.size());
        for (ToyRequestDto toyRequestDto: toyRequestsToBeUpdated) {
            boolean skipped = false;
            for (CustomFieldValue value: toyRequestDto.customFieldValues()) {
                Integer customFieldId = customFieldIds.get(customFieldComboKey(Keychain.TOY_KEY, value));
                if (null == customFieldId) {
                    skipped = true;
                    exceptionBackupImport.addException(new Exception("Error Importing Toy Data: Imported Custom Field not found but expected for toy with name: '"
                        + toyRequestDto.name() + "' and set '" + toyRequestDto.set() + "' with custom field value named '" + value.getCustomFieldName()
                        + "' The custom field must be included on the import and not just existing in the database."));
                } else {
                    value.setCustomFieldId(customFieldId);
                }
            }
            if (!skipped) {
                toyRequestsReady.add(toyRequestDto);
            }
        }

        for (ToyRequestDto toyRequestDto: toyRequestsReady) {
            try {
                int toyId = toyService.getIdByNameAndSet(toyRequestDto.name(), toyRequestDto.set());
                if (toyId > 0) {
                    Toy toy = toyService.getById(toyId);
                    toy.updateFromRequestDto(toyRequestDto);
//                    toyService.updateExisting(toy);
                    existingCount++;
                } else {
                    toyService.createNew(toyRequestDto);
                    createdCount++;
                }
            } catch (Exception exception) {
                exceptionBackupImport.addException(new Exception("Error importing toy data with name: '" + toyRequestDto.name()
                        + "' and set '" + toyRequestDto.set() + "' " + exception.getMessage()));
            }
        }
        return new ImportEntityResults(existingCount, createdCount, exceptionBackupImport);
    }

    private ImportEntityResults importSystems(BackupDataDto backupDataDto, Map<String, Integer> customFieldIds) {
        ExceptionBackupImport exceptionBackupImport = new ExceptionBackupImport();
        int existingCount = 0;
        int createdCount = 0;

        List<SystemRequestDto> systemRequestToBeUpdated = backupDataDto.systems();
        if (null == systemRequestToBeUpdated) {
            return new ImportEntityResults(existingCount, createdCount, exceptionBackupImport);
        }
        List<SystemRequestDto> systemRequestsReady = new ArrayList<>(systemRequestToBeUpdated.size());
        for (SystemRequestDto systemRequestDto: systemRequestToBeUpdated) {
            boolean skipped = false;
            for (CustomFieldValue value: systemRequestDto.customFieldValues()) {
                Integer customFieldId = customFieldIds.get(customFieldComboKey(Keychain.SYSTEM_KEY, value));
                if (null == customFieldId) {
                    skipped = true;
                    exceptionBackupImport.addException(new Exception("Error importing system data: Imported Custom Field not found but expected for system named: '"
                        + systemRequestDto.name() + "' with custom field value named '" + value.getCustomFieldName()
                        + "' The custom field must be included on the import and not just existing in the database."));
                } else {
                    value.setCustomFieldId(customFieldId);
                }
            }
            if (!skipped) {
                systemRequestsReady.add(systemRequestDto);
            }
        }

        for (SystemRequestDto systemRequestDto: systemRequestsReady) {
            try {
                int systemId = systemService.getIdByName(systemRequestDto.name());
                if (systemId > 0) {
                    System system = systemService.getById(systemId);
                    system.updateFromRequestDto(systemRequestDto);
//                    systemService.updateExisting(system);
                    existingCount++;
                } else {
                    systemService.createNew(systemRequestDto);
                    createdCount++;
                }
            } catch (Exception exception) {
                exceptionBackupImport.addException(new Exception("Error importing system data with name: '" + systemRequestDto.name()
                        + "' " + exception.getMessage()));
            }
        }
        return new ImportEntityResults(existingCount, createdCount, exceptionBackupImport);
    }

    private ImportEntityResults importVideoGames(BackupDataDto backupDataDto, Map<String, Integer> customFieldIds) {
        ExceptionBackupImport exceptionBackupImport = new ExceptionBackupImport();
        int existingCount = 0;
        int createdCount = 0;

        List<VideoGameRequestDto> videoGameData = backupDataDto.videoGames();
        if (null == videoGameData) {
            return new ImportEntityResults(existingCount, createdCount, exceptionBackupImport);
        }
        List<VideoGameRequestDto> videoGamesToBeImported = new ArrayList<>(videoGameData.size());
        for (VideoGameRequestDto videoGameRequestDto: videoGameData) {
            boolean skipped = false;
            for (CustomFieldValue value: videoGameRequestDto.customFieldValues()) {
                Integer customFieldId = customFieldIds.get(customFieldComboKey(Keychain.VIDEO_GAME_KEY, value));
                if (null == customFieldId) {
                    skipped = true;
                    exceptionBackupImport.addException(new Exception("Error importing video game data: Imported Custom Field not found but expected for video game with title: '"
                            + videoGameRequestDto.title() + "' with custom field value named '" + value.getCustomFieldName()
                            + "' The custom field must be included on the import and not just existing in the database."));
                } else {
                    value.setCustomFieldId(customFieldId);
                }
            }
            if (!skipped) {
                videoGamesToBeImported.add(videoGameRequestDto);
            }
        }

        for (VideoGameRequestDto videoGameRequestDto: videoGamesToBeImported) {
            try {
                int videoGameId = videoGameService.getIdByTitleAndSystem(videoGameRequestDto.title(), videoGameRequestDto.systemId());
                if (videoGameId > 0) {
                    VideoGame videoGame = videoGameService.getById(videoGameId);
                    videoGame.updateFromRequestDto(videoGameRequestDto);
                    //call update and create from the service instead of the repository because that will include the system validation code
//                    videoGameService.updateExisting(videoGame);
                    existingCount++;
                } else {
                    videoGameService.createNew(videoGameRequestDto);
                    createdCount++;
                }
            } catch (Exception exception) {
                exceptionBackupImport.addException(new Exception("Error importing video game data with title: '" + videoGameRequestDto.title()
                        + "' " + exception.getMessage()));
            }
        }
        return new ImportEntityResults(existingCount, createdCount, exceptionBackupImport);
    }
}

record ImportCustomFieldsResults(Map<String, Integer> customFieldIds, int existingCount, int createdCount, ExceptionBackupImport exceptionBackupImport) { }
record ImportEntityResults(int existingCount, int createdCount, ExceptionBackupImport exceptionBackupImport) { }

