package itstep.learning.dal.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public class Token {
    private UUID tokenId;
    private UUID userId;
    private Date iat;
    private Date exp;

    public Token() { }

    public Token( ResultSet resultSet ) throws SQLException {
        String id = resultSet.getString( "token_id" );
        if( id == null ) {
            throw new SQLException( "Token with null ID" );
        }
        this.setTokenId( UUID.fromString( id ) );

        this.setUserId( UUID.fromString( resultSet.getString( "user_id" ) ) );
        this.setIat( new Date( resultSet.getTimestamp( "iat" ).getTime() ) );
        this.setExp( new Date( resultSet.getTimestamp( "exp" ).getTime() ) );
    }

    public UUID getTokenId() {
        return tokenId;
    }

    public void setTokenId(UUID tokenId) {
        this.tokenId = tokenId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Date getIat() {
        return iat;
    }

    public void setIat(Date iat) {
        this.iat = iat;
    }

    public Date getExp() {
        return exp;
    }

    public void setExp(Date exp) {
        this.exp = exp;
    }
}
