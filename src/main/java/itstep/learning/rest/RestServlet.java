package itstep.learning.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RestServlet extends HttpServlet {
    protected RestResponse restResponse;
    private HttpServletResponse response;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        response = resp;
        super.service(req, resp);
    }

    protected void sendResponse( int code, Object data, int maxAge ) throws IOException {
        if( code > 0 ) {
            restResponse.setStatus( new RestStatus( code ) );
        }
        if( data != null ) {
            restResponse.setData( data );
        }
        // Self-descriptive messages: Each message includes enough information to describe how to process the message.
        // For example, which parser to invoke can be specified by a media type.
        response.setContentType( "application/json" );

        // Cache – Responses indicate their own cacheability
        response.setHeader( "Cache-Control", maxAge > 0 ? "max-age=" + maxAge : "no-cache" );

        response.getWriter().print( gson.toJson( restResponse ) );
    }

    protected void sendResponse( int code, Object data ) throws IOException {
        this.sendResponse( code, data, 0 );
    }

    protected void sendResponse( Object data ) throws IOException {
        if( restResponse.getStatus() == null ) {
            this.sendResponse( 200, data );
        }
        else {
            this.sendResponse( -1, data );
        }
    }

    protected void sendResponse( int code ) throws IOException {
        this.sendResponse( code, null );
    }

    protected void sendResponse() throws IOException {
        if( restResponse.getStatus() == null ) {
            this.sendResponse( 200 );
        }
        else {
            this.sendResponse( -1 );
        }
    }
}
