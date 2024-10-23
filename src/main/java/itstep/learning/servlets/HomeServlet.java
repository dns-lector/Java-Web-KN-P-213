package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import itstep.learning.dal.dao.AuthDao;
import itstep.learning.dal.dao.shop.CartDao;
import itstep.learning.dal.dao.shop.ProductDao;
import itstep.learning.services.db.DbService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Singleton
public class HomeServlet extends HttpServlet {
    // Впровадження залежностей (інжекція)
    private final AuthDao authDao;   // інжекцію класів (не інтерфейсів) реєструвати не треба
    private final DbService dbService;
    private final CartDao cartDao;

    @Inject
    public HomeServlet(AuthDao authDao, @Named("Oracle") DbService dbService, CartDao cartDao) {
        this.authDao = authDao;
        this.dbService = dbService;
        this.cartDao = cartDao;
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        boolean isSigned = false;
        Object signature = req.getAttribute("signature");
        if ( signature instanceof Boolean ) {
            isSigned = (Boolean) signature;
        }
        if( isSigned ) {
            String dbMessage;
            try {
                dbMessage =
                        authDao.install()
                        // && cartDao.install()
                        ? "Install OK"
                        : "Install failed";
            }
            catch( Exception e ) {
                dbMessage = e.getMessage();
            }


            try {
                Statement stmt = dbService.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery( "SELECT CURRENT_TIMESTAMP FROM dual" );
                rs.next();
                dbMessage += " " + rs.getString(1);
                rs.close();
                stmt.close();
            }
            catch (SQLException e) {
                dbMessage += " " + e.getMessage();
            }

            req.setAttribute( "hash", dbMessage );
            req.setAttribute( "body", "home.jsp" );   // ~ ViewData["body"] = "home.jsp";
        }
        else {
            req.setAttribute( "body", "not_found.jsp" );
        }

        // ~ return View();
        req.getRequestDispatcher( "WEB-INF/views/_layout.jsp" ).forward(req, resp);

        // resp.getWriter().println("<h1>Home</h1>");
    }
}

/*
Сервлети - спеціалізовані класи для мережних задач, зокрема,
HttpServlet - для веб-задач, є аналогом контролерів в ASP
 */
