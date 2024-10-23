package itstep.learning.dal.dao.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import itstep.learning.dal.dto.User;
import itstep.learning.dal.dto.shop.Cart;
import itstep.learning.dal.dto.shop.Product;
import itstep.learning.services.db.DbService;

import java.sql.*;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

@Singleton
public class CartDao {
    private final DbService dbService;
    private final Logger logger;

    @Inject
    public CartDao(@Named("Oracle") DbService dbService, Logger logger) {
        this.dbService = dbService;
        this.logger = logger;
    }

    public boolean add( User user, Product product ) {
        if( user == null || product == null ) {
            return false;
        }
        // ... 2. Дізнатись чи є в користувача відкритий кошик
        //          якщо немає, то створюємо новий
        //     додаємо до нього,
        Cart cart = getCartByUser( user ) ;
        if( cart == null ) {
            cart = openCartForUser( user );
            if( cart == null ) {
                return false;
            }
        }
        // 3. Перевіряємо чи є зазначений товар у кошику
        //     якщо є - збільшуємо кількість
        //     якщо немає - додаємо
        int quantity = -1;
        String sql = "SELECT ci.cart_item_quantity FROM cart_items ci WHERE ci.cart_id = ? AND ci.product_id = ?";
        try( PreparedStatement prep = dbService.getConnection().prepareStatement(sql) ) {
            prep.setString( 1, cart.getId().toString() );
            prep.setString( 2, product.getId().toString() );
            ResultSet rs = prep.executeQuery();
            if( rs.next() ) {
                quantity = rs.getInt( 1 );
            }
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }
        if( quantity == -1 ) {
            //     якщо немає - додаємо
            sql = "INSERT INTO cart_items (cart_item_quantity, cart_item_price, cart_id, product_id) " +
                    "VALUES (1, ?, ?, ?)";
        }
        else {
            //     якщо є - збільшуємо кількість
            sql = "UPDATE cart_items " +
                    "SET cart_item_quantity = cart_item_quantity + 1, cart_item_price = cart_item_price + ? " +
                    "WHERE cart_id = ? AND product_id = ?";
        }
        try( PreparedStatement prep = dbService.getConnection().prepareStatement(sql) ) {
            prep.setDouble( 1, product.getPrice() );
            prep.setString( 2, cart.getId().toString() );
            prep.setString( 3, product.getId().toString() );
            prep.executeUpdate();
            return true;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }
    }

    public Cart openCartForUser( User user ) {
        if( user == null ) {
            return null;
        }
        Cart cart = new Cart() ;
        cart.setId( UUID.randomUUID() );
        cart.setUserId( user.getUserId() );
        cart.setUser(user);
        cart.setCreateDt( new Date() );
        cart.setCloseDt( null );
        cart.setStatus( 0 );

        String sql = "INSERT INTO carts (cart_id, user_id, cart_create_dt, cart_close_dt, cart_status) " +
                "VALUES(?, ?, ?, ?, ?) ";
        try( PreparedStatement prep = dbService.getConnection().prepareStatement(sql) ) {
            prep.setString( 1, cart.getId().toString() );
            prep.setString( 2, cart.getUserId().toString() );
            prep.setTimestamp( 3, new Timestamp( cart.getCreateDt().getTime() ) );
            if( cart.getCloseDt() != null ) {
                prep.setTimestamp( 4, new Timestamp( cart.getCloseDt().getTime() ) );
            }
            else {
                prep.setTimestamp( 4, null );
            }
            prep.setInt( 5, cart.getStatus() );
            prep.executeUpdate();
            return cart;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
        }
        return null;
    }

    public Cart getCartByUser( User user ) {
        String sql = "SELECT * FROM carts c WHERE c.user_id = ? AND c.cart_close_dt IS NULL";
        try( PreparedStatement prep = dbService.getConnection().prepareStatement(sql) ) {
            prep.setString( 1, user.getUserId().toString() );
            ResultSet rs = prep.executeQuery();
            if( rs.next() ) {
                return new Cart( rs );
            }
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
        }
        return null;
    }

    public boolean install() {
        String sql = "CREATE TABLE carts (" +
                "cart_id         CHAR(36)  PRIMARY KEY ," +
                "user_id         CHAR(36)  NOT NULL," +
                "cart_create_dt  DATE      NOT NULL," +
                "cart_close_dt   DATE          NULL," +
                "cart_status     INT       DEFAULT ON NULL 0" +
                ") ";

        try( Statement stmt = dbService.getConnection().createStatement() ) {
            stmt.executeUpdate( sql ) ;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }
        sql = "CREATE TABLE cart_items (" +
                "cart_id            CHAR(36)     NOT NULL," +
                "product_id         CHAR(36)     NOT NULL," +
                "cart_item_quantity INT          DEFAULT ON NULL 1," +
                "cart_item_price    NUMBER(8,2)  NOT NULL," +
                "PRIMARY KEY(cart_id, product_id)" +
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
[carts]                    [cart_items]
|cart_id                   |cart_id
user_id                    |product_id
cart_create_dt             cart_item_quantity
cart_close_dt              cart_item_price
cart_status (0,1,-1)
 */
