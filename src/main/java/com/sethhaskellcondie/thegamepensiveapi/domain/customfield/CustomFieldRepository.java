package com.sethhaskellcondie.thegamepensiveapi.domain.customfield;

import com.sethhaskellcondie.thegamepensiveapi.domain.Keychain;
import com.sethhaskellcondie.thegamepensiveapi.exceptions.ErrorLogs;
import com.sethhaskellcondie.thegamepensiveapi.exceptions.ExceptionFailedDbValidation;
import com.sethhaskellcondie.thegamepensiveapi.exceptions.ExceptionInternalCatastrophe;
import com.sethhaskellcondie.thegamepensiveapi.exceptions.ExceptionResourceNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

@Repository
public class CustomFieldRepository {
    private final JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(CustomFieldRepository.class);
    private final RowMapper<CustomField> rowMapper = (resultSet, rowNumber) ->
            new CustomField(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("type"),
                    resultSet.getString("entity_key")
            );

    public CustomFieldRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public CustomField insertCustomField(String name, String type, String key) throws ExceptionFailedDbValidation {
        return insertCustomField(new CustomFieldRequestDto(name, type, key));
    }

    public CustomField insertCustomField(CustomFieldRequestDto customField) throws ExceptionFailedDbValidation {
        customFieldDbValidation(customField);
        final String sql = """
                			INSERT INTO custom_fields(name, type, entity_key) VALUES (?, ?, ?);
                """;
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, customField.name());
                    ps.setString(2, customField.type());
                    ps.setString(3, customField.entityKey());
                    return ps;
                },
                keyHolder
        );
        final Integer generatedId = (Integer) keyHolder.getKeys().get("id");

        try {
            return getById(generatedId);
        } catch (ExceptionResourceNotFound | NullPointerException e) {
            logger.error(ErrorLogs.InsertThenRetrieveError("custom_fields", generatedId));
            throw new ExceptionInternalCatastrophe("custom_fields", generatedId);
        }
    }

    public CustomField getById(int id) throws ExceptionResourceNotFound {
        final String sql = "SELECT * FROM custom_fields WHERE id = ? AND deleted = false";
        CustomField customField;
        try {
            customField = jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{id}, //args to bind to the sql ?
                    new int[]{Types.BIGINT}, //the types of the objects to bind to the sql
                    rowMapper
            );
        } catch (EmptyResultDataAccessException exception) {
            throw new ExceptionResourceNotFound("custom_fields", id);
        }
        return customField;
    }

    public CustomField getByIdIncludeDeleted(int id) throws ExceptionResourceNotFound {
        final String sql = "SELECT * FROM custom_fields WHERE id = ?";
        CustomField customField;
        try {
            customField = jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{id}, //args to bind to the sql ?
                    new int[]{Types.BIGINT}, //the types of the objects to bind to the sql
                    rowMapper
            );
        } catch (EmptyResultDataAccessException exception) {
            throw new ExceptionResourceNotFound("custom_fields (include deleted)", id);
        }
        return customField;
    }

    public List<CustomField> getAllCustomFields() {
        final String sql = "SELECT * FROM custom_fields";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<CustomField> getAllByKey(String entityKey) {
        final String sql = "SELECT * FROM custom_fields WHERE entity_key = ? ;";
        return jdbcTemplate.query(sql, rowMapper, entityKey);
    }

    public CustomField updateName(int id, String newName) throws ExceptionResourceNotFound {
        final String sql = """
                			UPDATE custom_fields SET name = ? WHERE id = ?;
                """;
        jdbcTemplate.update(sql, newName, id);
        return getById(id);
    }

    public void deleteById(int id) throws ExceptionResourceNotFound {
        final String sql = """
                			UPDATE custom_fields SET deleted = true WHERE id = ?;
                """;
        int rowsUpdated = jdbcTemplate.update(sql, id);
        if (rowsUpdated < 1) {
            throw new ExceptionResourceNotFound("Delete failed", "custom_fields", id);
        }
    }

    private void customFieldDbValidation(CustomFieldRequestDto customField) throws ExceptionFailedDbValidation {
        ExceptionFailedDbValidation exception = new ExceptionFailedDbValidation();
        if (!CustomField.getAllCustomFieldTypes().contains(customField.type())) {
            exception.addException("Custom Field Type: " + customField.type() + " is not a valid type. " +
                    "Valid types include [" + String.join(", ", CustomField.getAllCustomFieldTypes()) + "]");
        }
        if (!Keychain.getAllKeys().contains(customField.entityKey())) {
            exception.addException("Custom Field Entity Key: " + customField.entityKey() + " is not a valid entity key. " +
                    "Valid keys include [" + String.join(", ", Keychain.getAllKeys()) + "]");
        }
        if (!exception.getExceptions().isEmpty()) {
            throw exception;
        }
    }
}
