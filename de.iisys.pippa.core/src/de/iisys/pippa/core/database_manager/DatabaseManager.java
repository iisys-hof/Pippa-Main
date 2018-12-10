package de.iisys.pippa.core.database_manager;

import java.sql.Connection;
import java.sql.SQLException;

import de.iisys.pippa.core.skill.Skill;

public interface DatabaseManager {

	 public Connection createConnection(Skill skillRef) throws SQLException, Exception;
	
}
