package itstep.learning.dal.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import itstep.learning.services.db.DbService;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

@Singleton
public class ProductDao {
    private final DbService dbService;
    private final Logger logger;

    @Inject
    public ProductDao(@Named("Oracle") DbService dbService, Logger logger) {
        this.dbService = dbService;
        this.logger = logger;
    }

    public boolean install() {
        String sql = "CREATE TABLE products (" +
                "product_id          CHAR(36)        PRIMARY KEY ," +
                "product_name        NVARCHAR2(64)   NOT NULL," +
                "product_description NVARCHAR2(1024)     NULL," +
                "product_price       NUMBER(8,2)     NOT NULL," +
                "product_img_url     VARCHAR(256)        NULL," +
                "product_cnt         INT             DEFAULT ON NULL 1," +
                "product_delete_dt   DATE                NULL" +
                ") ";

        try( Statement stmt = dbService.getConnection().createStatement() ) {
            stmt.executeUpdate( sql ) ;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }

        sql = "CREATE TABLE products_images (" +
                "product_id      CHAR(36)," +
                "product_img_url VARCHAR(256) NOT NULL," +
                "PRIMARY KEY(product_id, product_img_url)" +
                ") ";

        try( Statement stmt = dbService.getConnection().createStatement() ) {
            stmt.executeUpdate( sql ) ;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }


        return true;
    }
}
/*
CREATE TABLE products (
product_id          CHAR(36)        PRIMARY KEY ,
product_name        NVARCHAR2(64)   NOT NULL,
product_description NVARCHAR2(1024)     NULL,
product_price       NUMBER(8,2)     NOT NULL,
product_img_url     VARCHAR(256)        NULL,
product_cnt         INT             NOT NULL  DEFAULT 1,
product_delete_dt   DATE                NULL
)
 */