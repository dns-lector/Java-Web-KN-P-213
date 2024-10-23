package itstep.learning.dal.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dto.Token;
import itstep.learning.dal.dto.User;
import itstep.learning.models.SignupFormModel;
import itstep.learning.services.db.DbService;
import itstep.learning.services.kdf.KdfService;

import java.sql.*;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

@Singleton
public class AuthDao {
    private final DbService dbService;
    private final Logger logger;
    private final KdfService kdfService;

    @Inject
    public AuthDao(DbService dbService, Logger logger, KdfService kdfService) {
        this.dbService = dbService;
        this.logger = logger;
        this.kdfService = kdfService;
    }

    public User getUserByToken( String token ) {
        UUID tokenId;
        try{ tokenId = UUID.fromString(token); }
        catch( Exception ignored ) { return null; }
        String sql = "SELECT * FROM users_access a " +
                " JOIN users u ON a.user_id = u.user_id " +
                " JOIN users_roles r ON a.role_id = r.role_id " +
                " JOIN tokens t ON u.user_id = t.user_id AND t.exp > CURRENT_TIMESTAMP" +
                " WHERE t.token_id = ?";
        try( PreparedStatement prep = dbService.getConnection().prepareStatement(sql) ) {
            prep.setString( 1, tokenId.toString() );
            ResultSet rs = prep.executeQuery();
            if( rs.next() ) {
                return new User( rs );
            }
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
        }
        return null;
    }

    public User signUp( SignupFormModel model ) {
        if( model == null ) {
            return null;
        }
        User user = new User();
        user.setUserId( UUID.randomUUID() );
        user.setUserName( model.getName() );
        user.setEmail( model.getEmail() );
        user.setPhone( model.getPhone() );
        user.setBirthdate( model.getBirthdate() );
        user.setAvatarUrl( model.getAvatar() );
        String sql = "INSERT INTO `users` " +
                "(`user_id`, `user_name`, `email`, `phone`, `avatar_url`, `birthdate`) " +
                "VALUES(?,?,?,?,?,?)";
        try( PreparedStatement prep = dbService.getConnection().prepareStatement( sql ) ) {
            prep.setString( 1, user.getUserId().toString() );
            prep.setString( 2, user.getUserName() );
            prep.setString( 3, user.getEmail() );
            prep.setString( 4, user.getPhone() );
            prep.setString( 5, user.getAvatarUrl() );
            prep.setTimestamp( 6, new Timestamp( user.getBirthdate().getTime() ) );
            prep.executeUpdate();
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return null;
        }


        String salt = UUID.randomUUID().toString().substring(0, 16);
        String password = model.getPassword();
        String dk = kdfService.dk( password, salt );
        sql = "INSERT INTO `users_access`(`user_id`, `login`, `salt`, `dk`) " +
                "VALUES (?, ?, ?, ?) ";
        try( PreparedStatement prep = dbService.getConnection().prepareStatement( sql ) ) {
            prep.setString( 1, user.getUserId().toString() );
            prep.setString( 2, model.getLogin() );
            prep.setString( 3, salt );
            prep.setString( 4, dk );
            prep.executeUpdate();
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            // TODO: Delete user
            return null;
        }

        return user;
    }

    public User authenticate( String login, String password ) {
        String sql = "SELECT * FROM users_access a " +
                " JOIN users u ON a.user_id = u.user_id " +
                " JOIN users_roles r ON a.role_id = r.role_id " +
                " LEFT JOIN tokens t ON u.user_id = t.user_id AND t.exp > CURRENT_TIMESTAMP " +
                " WHERE a.login = ? ";
        try( PreparedStatement prep = dbService.getConnection().prepareStatement( sql ) ) {
            prep.setString( 1, login );
            ResultSet rs = prep.executeQuery();
            if( rs.next() ) {  // є такий login
                String salt = rs.getString( "salt" );
                String dk   = rs.getString( "dk"   );
                // повторюємо процедуру DK і перевіряємо чи збігаються результати перетворень
                if( kdfService.dk( password, salt ).equals( dk ) ) {
                    User user = new User( rs );
                    Token token ;
                    try {
                        token = new Token( rs );   // перевіряємо чи є в користувача активний токен
                    }
                    catch( SQLException ignored ) {
                        token = null;
                    }

                    if( token == null ) {  // якщо немає активного, то створюємо новий токен для користувача
                        token = this.createToken( user );
                    }
                    user.setToken( token );
                    return user;
                }
            }
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
        }
        return null;
    }

