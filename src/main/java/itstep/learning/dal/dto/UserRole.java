package itstep.learning.dal.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserRole {
    private UUID roleId;
    private String roleName;
    private boolean canCreate;
    private boolean canRead;
    private boolean canUpdate;
    private boolean canDelete;

    public UserRole() { }

    public UserRole( ResultSet resultSet ) throws SQLException {
        this.setRoleId( UUID.fromString( resultSet.getString( "role_id" ) ) );
        this.setRoleName(  resultSet.getString(  "role_name"  ) );
        this.setCanCreate( resultSet.getBoolean( "can_create" ) );
        this.setCanRead(   resultSet.getBoolean( "can_read"   ) );
        this.setCanUpdate( resultSet.getBoolean( "can_update" ) );
        this.setCanDelete( resultSet.getBoolean( "can_delete" ) );
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public boolean isCanCreate() {
        return canCreate;
    }

    public void setCanCreate(boolean canCreate) {
        this.canCreate = canCreate;
    }

    public boolean isCanRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean isCanUpdate() {
        return canUpdate;
    }

    public void setCanUpdate(boolean canUpdate) {
        this.canUpdate = canUpdate;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }
}
