package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.db.DbService;
import itstep.learning.services.hash.HashService;
import itstep.learning.services.kdf.KdfService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@Singleton
public class HomeServlet extends HttpServlet {
    // Впровадження залежностей (інжекція)
    private final HashService hashService;
    private final KdfService kdfService;
    private final DbService dbService;

    @Inject
    public HomeServlet(HashService hashService, KdfService kdfService, DbService dbService) {
        this.hashService = hashService;
        this.kdfService = kdfService;
        this.dbService = dbService;
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
                dbService.getConnection();
                dbMessage = "Connection OK";
            }
            catch( SQLException ex ) {
                dbMessage = ex.getMessage();
            }

            req.setAttribute( "hash",
                    hashService.hash( "123" ) + " " +
                    kdfService.dk( "password", "salt.4" ) + " " +
                    dbMessage
            );
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
