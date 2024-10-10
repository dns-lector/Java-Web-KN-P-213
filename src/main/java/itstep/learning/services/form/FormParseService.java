package itstep.learning.services.form;

import javax.servlet.http.HttpServletRequest;

public interface FormParseService {
    FormParseResult parse( HttpServletRequest req );
}
