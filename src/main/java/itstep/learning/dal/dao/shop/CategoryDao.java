package itstep.learning.dal.dao.shop;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import itstep.learning.dal.dto.shop.Category;
import itstep.learning.services.db.DbService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class CategoryDao {
    private final DbService dbService;
    private final Logger logger;

    @Inject
    public CategoryDao(@Named("Oracle") DbService dbService, Logger logger) {
        this.dbService = dbService;
        this.logger = logger;
    }

    public List<Category> read() {
        List<Category> categories = new ArrayList<>();

        String sql = "SELECT * FROM categories";
        try( Statement stmt = dbService.getConnection().createStatement() ) {
            ResultSet rs = stmt.executeQuery( sql );
            while ( rs.next() ) {
                categories.add( new Category( rs ) ) ;
            }
            rs.close();
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
        }

        return categories;
    }

    public Category create( Category category ) {
        if( category == null ) {
            return null;
        }
        category.setId( UUID.randomUUID() );
        String sql = "INSERT INTO categories " +
            "(category_id,category_name,category_description,category_img_url,category_delete_dt )" +
                " VALUES (?, ?, ?, ?, ?)";
        try( PreparedStatement prep = dbService.getConnection().prepareStatement(sql) ) {
            prep.setString( 1, category.getId().toString() );
            prep.setString( 2, category.getName() );
            prep.setString( 3, category.getDescription() );
            prep.setString( 4, category.getImageUrl() );
            if( category.getDeleteDt() != null ) {
                prep.setTimestamp( 5, new Timestamp( category.getDeleteDt().getTime() ) );
            }
            else {
                prep.setTimestamp( 5, null );
            }
            prep.executeUpdate();
            return category;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return null;
        }
    }
}
