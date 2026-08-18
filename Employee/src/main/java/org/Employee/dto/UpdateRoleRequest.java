package org.Employee.dto;

import org.Employee.enums.RoleName;

public class UpdateRoleRequest {

    private RoleName roleName;

    public UpdateRoleRequest() {}

    public RoleName getRoleName() {
        return roleName;
    }

    public void setRoleName(RoleName roleName) {
        this.roleName = roleName;
    }
}
