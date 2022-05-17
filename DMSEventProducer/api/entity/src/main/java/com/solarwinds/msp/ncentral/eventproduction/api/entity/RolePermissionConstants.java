package com.solarwinds.msp.ncentral.eventproduction.api.entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 
 * This class holds the user role permission constants.
 *
 */
public class RolePermissionConstants {
    /**
     * Advanced reporting user read only permission for reports
     */
    public static final int NCENTRAL_REPORTS_ADVANCED_READ_ONLY = 922;
    /**
     * Advanced Reporting user manage permission for reports
     */
    public static final int NCENTRAL_REPORTS_ADVANCED_ALL = 923;

    /**
     * {@link List} of Advanced Reporting user permissions.
     */
    public static final List<Integer> NCENTRAL_REPORTS_ADVANCED_PERMISSIONS =
            Collections.unmodifiableList(Arrays.asList(RolePermissionConstants.NCENTRAL_REPORTS_ADVANCED_ALL,
                    RolePermissionConstants.NCENTRAL_REPORTS_ADVANCED_READ_ONLY));

    private RolePermissionConstants() {}
}
