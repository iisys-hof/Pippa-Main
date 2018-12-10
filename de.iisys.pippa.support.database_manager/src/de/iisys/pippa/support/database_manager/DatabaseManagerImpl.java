package de.iisys.pippa.support.database_manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.Driver;

import de.iisys.pippa.core.database_manager.DatabaseManager;
import de.iisys.pippa.core.skill.Skill;

public class DatabaseManagerImpl implements DatabaseManager{

	private final String DATABASE_URL_PREFIX = "jdbc:h2:~/";
	private final String SERVER_USER = "sa";
	private final String SERVER_PASSWORD = "";

	  private static DatabaseManagerImpl instance;

	  private DatabaseManagerImpl () {}

	  public static synchronized DatabaseManagerImpl getInstance () {
	    if (DatabaseManagerImpl.instance == null) {
	    	DatabaseManagerImpl.instance = new DatabaseManagerImpl ();
	    }
	    return DatabaseManagerImpl.instance;
	  }
	  
	  public synchronized Connection createConnection(Skill skillRef) throws SQLException, Exception {

			if (skillRef == null) {
				throw new NullPointerException("No Reference to a Skill was given when creating a Database Connection.");
			}

			if (!(skillRef instanceof Skill)) {
				throw new IllegalArgumentException("Given Reference not of Type ISkill when creating a Database Connection.");
			}

			@SuppressWarnings("unused")
			Driver driver = Driver.load();

			// set database url from url-prefix and qualified classname
			String databaseUrl = this.DATABASE_URL_PREFIX + skillRef.getClass().getName();

			// create connection with url
			Connection connection = DriverManager.getConnection(databaseUrl, this.SERVER_USER,
					this.SERVER_PASSWORD);

			return connection;

			// By default, if the database specified in the URL does not yet exist, a new
			// (empty) database is created automatically.
		}	  
	  
	}
