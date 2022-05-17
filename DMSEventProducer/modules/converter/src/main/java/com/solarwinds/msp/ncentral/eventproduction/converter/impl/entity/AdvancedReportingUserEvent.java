package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.RolePermissionConstants;
import com.solarwinds.msp.ncentral.proto.entity.entity.AccessGroupTypeOuterClass.AccessGroupType;
import com.solarwinds.msp.ncentral.proto.entity.entity.AdvancedReportingUserOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.AdvancedReportingUserOuterClass.AdvancedReportingUser;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools.getBoolean;
import static com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools.getInteger;

/**
 * This class handles additional business logic for processing data for the
 * {@link AdvancedReportingUsersOuterClass.AccessGroup} data Protocol Buffers
 * entity.
 */
class AdvancedReportingUserEvent implements EntityParser<AdvancedReportingUserOuterClass.AdvancedReportingUser> {

    private static final String ATTRIBUTE_PERMISSION_ID = "permissionid";
    private static final String ATTRIBUTE_DELETED = "deleted";
    private static final String ATTRIBUTE_CUSTOMER_GROUP = "customergroup";
    private static final String ATTRIBUTE_FIRST_NAME = "firstname";
    private static final String ATTRIBUTE_EMAIL = "email1";
    private static final String DELETED_PATTERN = "DELETED(\\s+ID)?\\s+[0-9]+:\\s+";
    @Override
    public List<AdvancedReportingUser> parseRecord(Event event, AdvancedReportingUser messageEntity) {
        final Map<String, String> entity = event.getEntity();
        final Integer permissionId = getInteger(entity, ATTRIBUTE_PERMISSION_ID);
        final AdvancedReportingUser.RoleType roleType;

        if (RolePermissionConstants.NCENTRAL_REPORTS_ADVANCED_READ_ONLY == permissionId) {
            roleType = AdvancedReportingUser.RoleType.READ_ONLY;
        } else if (RolePermissionConstants.NCENTRAL_REPORTS_ADVANCED_ALL == permissionId) {
            roleType = AdvancedReportingUser.RoleType.MANAGE;
        } else {
            roleType = AdvancedReportingUser.RoleType.UNKNOWN;
        }

        final boolean deleted = getBoolean(entity, ATTRIBUTE_DELETED);

        if ((!deleted
                && AdvancedReportingUser.RoleType.UNKNOWN == roleType)
                || !entity.containsKey(ATTRIBUTE_CUSTOMER_GROUP)) {
            return Collections.emptyList();
        }


        final String customerGroup = StringUtils.defaultString(entity.get(ATTRIBUTE_CUSTOMER_GROUP));
        final List<Integer> customerIdList = deleted ? Collections.emptyList()
                : Stream.of(customerGroup.split(","))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());

        final AdvancedReportingUser.ResourceAccessGroup resourceAccessGroup =
                AdvancedReportingUser.ResourceAccessGroup
                        .newBuilder()
                        .addAllId(customerIdList)
                        .setType(AccessGroupType.CLIENT)
                        .build();

        if (deleted) {
            final String firstName = entity.getOrDefault(ATTRIBUTE_FIRST_NAME, "").replaceAll(DELETED_PATTERN, "");
            final String email = entity.getOrDefault(ATTRIBUTE_EMAIL, "").replaceAll(DELETED_PATTERN, "");

            if (StringUtils.isNotBlank(firstName) && StringUtils.isNotBlank(email)) {
                return Collections.singletonList(
                        messageEntity.toBuilder()
                                .setFirstName(firstName)
                                .setEmail(email)
                                .setRoleType(roleType)
                                .addResourceAccessGroup(resourceAccessGroup)
                                .build());
            }

        }

        return Collections.singletonList(
                messageEntity.toBuilder().setRoleType(roleType).addResourceAccessGroup(resourceAccessGroup).build());
    }

}
