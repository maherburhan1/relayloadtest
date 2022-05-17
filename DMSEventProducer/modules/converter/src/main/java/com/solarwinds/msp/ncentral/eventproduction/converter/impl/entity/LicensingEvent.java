package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.proto.entity.entity.LicensingOuterClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles additional business logic for processing data for the {@link LicensingOuterClass.Licensing} data
 * Protocol Buffers entity.
 */
class LicensingEvent implements EntityParser<LicensingOuterClass.Licensing> {

    // TODO Split this huge "God Method" to smaller methods with clear and readable business logic.
    // TODO Define and use constants instead of duplicating strings and numbers.
    // TODO It is out of the scope of the NCCF-12175 bug.
    @Override
    public List<LicensingOuterClass.Licensing> parseRecord(Event event, LicensingOuterClass.Licensing messageEntity) {
        Map<String, String> entity = event.getEntity();

        Boolean thirdPartyPatchEnabled = Tools.getBoolean(entity, "thirdpartypatchenabled");
        String licenseMode = entity.getOrDefault("licensemode", null);
        Boolean veritasEnabled = Tools.getBoolean(entity, "veritasenabled");
        Boolean snmpEnabled = Tools.getBoolean(entity, "snmpenabled");
        Boolean edfEnabled = Tools.getBoolean(entity, "edfenabled");
        String backupManagerLicenseType = entity.getOrDefault("backupmanagerlicensetype", null);
        Boolean patchEnabled = Tools.getBoolean(entity, "patchenabled");
        Boolean netpathEnabled = Tools.getBoolean(entity, "netpathenabled");
        Boolean isManagedAsset = Tools.getBoolean(entity, "ismanagedasset");
        Boolean remoteManagementEnabled = Tools.getBoolean(entity, "remotemanagementenabled");
        Boolean reactiveEnabled = Tools.getBoolean(entity, "reactiveenabled");
        Boolean securityManagerEnabled = Tools.getBoolean(entity, "securitymanagerenabled");
        String professionalLicenseMode = event.getProfessionalModeLicenseType();
        Integer deviceClassId = Tools.getInteger(entity, "deviceclassid");

        ArrayList<Integer> serverClassIds = new ArrayList<>();
        serverClassIds.add(104);
        serverClassIds.add(106);
        serverClassIds.add(98);
        serverClassIds.add(95);
        serverClassIds.add(92);
        serverClassIds.add(91);

        ArrayList<Integer> workstationClassIds = new ArrayList<>();
        workstationClassIds.add(107);
        workstationClassIds.add(105);
        workstationClassIds.add(99);
        workstationClassIds.add(94);
        workstationClassIds.add(90);

        ArrayList<Integer> networkClassIds = new ArrayList<>();
        networkClassIds.add(103);
        networkClassIds.add(102);
        networkClassIds.add(101);
        networkClassIds.add(100);
        networkClassIds.add(93);

        Map<String, Boolean> licenseValues = new HashMap<>();
        licenseValues.put("Essential", ("Essential".equalsIgnoreCase(licenseMode) && isManagedAsset));
        licenseValues.put("Professional",
                ("Professional".equalsIgnoreCase(licenseMode) && "Per Device".equalsIgnoreCase(professionalLicenseMode)
                        && isManagedAsset));
        licenseValues.put("ProfessionalServer",
                ("Professional".equalsIgnoreCase(licenseMode) && "Tiered".equalsIgnoreCase(professionalLicenseMode)
                        && serverClassIds.contains(deviceClassId) && isManagedAsset));
        licenseValues.put("ProfessionalWorkstation",
                ("Professional".equalsIgnoreCase(licenseMode) && "Tiered".equalsIgnoreCase(professionalLicenseMode)
                        && workstationClassIds.contains(deviceClassId) && isManagedAsset));
        licenseValues.put("ProfessionalNetwork",
                ("Professional".equalsIgnoreCase(licenseMode) && "Tiered".equalsIgnoreCase(professionalLicenseMode)
                        && networkClassIds.contains(deviceClassId) && isManagedAsset));
        licenseValues.put("Mobile", ("Mobile".equalsIgnoreCase(licenseMode) && isManagedAsset));
        licenseValues.put("remotemanagementenabled", (remoteManagementEnabled && isManagedAsset));
        licenseValues.put("patchenabledessential",
                (patchEnabled && "Essential".equalsIgnoreCase(licenseMode) && isManagedAsset));
        licenseValues.put("patchenabled",
                (patchEnabled && !"Essential".equalsIgnoreCase(licenseMode) && isManagedAsset));
        licenseValues.put("thirdpartypatchenabled", (thirdPartyPatchEnabled && isManagedAsset));
        licenseValues.put("BackupManagerStandard",
                ("BackupManagerStandard".equalsIgnoreCase(backupManagerLicenseType) && isManagedAsset));
        licenseValues.put("BackupManagerAdvanced",
                ("BackupManagerAdvanced".equalsIgnoreCase(backupManagerLicenseType) && isManagedAsset));
        licenseValues.put("BackupManagerSBS",
                ("BackupManagerSBS".equalsIgnoreCase(backupManagerLicenseType) && isManagedAsset));
        licenseValues.put("BackupManagerWorkstation",
                ("BackupManagerWorkstation".equalsIgnoreCase(backupManagerLicenseType) && isManagedAsset));
        licenseValues.put("securitymanagerenabled", (securityManagerEnabled && isManagedAsset));
        licenseValues.put("edfenabled", (edfEnabled && isManagedAsset));
        licenseValues.put("snmpenabled", (snmpEnabled && isManagedAsset));
        licenseValues.put("veritasenabled", (veritasEnabled && isManagedAsset));
        licenseValues.put("reactiveenabled",
                (reactiveEnabled && !"Essential".equalsIgnoreCase(licenseMode) && isManagedAsset));
        licenseValues.put("netpathenabled",
                (netpathEnabled && !"Essential".equalsIgnoreCase(licenseMode) && isManagedAsset));

        Iterable<LicensingOuterClass.Licensing.License> allLicenses = getLicenses(licenseValues);

        Map<String, Object> coreLicenseValues = new HashMap<>();
        coreLicenseValues.put("addAllLicense", allLicenses);

        return Collections.singletonList(Tools.setNullableField(messageEntity.toBuilder(), coreLicenseValues).build());
    }

