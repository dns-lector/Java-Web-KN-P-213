package itstep.learning.servlets.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.CategoryDao;
import itstep.learning.dal.dto.shop.Category;
import itstep.learning.rest.RestMetaData;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestServlet;
import itstep.learning.services.form.FormParseResult;
import itstep.learning.services.form.FormParseService;
import itstep.learning.services.storage.StorageService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

@Singleton
public class CategoryServlet extends RestServlet {
    private final Logger logger;
    private final FormParseService formParseService;
    private final StorageService storageService;
    private final CategoryDao categoryDao;

    @Inject
    public CategoryServlet(Logger logger, FormParseService formParseService, StorageService storageService, CategoryDao categoryDao) {
        this.logger = logger;
        this.formParseService = formParseService;
        this.storageService = storageService;
        this.categoryDao = categoryDao;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.restResponse = new RestResponse()
                .setMeta( new RestMetaData()
                        .setUri( "/shop/category" )
                        .setMethod( req.getMethod() )
                        .setName( "KN-P-213 Shop API for product categories" )
                        .setServerTime( new Date() )
                        .setAllowedMethods( new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"} )
                );
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.sendResponse( 200, categoryDao.read() );
    }

    @Override   // CREATE - створити нову категорію товарів
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FormParseResult formParseResult = formParseService.parse( req );
        Category category = new Category();
        String data = formParseResult.getFields().get( "category-name" );
        if( data == null || data.isEmpty() ) {
            super.sendResponse( 400, "Missing required field 'category-name' " );
            return;
        }
        category.setName( data );

        data = formParseResult.getFields().get( "category-description" );
        if( data == null || data.isEmpty() ) {
            super.sendResponse( 400, "Missing required field 'category-description' " );
            return;
        }
        category.setDescription( data );

        try {
            data = storageService.saveFile(
                    formParseResult.getFiles().get( "category-image" ) );
        }
        catch( IOException ex ) {
            logger.warning( ex.getMessage() );
            super.sendResponse( 400, "Error processing 'category-image'" );
        }
        category.setImageUrl( data );

        if( ( category = categoryDao.create( category ) ) != null ) {
            super.sendResponse( 201, category ) ;
        }
        else {
            super.sendResponse( 500, "Error creating category" );
        }
    }
}
