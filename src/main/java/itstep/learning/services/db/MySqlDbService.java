package itstep.learning.services.db;

import com.google.inject.Singleton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Singleton
public class MySqlDbService implements DbService {
    private Connection connection;

    @Override
    public Connection getConnection() throws SQLException {
        if( connection == null ) {
            // процес підключення: реєструємо драйвер СУБД (MySQL)
            DriverManager.registerDriver( new com.mysql.cj.jdbc.Driver() );
            // формуємо рядок підключення
            String connectionUrl = "jdbc:mysql://localhost:3308/java_kn_p_213" +
                    "?useUnicode=true&characterEncoding=utf8";
            String username = "user213";
            String password = "pass213";
            // одержуємо підключення
            connection = DriverManager.getConnection( connectionUrl, username, password );
        }
        return connection;
    }
}

/*
JDBC - Java DataBase Connectivity - технологія доступу до даних, аналогічна
  до ADO.NET
Надає узагальнений інтерфейс для роботи з різними джерелами даних (СУБД)
Для роботи з конкретною СУБД необхідно встановити конектор (драйвер)
  відповідної СУБД
 */
