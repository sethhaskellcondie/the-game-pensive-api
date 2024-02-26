package com.sethhaskellcondie.thegamepensiveapi.domain.toy;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.sethhaskellcondie.thegamepensiveapi.domain.system.SystemRepositoryImpl;
import com.sethhaskellcondie.thegamepensiveapi.exceptions.ErrorLogs;
import com.sethhaskellcondie.thegamepensiveapi.exceptions.ExceptionFailedDbValidation;
import com.sethhaskellcondie.thegamepensiveapi.exceptions.ExceptionResourceNotFound;

@Repository
public class ToyRepositoryImpl implements ToyRepository {
	private final JdbcTemplate jdbcTemplate;
	private final String resourceName = "Toy";
	private final String baseQuery = "SELECT * FROM toys WHERE 1 = 1 ";
	private final RowMapper<Toy> rowMapper =
		(resultSet, i) ->
			new Toy(
				resultSet.getInt("id"),
				resultSet.getString("name"),
				resultSet.getString("set")
			);
	private final Logger logger = LoggerFactory.getLogger(SystemRepositoryImpl.class);

	public ToyRepositoryImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Toy insert(Toy toy) throws ExceptionFailedDbValidation {
		toyDbValidation(toy);
		String sql = """
   			INSERT INTO toys(name, set) VALUES (?, ?);
			""";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, toy.getName());
			ps.setString(2, toy.getSet());
			return ps;
		}, keyHolder);

		Integer generatedId = (Integer) keyHolder.getKey();
		try {
			return getById(generatedId);
		} catch (ExceptionResourceNotFound | NullPointerException e) {
			//we shouldn't ever reach this block because the database is managing the ID's
			logger.error(ErrorLogs.InsertThenRetrieveError(toy.getClass().getSimpleName(), generatedId));
			return null;
		}
	}

	@Override
	public List<Toy> getWithFilters(String filters) {
		String sql = baseQuery + filters + ";";
		return jdbcTemplate.query(sql, rowMapper);
	}

	@Override
	public Toy getById(int id) throws ExceptionResourceNotFound {
		String sql = baseQuery + " AND id = ? ;";
		Toy toy = jdbcTemplate.queryForObject(sql, rowMapper);
		if (toy == null || !toy.isPersistent()) {
			throw new ExceptionResourceNotFound(resourceName, id);
		}
		return toy;
	}

	@Override
	public Toy update(Toy toy) throws ExceptionFailedDbValidation {
		toyDbValidation(toy);
		String sql = """
   			UPDATE toys SET name = ?, set = ? WHERE id = ?;
			""";
		jdbcTemplate.update(
			sql,
			toy.getName(),
			toy.getSet(),
			toy.getId()
		);

		try {
			return getById(toy.getId());
		} catch (ExceptionResourceNotFound e) {
			//we shouldn't ever reach this block of code
			logger.error(ErrorLogs.UpdateThenRetrieveError(toy.getClass().getSimpleName(), toy.getId()));
			return null;
		}
	}

	@Override
	public void deleteById(int id) throws ExceptionResourceNotFound {
		String sql = """
   			DELETE FROM toys WHERE id = ?;
			""";
		int rowsUpdated = jdbcTemplate.update(sql, id);
		if (rowsUpdated < 1) {
			throw new ExceptionResourceNotFound("Delete failed", resourceName, id);
		}
	}

	private void toyDbValidation(Toy toy) throws ExceptionFailedDbValidation {
		//no validation needed for Toy table
	}
}