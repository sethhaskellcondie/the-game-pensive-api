package com.sethhaskellcondie.thegamepensiveapi.domain.entity.system;

import com.sethhaskellcondie.thegamepensiveapi.domain.entity.EntityRepository;
import com.sethhaskellcondie.thegamepensiveapi.domain.entity.EntityRepositoryAbstract;
import com.sethhaskellcondie.thegamepensiveapi.domain.Keychain;
import com.sethhaskellcondie.thegamepensiveapi.domain.filter.Filter;
import com.sethhaskellcondie.thegamepensiveapi.domain.exceptions.ExceptionFailedDbValidation;
import com.sethhaskellcondie.thegamepensiveapi.domain.exceptions.ExceptionInvalidFilter;
import com.sethhaskellcondie.thegamepensiveapi.domain.exceptions.ExceptionMalformedEntity;
import com.sethhaskellcondie.thegamepensiveapi.domain.exceptions.ExceptionResourceNotFound;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SystemRepository extends EntityRepositoryAbstract<System, SystemRequestDto, SystemResponseDto> implements EntityRepository<System, SystemRequestDto, SystemResponseDto> {

    public SystemRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    protected String getBaseQuery() {
        return """
                SELECT systems.id, systems.name, systems.generation, systems.handheld, systems.created_at, systems.updated_at, systems.deleted_at
                    FROM systems WHERE deleted_at IS NULL
            """;
    }

    protected String getBaseQueryJoinCustomFieldValues() {
        //in general the first row are the columns for the entity
        //the second and third row are the columns for the custom field values and the custom fields they should always be the same
        //then the remainder of the query is the same but fill in the property entity (systems)
        return """
                SELECT systems.id, systems.name, systems.generation, systems.handheld, systems.created_at, systems.updated_at, systems.deleted_at,
                        values.custom_field_id, values.entity_key, values.value_text, values.value_number,
                        fields.name as custom_field_name, fields.type as custom_field_type
                    FROM systems
                    JOIN custom_field_values as values ON systems.id = values.entity_id
                    JOIN custom_fields as fields ON values.custom_field_id = fields.id
                    WHERE systems.deleted_at IS NULL
                    AND values.entity_key = 'system'
            """;
    }

    protected String getBaseQueryWhereDeletedAtIsNotNull() {
        return """
                SELECT systems.id, systems.name, systems.generation, systems.handheld, systems.created_at, systems.updated_at, systems.deleted_at
                    FROM systems WHERE deleted_at IS NOT NULL
            """;
    }

    protected String getEntityKey() {
        return Keychain.SYSTEM_KEY;
    }

    protected RowMapper<System> getRowMapper() {
        return (resultSet, rowNumber) ->
                new System(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("generation"),
                        resultSet.getBoolean("handheld"),
                        resultSet.getTimestamp("created_at"),
                        resultSet.getTimestamp("updated_at"),
                        resultSet.getTimestamp("deleted_at"),
                        new ArrayList<>()
                );
    }

    protected void dbValidation(System system) throws ExceptionFailedDbValidation, ExceptionInvalidFilter {
        Filter nameFilter = new Filter("system", Filter.FIELD_TYPE_TEXT, "name", "equals", system.getName(), false);
        final List<System> existingSystems = getWithFilters(List.of(nameFilter));
        if (!existingSystems.isEmpty()) {
            throw new ExceptionFailedDbValidation("System insert/update failed, duplicate name found.");
        }
    }

    protected Integer insertImplementation(System system) {
        final String sql = """
                			INSERT INTO systems(name, generation, handheld, created_at, updated_at) VALUES (?, ?, ?, ?, ?);
                """;
        final KeyHolder keyHolder = new GeneratedKeyHolder();

        // This update call will take a preparedStatementCreator and a KeyHolder,
        // the preparedStatementCreator takes a connection, the connection can
        // include a Statement to hold the generated key and then put them in the
        // KeyHolder.
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, system.getName());
                    ps.setInt(2, system.getGeneration());
                    ps.setBoolean(3, system.isHandheld());
                    ps.setTimestamp(4, Timestamp.from(Instant.now()));
                    ps.setTimestamp(5, Timestamp.from(Instant.now()));
                    return ps;
                },
                keyHolder
        );
        return (Integer) keyHolder.getKeys().get("id");
    }

    protected void updateImplementation(System system) {
        final String sql = """
                			UPDATE systems SET name = ?, generation = ?, handheld = ?, updated_at = ? WHERE id = ?;
                """;
        jdbcTemplate.update(
                sql,
                system.getName(),
                system.getGeneration(),
                system.isHandheld(),
                Timestamp.from(Instant.now()),
                system.getId()
        );
    }

    @Override
    public System insert(SystemRequestDto requestDto) throws ExceptionMalformedEntity, ExceptionFailedDbValidation {
        final System system = new System().updateFromRequestDto(requestDto);
        return this.insert(system);
    }

    @Override
    public void deleteById(int id) throws ExceptionResourceNotFound {
        final String sql = """
                			UPDATE systems SET deleted_at = ? WHERE id = ?;
                """;
        int rowsUpdated = jdbcTemplate.update(sql, Timestamp.from(Instant.now()), id);
        if (rowsUpdated < 1) {
            throw new ExceptionResourceNotFound("Delete failed", System.class.getSimpleName(), id);
        }
    }
}