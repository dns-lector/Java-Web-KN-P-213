package itstep.learning.dal.dto.shop;

import itstep.learning.dal.dto.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class Cart {
    private UUID id;
    private UUID userId;
    private Date createDt;
    private Date closeDt;
    private int  status;

    private User user;

    public Cart() {
    }

    public Cart( ResultSet rs ) throws SQLException {
        this.setId( UUID.fromString( rs.getString("cart_id") ) );
        this.setUserId( UUID.fromString( rs.getString("user_id") ) );
        Timestamp timestamp = rs.getTimestamp( "cart_create_dt" );
        this.setCreateDt( new Date( timestamp.getTime() ) );
        timestamp = rs.getTimestamp( "cart_close_dt" );
        if ( timestamp != null ) {
            this.setCloseDt( new Date( timestamp.getTime() ) );
        }
        this.setStatus( rs.getInt( "cart_status" ) );

        try { this.setUser( new User( rs ) ); }
        catch( Exception ignored ) {}
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Date getCreateDt() {
        return createDt;
    }

    public void setCreateDt(Date createDt) {
        this.createDt = createDt;
    }

    public Date getCloseDt() {
        return closeDt;
    }

    public void setCloseDt(Date closeDt) {
        this.closeDt = closeDt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
