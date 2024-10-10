package itstep.learning.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.AuthDao;
import itstep.learning.dal.dto.User;
import itstep.learning.rest.RestMetaData;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestServlet;
import itstep.learning.rest.RestStatus;
import itstep.learning.services.form.FormParseResult;
import itstep.learning.services.form.FormParseService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;

@Singleton
public class AuthServlet extends RestServlet {
    private final AuthDao authDao;
    private final FormParseService formParseService;

    @Inject
    public AuthServlet(AuthDao authDao, FormParseService formParseService) {
        this.authDao = authDao;
        this.formParseService = formParseService;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.restResponse = new RestResponse()
                .setMeta( new RestMetaData()
                        .setUri( "/auth" )
                        .setMethod( req.getMethod() )
                        .setName( "KN-P-213 Authentication API" )
                        .setServerTime( new Date() )
                        .setAllowedMethods( new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"} )
                );
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /*
            The 'Basic' HTTP Authentication Scheme
            https://datatracker.ietf.org/doc/html/rfc7617
         */
        // Вилучаємо заголовок Authorization
        // Перевіряємо, що схема Basic
        // Виділяємо дані автентифікації (credentials)
        // Декодуємо їх за Base64
        // Розділяємо за першим символом ':'
        // запитуємо автентифікацію в DAO
        RestResponse restResponse = new RestResponse();
        try {
            // Вилучаємо заголовок Authorization
            String authHeader = req.getHeader( "Authorization" );
            if( authHeader == null ) {
                throw new ParseException( "Authorization header not found", 401 );
            }
            // Перевіряємо, що схема Basic
            String authScheme = "Basic ";  // trailing space - the part of standard
            if( ! authHeader.startsWith( authScheme ) ) {
                throw new ParseException( "Invalid Authorization scheme. Required " + authScheme, 400 );
            }
            // Виділяємо дані автентифікації (credentials)
            String credentials = authHeader.substring( authScheme.length() );
            // Декодуємо їх за Base64
            String decodedCredentials;
            try {
                decodedCredentials = new String(
                        Base64.getUrlDecoder().decode( credentials.getBytes( StandardCharsets.UTF_8 ) ),
                        StandardCharsets.UTF_8
                );
            }
            catch( IllegalArgumentException ignored ) {
                throw new ParseException( "Invalid credentials format", 400 );
            }
            // Розділяємо за першим символом ':'
            String[] parts = decodedCredentials.split( ":", 2 );
            if( parts.length != 2 ) {
                throw new ParseException( "Invalid credentials composition", 400 );
            }

            User user = authDao.authenticate( parts[0], parts[1] );
            if( user == null ) {
                throw new ParseException( "Credentials rejected", 401 );
            }
            super.sendResponse( user );
        }
        catch( ParseException ex ) {
            super.sendResponse( ex.getErrorOffset(), ex.getMessage() );
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Sign Up
        // req.getParameter("name") - параметри запиту: URL- або form-дані
        // АЛЕ! за умови, що форма передається як x-www-form-urlencoded
        // і не працює для multipart/form-data
        FormParseResult formParseResult = formParseService.parse( req );
        super.sendResponse(
                "files: " + formParseResult.getFiles().size() +
                ", fields: " + formParseResult.getFields().size()
        );
    }
}
/*
HTTP/1.1 200 OK
Content-Type: application/x-www-form-urlencoded

name=User&password=123


HTTP/1.1 200 OK
Content-Type: multipart/form-data; delimiter=asdf...w
--asdf...w
Content-Disposition: form-field; name=user-name

user
--asdf...w
Content-Disposition: form-field; name=user-password

123
--asdf...w
Content-Disposition: form-file; name=user-avatar

PNGd45sf4ghsem8jz'dflhgsd4f
Gzz;jd'
--asdf...w--

 */