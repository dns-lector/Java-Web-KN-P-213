package itstep.learning.dal.dto.shop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class Category {
    private UUID   id;
    private String name;
    private String description;
    private String imageUrl;
    private String slug;
    private Date   deleteDt;

    public Category() {}

    public Category( ResultSet rs ) throws SQLException {
        this.setId( UUID.fromString( rs.getString( "category_id" ) ) );
        this.setName( rs.getString( "category_name" ) );
        this.setDescription( rs.getString( "category_description" ) );
        this.setImageUrl( rs.getString( "category_img_url" ) );
        this.setSlug( rs.getString( "category_slug" ) );
        Timestamp timestamp = rs.getTimestamp( "category_delete_dt" );
        if( timestamp != null ) {
            this.setDeleteDt( new Date( timestamp.getTime() ) );
        }
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getDeleteDt() {
        return deleteDt;
    }

    public void setDeleteDt(Date deleteDt) {
        this.deleteDt = deleteDt;
    }
}
