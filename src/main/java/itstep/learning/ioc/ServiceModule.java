package itstep.learning.ioc;

import com.google.inject.AbstractModule;
import itstep.learning.services.db.DbService;
import itstep.learning.services.db.MySqlDbService;
import itstep.learning.services.form.FormParseService;
import itstep.learning.services.form.MixedFormParseService;
import itstep.learning.services.hash.HashService;
import itstep.learning.services.hash.Md5HashService;
import itstep.learning.services.kdf.KdfService;
import itstep.learning.services.kdf.PbKdf1Service;
import itstep.learning.services.storage.LocalStorageService;
import itstep.learning.services.storage.StorageService;

public class ServiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind( HashService.class      ).to( Md5HashService.class        );
        bind( KdfService.class       ).to( PbKdf1Service.class         );
        bind( DbService.class        ).to( MySqlDbService.class        );
        bind( FormParseService.class ).to( MixedFormParseService.class );
        bind( StorageService.class   ).to( LocalStorageService.class   );
    }
}
