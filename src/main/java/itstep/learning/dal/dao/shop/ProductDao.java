package itstep.learning.dal.dao.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import itstep.learning.dal.dto.shop.Category;
import itstep.learning.dal.dto.shop.Product;
import itstep.learning.services.db.DbService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    public boolean isSlugFree( String slug ) {
        String sql = "SELECT COUNT(*) FROM products p WHERE p.product_slug = ?";
        try( PreparedStatement prep = dbService.getConnection().prepareStatement(sql) ) {
            prep.setString( 1, slug );
            ResultSet rs = prep.executeQuery();
            if( rs.next() ) {
                return rs.getInt(1) == 0;
            }
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
        }
        return false;
    }

    public Product create( Product product ) {
        if( product == null ) {
            return null;
        }
        product.setId( UUID.randomUUID() );
        String sql = "INSERT INTO products (product_id, category_id,product_name,product_description," +
                "product_slug,product_img_url,product_price,product_cnt,product_delete_dt )" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try( PreparedStatement prep = dbService.getConnection().prepareStatement(sql) ) {
            prep.setString( 1, product.getId().toString() );
            prep.setString( 2, product.getCategoryId().toString() );
            prep.setString( 3, product.getName() );
            prep.setString( 4, product.getDescription() );
            prep.setString( 5, product.getSlug() );
            prep.setString( 6, product.getImageUrl() );
            prep.setDouble( 7, product.getPrice() );
            prep.setInt( 8, product.getQuantity() );
            if( product.getDeleteDt() != null ) {
                prep.setTimestamp( 9, new Timestamp( product.getDeleteDt().getTime() ) );
            }
            else {
                prep.setTimestamp( 9, null );
            }
            prep.executeUpdate();
            return product;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return null;
        }
    }

    public List<Product> read( String categoryIdOrSlug ) {
        return read( categoryIdOrSlug, false );
    }

    public List<Product> read( String categoryIdOrSlug, boolean withDeleted ) {
        List<Product> products = new ArrayList<>();
        if( categoryIdOrSlug == null ) {
            return products;
        }
        String categoryId = null;
        String sql;
        try {
            UUID.fromString( categoryIdOrSlug );
            sql = "SELECT category_id FROM categories WHERE category_id = ? ";
        }
        catch( Exception ignored ) {
            sql = "SELECT category_id FROM categories WHERE category_slug = ? ";
        }
        try( PreparedStatement prep = dbService.getConnection().prepareStatement(sql) ) {
            prep.setString( 1, categoryIdOrSlug );
            ResultSet rs = prep.executeQuery();
            if( rs.next() ) {
                categoryId = rs.getString( 1 );
            }
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
        }
        if( categoryId == null ) {
            return products;
        }

        sql = "SELECT * FROM products WHERE category_id = ? ";
        if( ! withDeleted ) {
            sql += " AND product_delete_dt IS NULL";
        }
        try( PreparedStatement prep = dbService.getConnection().prepareStatement(sql) ) {
            prep.setString( 1, categoryId );
            ResultSet rs = prep.executeQuery();
            while ( rs.next() ) {
                products.add( new Product( rs ) ) ;
            }
            rs.close();
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
        }
        return products;
    }

    public boolean install() {

        String sql = "CREATE TABLE categories (" +
                "category_id          CHAR(36)        PRIMARY KEY ," +
                "category_name        NVARCHAR2(64)   NOT NULL," +
                "category_description NVARCHAR2(1024) NOT NULL," +
                "category_img_url     VARCHAR(256)    NOT NULL," +
                "category_delete_dt   DATE                NULL," +
                "category_slug        VARCHAR(128)        NULL," +
                "UNIQUE(category_slug)" +
            ") ";

        try( Statement stmt = dbService.getConnection().createStatement() ) {
            stmt.executeUpdate( sql ) ;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }

        sql = "CREATE TABLE products (" +
                "product_id          CHAR(36)        PRIMARY KEY ," +
                "category_id         CHAR(36)        NOT NULL," +
                "product_name        NVARCHAR2(64)   NOT NULL," +
                "product_description NVARCHAR2(1024)     NULL," +
                "product_price       NUMBER(8,2)     NOT NULL," +
                "product_img_url     VARCHAR(256)        NULL," +
                "product_cnt         INT             DEFAULT ON NULL 1," +
                "product_delete_dt   DATE                NULL," +
                "product_slug        VARCHAR(256)        NULL," +
                "UNIQUE(product_slug)" +
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