    // TODO Split this huge "God Method" to smaller methods with clear and readable business logic.
    // TODO Define and use constants instead of magic strings.
    // TODO It is out of the scope of the NCCF-12175 bug.
    private static List<LicensingOuterClass.Licensing.License> getLicenses(Map<String, Boolean> licenseValues) {
        HashMap<String, String> licenses = new HashMap<>();
        licenses.put("edfenabled", "External Data Feed");
        licenses.put("patchenabled", "Microsoft Patch Management");
        licenses.put("snmpenabled", "Simple Network Management Protocol");
        licenses.put("thirdpartypatchenabled", "Third Party Patch Management (Professional and Essentials)");
        licenses.put("veritasenabled", "Backup Exec");
        licenses.put("Professional", "Professional");
        licenses.put("Mobile", "Mobile");
        licenses.put("Essential", "Essential");
        licenses.put("endpointsecurityenabled", "Endpoint Security");
        licenses.put("remotemanagementenabled", "Remote Support Manager");
        licenses.put("BackupManagerStandard", "Backup Manager Standard Server");
        licenses.put("BackupManagerAdvanced", "Backup Manager Advanced Server");
        licenses.put("BackupManagerSBS", "Backup Manager SBS Server");
        licenses.put("BackupManagerWorkstation", "Backup Manager Workstation (Laptop/Desktop)");
        licenses.put("ProfessionalNetwork", "Professional Network-class");
        licenses.put("ProfessionalServer", "Professional Server-class");
        licenses.put("ProfessionalWorkstation", "Professional Workstation-class");
        licenses.put("securitymanagerenabled", "AV Defender");
        licenses.put("patchenabledessential", "Microsoft Patch Management on Essentials");
        licenses.put("exchangeemailprotectionenabled", "Exchange E-mail Protection");
        licenses.put("reactiveenabled", "Direct Support");
        licenses.put("netpathenabled", "Net Path");

        List<LicensingOuterClass.Licensing.License> allLicenses = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : licenseValues.entrySet()) {
            String licenseKey = entry.getKey();
            String licenseName = licenses.get(licenseKey);
            Boolean licenseActive = entry.getValue();

            HashMap<String, Object> license = new HashMap<>();
            license.put("setSourceName", licenseKey);
            license.put("setName", licenseName);
            license.put("setActive", licenseActive);

            LicensingOuterClass.Licensing.License.Builder mspEventBusEntityBuilder =
                    LicensingOuterClass.Licensing.License.newBuilder();
            Tools.setNullableField(mspEventBusEntityBuilder, license);
            LicensingOuterClass.Licensing.License mspEventBusEntity = mspEventBusEntityBuilder.build();
            allLicenses.add(mspEventBusEntity);
        }
        return allLicenses;
    }
}
