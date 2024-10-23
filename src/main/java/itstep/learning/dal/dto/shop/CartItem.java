package itstep.learning.dal.dto.shop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CartItem {
    private UUID cartId;
    private UUID productId;
    private int quantity;
    private double price;

    private Cart cart;
    private Product product;

    public CartItem() {}

    public CartItem( ResultSet rs ) throws SQLException {
        this.setCartId( UUID.fromString( rs.getString("cart_id") ) );
        this.setProductId( UUID.fromString( rs.getString("product_id") ) );
        this.setQuantity( rs.getInt("cart_item_quantity") );
        this.setPrice( rs.getDouble("cart_item_price") );

        try { this.setCart( new Cart( rs ) ); } catch( Exception ignored ) {}
        try { this.setProduct( new Product( rs ) ); } catch( Exception ignored ) {}
    }

    public UUID getCartId() {
        return cartId;
    }

    public void setCartId(UUID cartId) {
        this.cartId = cartId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
