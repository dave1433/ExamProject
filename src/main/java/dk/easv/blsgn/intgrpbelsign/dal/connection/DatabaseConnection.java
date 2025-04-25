package dk.easv.blsgn.intgrpbelsign.dal.connection;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import java.sql.Connection;

public class DatabaseConnection {
    public Connection getConnection() throws SQLServerException {
        SQLServerDataSource ds;
        ds = new SQLServerDataSource();
        ds.setDatabaseName("ExamProject-DALB"); // make this unique as names are shared on server
        ds.setUser("CSe2024b_e_18"); // Use your own username
        ds.setPassword("CSe2024bE18!24"); // Use your own password
        ds.setServerName("EASV-DB4");
        ds.setPortNumber(1433);
        ds.setTrustServerCertificate(true);
        return ds.getConnection();

    }

}
