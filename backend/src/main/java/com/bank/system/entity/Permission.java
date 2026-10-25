package com.bank.system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "permissions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_permission_name", columnNames = "permission_name")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Permission extends BaseEntity {

    @Column(name = "permission_name", nullable = false, length = 50)
    private String permissionName;

    @Column(name = "permission_description", length = 200)
    private String permissionDescription;

    @Column(name = "resource_type", length = 100)
    private String resourceType;

    @Column(name = "action", length = 50)
    private String action;
}
