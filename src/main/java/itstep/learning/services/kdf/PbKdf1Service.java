package itstep.learning.services.kdf;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.hash.HashService;

/**
 * KDF implementation by sec. 5.1 RFC 2898
 */
@Singleton
public class PbKdf1Service implements KdfService {
    private final static int dkLen = 20;   // довжина ключа у символах
    private final HashService hashService;

    @Inject
    public PbKdf1Service(HashService hashService) {
        this.hashService = hashService;
    }

    @Override
    public String dk( String password, String salt ) {
        // додатковий параметр iterationCount будемо закладати у сіль
        // як її суфікс після символу "."
        // salt = 202c962c59075b.3
        int iterationCount = 0;
        int dotPos = salt.lastIndexOf( "." );
        if ( dotPos > 0 ) {
            try {
                iterationCount = Integer.parseInt( salt.substring( dotPos + 1 ) );
            }
            catch ( NumberFormatException ignored ) {}
        }
        if( iterationCount < 1 || iterationCount >= 10 ) {
            iterationCount = 3;   // значення за замовчанням
        }
        String t = hashService.hash( password + salt ) ;
        for( int i = 1; i < iterationCount; i++ ) {
            t = hashService.hash( t ) ;
        }
        while( t.length() < dkLen ) {
            // якщо геш дає менше символів ніж вимагається - дублюємо його
            t += t;
        }
        return t.substring( 0, dkLen );
    }
}
