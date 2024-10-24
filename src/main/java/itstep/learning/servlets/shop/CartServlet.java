package itstep.learning.servlets.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.CartDao;
import itstep.learning.dal.dao.shop.ProductDao;
import itstep.learning.dal.dto.User;
import itstep.learning.dal.dto.shop.Cart;
import itstep.learning.dal.dto.shop.Product;
import itstep.learning.rest.RestMetaData;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Singleton
public class CartServlet extends RestServlet {
    private final CartDao cartDao;
    private final ProductDao productDao;

    @Inject
    public CartServlet(CartDao cartDao, ProductDao productDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.restResponse = new RestResponse()
                .setMeta( new RestMetaData()
                        .setUri( "/shop/cart" )
                        .setMethod( req.getMethod() )
                        .setName( "KN-P-213 Shop API for user's carts" )
                        .setServerTime( new Date() )
                        .setAllowedMethods( new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"} )
                );
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getAttribute( "auth-token-user" );
        if( user != null ) {
            super.sendResponse( 200, cartDao.getCartByUser( user, true ) );
        }
        else {
            super.sendResponse( 401, null );
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Додати товар до кошику (CREATE)
        // 1. Встановити користувача (за токеном) - у фільтрі
        //  1.1. Якщо приходить запит від неавторизованого користувача, то ...
        // 2. Дізнатись чи є в користувача відкритий кошик
        //     якщо є, то додаємо до нього,
        //     якщо немає, то створюємо новий
        // 3. Перевіряємо чи є зазначений товар у кошику
        //     якщо є - збільшуємо кількість
        //     якщо немає - додаємо
        String productId = req.getParameter( "product-id" );
        if( productId == null || productId.isEmpty() ) {
            super.sendResponse( 400, "Missing required parameter 'product-id'" );
            return;
        }
        Product product = productDao.getByIdOrSlug( productId );
        if( product == null ) {
            super.sendResponse( 404, "Product not found" );
            return;
        }
        User user = (User) req.getAttribute( "auth-token-user" );
        if( user == null ) {
            super.sendResponse( 401 );
            return;
        }
        try {
            if( cartDao.add(user, product) ) {
                super.sendResponse( 201, "Added" );
            }
            else {
                super.sendResponse( 500, "Error adding product" );
            }
        }
        catch( Exception ex ) {
            super.sendResponse( 400, ex.getMessage() );
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getAttribute( "auth-token-user" );
        if( user == null ) {
            super.sendResponse( 401, null );
            return;
        }
        String cartId = req.getParameter( "cart-id" );
        if( cartId == null || cartId.isEmpty() ) {
            super.sendResponse( 400, "Missing required parameter 'cart-id'" );
            return;
        }
        String productId = req.getParameter( "product-id" );
        if( productId == null || productId.isEmpty() ) {
            super.sendResponse( 400, "Missing required parameter 'product-id'" );
        }
        int increment;
        String delta = req.getParameter( "delta" );
        if( delta == null || delta.isEmpty() ) {
            increment = 1;
        }
        else {
            try { increment = Integer.parseInt( delta ); }
            catch( NumberFormatException ignored ) {
                super.sendResponse( 400, "Invalid format for parameter 'delta'" );
                return;
            }
        }
        try {
            if( cartDao.update(cartId, productId, increment) ) {
                super.sendResponse( 202, "Updated" );
            }
            else {
                super.sendResponse( 500, "Error updating product" );
            }
        }
        catch( Exception ex ) {
            super.sendResponse( 400, ex.getMessage() );
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getAttribute( "auth-token-user" );
        if( user == null ) {
            super.sendResponse( 401, null );
            return;
        }
        Cart cart = cartDao.getCartByUser(user);
        if( cart == null ) {
            super.sendResponse( 404, "User's cart not found" );
            return;
        }
        String cartId = req.getParameter( "cart-id" );
        if( cartId == null || cartId.isEmpty() ) {
            super.sendResponse( 400, "Missing required parameter 'cart-id'" );
            return;
        }
        if( ! cart.getId().toString().equals( cartId ) ) {
            super.sendResponse( 403, "Access forbidden to 'cart-id'" );
            return;
        }
        // Наявність параметра "product-id" свідчить про видалення cart-item,
        // відсутність - про закриття всього кошику
        String productId = req.getParameter( "product-id" );
        if( productId == null || productId.isEmpty() ) {
            cartDao.deleteCart( cartId );
        }
        else {
            cartDao.deleteCartItem( cartId, productId );
        }
        super.sendResponse( 202, "Data to be processed" );
    }

}
