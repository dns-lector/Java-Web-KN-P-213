package itstep.learning.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.AuthDao;
import itstep.learning.dal.dto.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;

@Singleton
public class AuthServlet extends HttpServlet {
    private final AuthDao authDao;

    @Inject
    public AuthServlet(AuthDao authDao) {
        this.authDao = authDao;
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

            restResponse.setStatus( "success" );
            restResponse.setCode( 200 );
            restResponse.setData( user );
        }
        catch( ParseException ex ) {
            restResponse.setStatus( "error" );
            restResponse.setCode( ex.getErrorOffset() );
            restResponse.setData( ex.getMessage() );
        }

        Gson gson = new GsonBuilder().serializeNulls().create();
        resp.setContentType( "application/json" );
        resp.getWriter().print( gson.toJson( restResponse ) );
    }

    class RestResponse {
        private int code;
        private String status;
        private Object data;

        public RestResponse() {
        }

        public RestResponse(int code, String status, Object data) {
            this.code = code;
            this.status = status;
            this.data = data;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}
/*
Д.З. Створити сторінку для автоматизованого тестування АРІ
У кодах сторінки надсилаються різні запити на /auth
 як правильні, так і такі, що містять помилки
 і виводяться відповіді на них

[Auth]
Without 'Authorization' header: {code: 401, status: 'error', data: 'Authorization header not found'}
With non-Basic scheme: {...}
Correct with login '234' and password '123': {code: 200, status: 'success', data: '234:123'}

** Відповіді, що відповідають очікуванням, позначати зеленим кольором, інші - червоним

Встановити Oracle XE
https://www.oracle.com/database/technologies/appdev/xe/quickstart.html
 */