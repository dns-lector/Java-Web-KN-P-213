package itstep.learning.servlets.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.CartDao;
import itstep.learning.dal.dao.shop.ProductDao;
import itstep.learning.dal.dto.User;
import itstep.learning.dal.dto.shop.CartItem;
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

        }
        else {
            super.sendResponse( 401, new CartItem[0] );
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
        if( user != null ) {
            if( cartDao.add( user, product ) ) {
                super.sendResponse( 201, "Added" );
            }
            else {
                super.sendResponse( 500, "Error adding product" );
            }
        }
        else {
            super.sendResponse( 401 );
        }
    }
}