    public Token createToken( User user ) {
        Token token = new Token();
        token.setTokenId( UUID.randomUUID() );
        token.setUserId( user.getUserId() );
        token.setIat( new Date( System.currentTimeMillis() ) );
        token.setExp( new Date( System.currentTimeMillis() + 86400000 ) );

        String sql = "INSERT INTO tokens ( token_id, user_id, iat, exp ) " +
                " VALUES ( ?, ?, ?, ? )";
        try( PreparedStatement prep = dbService.getConnection().prepareStatement( sql ) ) {
            prep.setString( 1, token.getTokenId().toString() );
            prep.setString( 2, token.getUserId().toString() );
            prep.setTimestamp( 3, new Timestamp( token.getIat().getTime() ) );
            prep.setTimestamp( 4, new Timestamp( token.getExp().getTime() ) );
            prep.executeUpdate();
            return token;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
        }
        return null;
    }

    public boolean install() {
        String sql = "CREATE TABLE  IF NOT EXISTS `users` (" +
                "`user_id`     CHAR(36)     PRIMARY KEY  DEFAULT( UUID() )," +
                "`user_name`   VARCHAR(64)  NOT NULL," +
                "`email`       VARCHAR(128) NOT NULL," +
                "`phone`       VARCHAR(16)      NULL," +
                "`avatar_url`  VARCHAR(256)     NULL," +
                "`birthdate`   DATETIME     NOT NULL," +
                "`delete_dt`   DATETIME         NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try( Statement stmt = dbService.getConnection().createStatement() ) {
            stmt.executeUpdate( sql ) ;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }

        sql = "CREATE TABLE  IF NOT EXISTS `users_access` (" +
                "`access_id` CHAR(36)     PRIMARY KEY  DEFAULT( UUID() )," +
                "`user_id`   CHAR(36)     NOT NULL," +
                "`login`     VARCHAR(32)  NOT NULL," +
                "`salt`      CHAR(16)         NULL," +
                "`dk`        CHAR(20)         NULL," +
                "`role_id`   CHAR(36)     NOT NULL DEFAULT 'acffa6f6-89f9-11ef-a6bd-6f31a5ab6a0f'," +
                "`is_active` TINYINT      DEFAULT 1" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try( Statement stmt = dbService.getConnection().createStatement() ) {
            stmt.executeUpdate( sql ) ;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }

        sql = "CREATE TABLE  IF NOT EXISTS `users_roles` (" +
                "`role_id`    CHAR(36)     PRIMARY KEY  DEFAULT( UUID() )," +
                "`role_name`  VARCHAR(36)  NOT NULL," +
                "`can_create` TINYINT      DEFAULT 0," +
                "`can_read`   TINYINT      DEFAULT 1," +
                "`can_update` TINYINT      DEFAULT 0," +
                "`can_delete` TINYINT      DEFAULT 0" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try( Statement stmt = dbService.getConnection().createStatement() ) {
            stmt.executeUpdate( sql ) ;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }

