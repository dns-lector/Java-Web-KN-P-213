package itstep.learning.dal.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class User {
    private UUID   userId;
    private String userName;
    private String email;
    private String phone;
    private String avatarUrl;
    private Date   birthdate;
    private Date   deleteDt;
    private UserRole role;

    public User() {}

    public User( ResultSet resultSet ) throws SQLException {
        this.setUserId( UUID.fromString( resultSet.getString( "user_id" ) ) );
        this.setUserName( resultSet.getString( "user_name" ) );
        this.setEmail( resultSet.getString( "email" ) );
        this.setPhone( resultSet.getString( "phone" ) );
        this.setAvatarUrl( resultSet.getString( "avatar_url" ) );
        Timestamp timestamp = resultSet.getTimestamp( "birthdate" );
        this.setBirthdate( new Date( timestamp.getTime() ) );
        timestamp = resultSet.getTimestamp( "delete_dt" );
        if( timestamp != null ) {
            this.setDeleteDt( new Date( timestamp.getTime() ) );
        }
        try {
            this.setRole( new UserRole( resultSet ) );
        }
        catch( Exception ignored ) {
            this.setRole( null ) ;
        }
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public Date getDeleteDt() {
        return deleteDt;
    }

    public void setDeleteDt(Date deleteDt) {
        this.deleteDt = deleteDt;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
