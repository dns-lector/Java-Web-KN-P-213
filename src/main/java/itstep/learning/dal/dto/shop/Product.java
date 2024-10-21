package itstep.learning.dal.dto.shop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Product {
    private UUID   id;
    private UUID   categoryId;
    private String name;
    private String description;
    private String imageUrl;
    private String slug;
    private Date   deleteDt;
    private double price;
    private int    quantity;

    private Category category;
    private List<String> imageUrls;
    private List<Product> similarProducts;

    public Product() {
    }

    public Product( ResultSet rs ) throws SQLException {
        this.setId( UUID.fromString( rs.getString( "product_id" ) ) );
        this.setCategoryId( UUID.fromString( rs.getString( "category_id" ) ) );
        this.setName( rs.getString( "product_name" ) );
        this.setDescription( rs.getString( "product_description" ) );
        this.setImageUrl( rs.getString( "product_img_url" ) );
        this.setSlug( rs.getString( "product_slug" ) );
        this.setPrice( rs.getDouble( "product_price" ) );
        this.setQuantity( rs.getInt( "product_cnt" ) );
        Timestamp timestamp = rs.getTimestamp( "product_delete_dt" );
        if( timestamp != null ) {
            this.setDeleteDt( new Date( timestamp.getTime() ) );
        }
        try { this.setCategory( new Category( rs ) ) ; }
        catch( Exception ignored ) { }
    }

    public UUID getId() {

        return id;
    }

    public List<Product> getSimilarProducts() {
        return similarProducts;
    }

    public void setSimilarProducts(List<Product> similarProducts) {
        this.similarProducts = similarProducts;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Date getDeleteDt() {
        return deleteDt;
    }

    public void setDeleteDt(Date deleteDt) {
        this.deleteDt = deleteDt;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