        sql = "CREATE TABLE  IF NOT EXISTS `tokens` (" +
                "`token_id` CHAR(36)   PRIMARY KEY  DEFAULT( UUID() )," +
                "`user_id`  CHAR(36)   NOT NULL," +
                "`iat`      DATETIME   DEFAULT CURRENT_TIMESTAMP," +
                "`exp`      DATETIME   NOT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try( Statement stmt = dbService.getConnection().createStatement() ) {
            stmt.executeUpdate( sql ) ;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }

        // Seed - сідування: впровадження початкових даних / повернення "заводських" налаштувань
        sql = "INSERT INTO `users_roles`(`role_id`,`role_name`,`can_create`,`can_read`,`can_update`,`can_delete`) " +
                "VALUES ('81661d9f-815d-11ef-bb48-fcfbf6dd7098','Administrator',1,1,1,1) " +
                "ON DUPLICATE KEY UPDATE " +
                "`role_name` = 'Administrator', " +
                "`can_create` = 1, `can_read` = 1,`can_update` = 1,`can_delete` = 1";
        try( Statement stmt = dbService.getConnection().createStatement() ) {
            stmt.executeUpdate( sql ) ;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }
        sql = "INSERT INTO `users_roles`(`role_id`,`role_name`,`can_create`,`can_read`,`can_update`,`can_delete`) " +
                "VALUES ('acffa6f6-89f9-11ef-a6bd-6f31a5ab6a0f','Guest',0,1,0,0) " +
                "ON DUPLICATE KEY UPDATE " +
                "`role_name` = 'Guest', " +
                "`can_create` = 0, `can_read` = 1,`can_update` = 0,`can_delete` = 0";
        try( Statement stmt = dbService.getConnection().createStatement() ) {
            stmt.executeUpdate( sql ) ;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }


        sql = "INSERT INTO `users`(`user_id`,`user_name`,`email`,`birthdate`,`delete_dt`) " +
                "VALUES ('7dd7d8a9-815e-11ef-bb48-fcfbf6dd7098','Administrator','admin@change.me','1970-01-01',NULL) " +
                "ON DUPLICATE KEY UPDATE " +
                "`user_name` = 'Administrator', " +
                "`email` = 'admin@change.me', " +
                "`birthdate` = '1970-01-01'," +
                "`delete_dt` = NULL";
        try( Statement stmt = dbService.getConnection().createStatement() ) {
            stmt.executeUpdate( sql ) ;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }
        String salt = "bb48fc1bf6dd70.4";
        String password = "root";
        String dk = kdfService.dk( password, salt );

        sql = "INSERT INTO `users_access`(`access_id`,`user_id`,`role_id`,`login`,`salt`,`dk`,`is_active`) " +
                "VALUES ('5392cdda-815f-11ef-bb48-fcfbf6dd7098'," +
                "'7dd7d8a9-815e-11ef-bb48-fcfbf6dd7098', " +
                "'81661d9f-815d-11ef-bb48-fcfbf6dd7098'," +
                "'admin', ?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE " +
                "`user_id` = '7dd7d8a9-815e-11ef-bb48-fcfbf6dd7098', " +
                "`role_id` = '81661d9f-815d-11ef-bb48-fcfbf6dd7098', " +
                "`login` = 'admin'," +
                "`salt` = ?," +
                "`dk` = ?," +
                "`is_active` = 1";
        try( PreparedStatement prep = dbService.getConnection().prepareStatement( sql ) ) {
            prep.setString( 1, salt );   // JDBC - відлік параметрів з 1
            prep.setString( 2, dk );
            prep.setString( 3, salt );
            prep.setString( 4, dk );
            prep.executeUpdate();
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }

        return true;
    }
}
/*
DAO - Data Access Object
набір інструментів (бізнес-логіка) для роботи з
DTO - Data Transfer Object (Entities) - моделями
передачі даних

Задачі авторизації / автентифікації
[users]       [users_access]     [users_roles]     [tokens]
|user_id      |access_id         |role_id          |token_id
|user_name    |user_id           |role_name        |user_id
|email        |login             |can_create       |iat
|phone        |salt              |can_read         |exp
|avatar_url   |dk                |can_update       |
|birthdate    |role_id           |can_delete       |
|delete_dt    |is_active

[users_details]
|user_id
|tg_url
|fb_url
|work_email
|work_phone
|work_address
|home_address

Д.З. Реалізувати DAO для ведення журналу доступу до сайту:
- хто заходив
- коли заходив
- на яку сторінку
Розробити структуру таблиці (таблиць), створити метод
install() який створить таблиці у БД.
На домашній сторінці переконатись у дієздатності методу.
 */
