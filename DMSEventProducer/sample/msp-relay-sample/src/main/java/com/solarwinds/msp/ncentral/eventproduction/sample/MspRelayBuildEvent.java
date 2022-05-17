package com.solarwinds.msp.ncentral.eventproduction.sample;

import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.ApplicationStatusOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.AssetMediaAccessOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.DeviceAssetOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.LogicalDeviceOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.MappedDriveOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.NetworkAdapterConfigurationOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.NetworkAdapterOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.PortStatusOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.SharedFoldersOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.SystemServiceOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.VideoControllerOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.AddressOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ApplianceOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ApplicationOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ClientOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ContactOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.DeviceOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.NotificationProfileOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.PatchCategoryOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.PatchOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.PatchProductOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.RuleCustomerOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.UsersOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.FirewallIncidentsOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.GenericServiceDataOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.TaskThresholdOuterClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

public class MspRelayBuildEvent {
    static UUID uuid = UUID.randomUUID();
    public static String validDeviceEvent = DeviceOuterClass.Device.class.getCanonicalName();
    public static DeviceOuterClass.Device validDeviceEventObject = DeviceOuterClass.Device.newBuilder()
            .setClientId(101)
            .setDeviceId(1907480434)
            .setGuid("ee507d9d-498a-4921-a1ce-013a38504213-20190129-184159")
            .setName("EHAKKI-DT")
            .setDescription("Network device discovered using Asset Discovery - 1907480434")
            .setUri("1.1.1.1")
            .setDeviceClass("Servers - Windows")
            .setIsManaged(true)
            .setActive(true)
            .setInstallTimestamp(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
            .setWarrantyExpiryDate(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
            .setPurchaseDate(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
            .setSourceUri("test.testing.com")
            .setRemoteControlUri("remote.control.com")
            .setIsDynamicUri(true)
            .setIsSystem(true)
            .setIsProbe(true)
            .setIsDiscoveredAsset(true)
            .setLeaseExpiryDate(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
            .setExpectedReplacementDate(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
            .setExplicitlyUnmanaged(true)
            .setDeviceCost(123.45)
            .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
            .setAction(ActionOuterClass.Action.ADD)
            .setContext(MspContextOuterClass.MspContext.newBuilder()
                    .setBizAppsCustomerId(uuid.toString())
                    .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                    .build())
            .build();

    public static final String ValidClientEvent = ClientOuterClass.Client.class.getCanonicalName();
    public static ClientOuterClass.Client ValidClientEventObject = ClientOuterClass.Client.newBuilder()
            .setClientId(103)
            .setParentId(1)
            .setName("Another So")
            .setClientType(ClientOuterClass.Client.ClientType.VAR)
            .setCreated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
            .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
            .addAddress(AddressOuterClass.Address.newBuilder()
                    .setAddress1("235 street st")
                    .setAddress2("u7")
                    .setCity("kanata")
                    .setPostalCode("k7k6k4")
                    .setStateProvince("ontario")
                    .setCountry("CA")
                    .build())
            .addContact(ContactOuterClass.Contact.newBuilder()
                    .setTitle("Mr.")
                    .setPhoneNumber("555-444-3333")
                    .setLastName("Testingstuff")
                    .setFirstName("Ima")
                    .setEmail("Ima.Testingstuff@mail.com")
                    .setDepartment("Test Department")
                    .setIsPrimary(true)
                    .setFullName("Ima Testingstuff")
                    .build())
            .setAction(ActionOuterClass.Action.ADD)
            .setContext(MspContextOuterClass.MspContext.newBuilder()
                    .setBizAppsCustomerId(uuid.toString())
                    .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                    .build())
            .build();

    public static final String clientMissingCustomeridEvent =
            GenericServiceDataOuterClass.GenericServiceData.class.getCanonicalName();
    public static GenericServiceDataOuterClass.GenericServiceData clientMissingCustomeridEventObject =
            GenericServiceDataOuterClass.GenericServiceData.newBuilder()
                    .setTaskId(887366936)
                    .setSourceName("datadns_detailed")
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("dns_response")
                            .setFieldType("Integer")
                            .setIntegerField(0)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("dnsa")
                            .setFieldType("Boolean")
                            .setBooleanField(false)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("dnsr")
                            .setFieldType("Boolean")
                            .setBooleanField(true)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo01_tinyint")
                            .setFieldType("Integer")
                            .setIntegerField(Integer.MAX_VALUE - 1)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo02_smallint")
                            .setFieldType("Integer")
                            .setIntegerField(Integer.MAX_VALUE - 2)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo03_bigint")
                            .setFieldType("Long")
                            .setLongField(Long.MAX_VALUE - 1)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo04_decimal")
                            .setFieldType("Float")
                            .setFloatField(123.456F)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo05_real")
                            .setFieldType("Float")
                            .setFloatField(123.4567F)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo06_float")
                            .setFieldType("Float")
                            .setFloatField(123.45678F)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo07_double")
                            .setFieldType("Double")
                            .setDoubleField(Double.MAX_VALUE - 1.0D)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo08_date")
                            .setFieldType("Timestamp")
                            .setTimestampField(
                                    Timestamp.newBuilder().setSeconds(1550757112 - 1).setNanos(68000000).build())
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo09_timestamp")
                            .setFieldType("Timestamp")
                            .setTimestampField(
                                    Timestamp.newBuilder().setSeconds(1550757112 - 2).setNanos(68000000).build())
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo10_string")
                            .setFieldType("String")
                            .setStringField("foo string value")
                            .build())
                    .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setStateId(5)
                    .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId(uuid.toString())
                            .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                            .build())
                    .build();
    public static final String user = UsersOuterClass.Users.class.getCanonicalName();
    public static UsersOuterClass.Users userObject = UsersOuterClass.Users.newBuilder()
            .setUserId(1813129454)
            .setUserName("servicetech@theservicegroup.com")
            .setClientId(261)
            .setDescription("Service Tech for ErdemCo")
            .setEnabled(false)
            .setDeleted(false)
            .addAddress(AddressOuterClass.Address.newBuilder()
                    .setAddress1("235 street st")
                    .setAddress2("u7")
                    .setCity("kanata")
                    .setStateProvince("ontario")
                    .setPostalCode("k7k6k4")
                    .setCountry("CA")
                    .build())
            .addContact(ContactOuterClass.Contact.newBuilder()
                    .setTitle("Mr.")
                    .setPhoneNumber("555-444-3333")
                    .setLastName("Testingstuff")
                    .setFirstName("Ima")
                    .setEmail("Ima.Testingstuff@mail.com")
                    .setDepartment("Test Department")
                    .setIsPrimary(true)
                    .setFullName("Ima Testingstuff")
                    .build())

            .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
            .setAction(ActionOuterClass.Action.ADD)
            .setContext(MspContextOuterClass.MspContext.newBuilder()
                    .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                    .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                    .build())
            .build();

    public static String taskThreshold = TaskThresholdOuterClass.TaskThreshold.class.getCanonicalName();
    public static TaskThresholdOuterClass.TaskThreshold taskThresholdObject =
            TaskThresholdOuterClass.TaskThreshold.newBuilder()
                    .setTaskId(529324613)
                    .setGenericServiceScanDetailId(3561800)
                    .setMaxValue(300)
                    .setMinValue(0)
                    .setStateId(3)
                    //.setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                            .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                            .build())
                    .build();

    public static final String ruleCustomer = RuleCustomerOuterClass.RuleCustomer.class.getCanonicalName();
    public static RuleCustomerOuterClass.RuleCustomer ruleCustomerObject =
            RuleCustomerOuterClass.RuleCustomer.newBuilder()
                    .setClientId(101)
                    .setRuleId(502)
                    .setDeleted(false)
                    .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                            .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                            .build())
                    .build();
    public static final String firewallIncidents =
            FirewallIncidentsOuterClass.FirewallIncidents.class.getCanonicalName();
    public static FirewallIncidentsOuterClass.FirewallIncidents firewallIncidentsObject =
            FirewallIncidentsOuterClass.FirewallIncidents.newBuilder()
                    .setTaskId(2030277180)
                    .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setStateId(3)
                    .setEmergencyIncidents(0)
                    .setAlertIncidents(0)
                    .setCriticalIncidents(0.0F)
                    .setErrorIncidents(1.0F)
                    .setWarningIncidents(0.0F)
                    .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                            .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                            .build())
                    .build();

    private static final String BIZAPPS_CUSTOMER_ID = "aaea6111-b13f-462b-9385-4f3baa7f0ccc";
    private static final boolean DELETED = true;
    private static final Integer DEVICE_ID = 12345;
    private static final String DISPLAY_NAME = "Application display name";
    private static final String LICENSE_KEY = "Application license key";
    private static final String LICENSE_TYPE = "Application license type";
    private static final String NCENTRAL_SERVER_GUID = "225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5";
    private static final String PRODUCT_ID = "Application product id";
    private static final String PUBLISHER = "Application publisher name";
    private static final String VERSION = "Application version name";
    private static final Integer EVENTING_CONFIG_CUSTOMER_ID = 1;

    public static final String applicationStatus =
            ApplicationStatusOuterClass.ApplicationStatus.class.getCanonicalName();
    ApplicationStatusOuterClass.ApplicationStatus applicationStatusObject =
            ApplicationStatusOuterClass.ApplicationStatus.newBuilder()
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId(BIZAPPS_CUSTOMER_ID)
                            .setSystemGuid(NCENTRAL_SERVER_GUID)
                            .build())
                    .setDeleted(DELETED)
                    .setDeviceId(DEVICE_ID)
                    .setInstallationDate(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setApplication(ApplicationOuterClass.Application.newBuilder()
                            .setName(DISPLAY_NAME)
                            .setPublisher(PUBLISHER)
                            .setVersion(VERSION)
                            .build())
                    .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757113).setNanos(68000000).build())
                    .setLicenseKey(LICENSE_KEY)
                    .setLicenseType(LICENSE_TYPE)
                    .setProductId(PRODUCT_ID)
                    .build();

    private static LocalDate publishedDate = LocalDate.of(2019, 2, 20);

    public static String patch = PatchOuterClass.Patch.class.getCanonicalName();
    public static PatchOuterClass.Patch patchObject = PatchOuterClass.Patch.newBuilder()
            .setPatchGuid("06b10fef-59d6-4fff-a85b-44a5b73abb61")
            .setName("Definition Update for Windows Defender Antivirus - KB2267602 (Definition 1.287.394.0)")
            .setDescription(
                    "Install this update to revise the definition files that are used to detect viruses, spyware, and other potentially unwanted software. Once you have installed this item, it cannot be removed.")
            .setKbNumber("2267602")
            .setInfoUrls("https://go.microsoft.com/fwlink/?linkid=2007160")
            .setProducts("8c3fcc84-7410-4a95-8b89-a166a0190486")
            .setSeverity(PatchOuterClass.Patch.Severity.UNSPECIFIED)
            .setPublishDate(Timestamp.newBuilder()
                    .setSeconds(
                            Instant.from(publishedDate.atStartOfDay().atZone(ZoneId.systemDefault())).getEpochSecond())
                    .build())
            .setPatchCategory(PatchCategoryOuterClass.PatchCategory.newBuilder()
                    .setSourceId("e0789628-ce08-4437-be74-2495b842f43b")
                    .build())
            .setAction(ActionOuterClass.Action.ADD)
            .setContext(MspContextOuterClass.MspContext.newBuilder()
                    .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                    .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                    .build())
            .build();

    public static final String patchProduct = PatchProductOuterClass.PatchProduct.class.getCanonicalName();
    public static PatchProductOuterClass.PatchProduct patchProductObject =
            PatchProductOuterClass.PatchProduct.newBuilder()
                    .setSourceId("df5e394f-553a-4e24-835b-94cb72ca665e")
                    .setParentSourceId("388b1461-f354-4811-98e2-4501c204a985")
                    .setName("Firefox 21.0")
                    .build();

    public static final String systemService = SystemServiceOuterClass.SystemService.class.getCanonicalName();
    public static SystemServiceOuterClass.SystemService systemServiceObject =
            SystemServiceOuterClass.SystemService.newBuilder()
                    .setDeviceId(1454030117)
                    .setDeleted(false)
                    .setCaption("Servicio Google Update (gupdate)")
                    .setExecutableName("\"GoogleUpdate.exe\"")
                    .setServiceName("gupdate")
                    .setStartupType("Auto")
                    .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                            .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                            .build())
                    .build();

    public static final String genericServiceData =
            GenericServiceDataOuterClass.GenericServiceData.class.getCanonicalName();
    public static GenericServiceDataOuterClass.GenericServiceData genericServiceDataObject =
            GenericServiceDataOuterClass.GenericServiceData.newBuilder()
                    .setTaskId(887366936)
                    .setSourceName("datadns_detailed")
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("dns_response")
                            .setFieldType("Integer")
                            .setIntegerField(0)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("dnsa")
                            .setFieldType("Boolean")
                            .setBooleanField(false)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("dnsr")
                            .setFieldType("Boolean")
                            .setBooleanField(true)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo01_tinyint")
                            .setFieldType("Integer")
                            .setIntegerField(Integer.MAX_VALUE - 1)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo02_smallint")
                            .setFieldType("Integer")
                            .setIntegerField(Integer.MAX_VALUE - 2)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo03_bigint")
                            .setFieldType("Long")
                            .setLongField(Long.MAX_VALUE - 1)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo04_decimal")
                            .setFieldType("Float")
                            .setFloatField(123.456F)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo05_real")
                            .setFieldType("Float")
                            .setFloatField(123.4567F)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo06_float")
                            .setFieldType("Float")
                            .setFloatField(123.45678F)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo07_double")
                            .setFieldType("Double")
                            .setDoubleField(Double.MAX_VALUE - 1.0D)
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo08_date")
                            .setFieldType("Timestamp")
                            .setTimestampField(
                                    Timestamp.newBuilder().setSeconds(1550757112 - 1).setNanos(68000000).build())
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo09_timestamp")
                            .setFieldType("Timestamp")
                            .setTimestampField(
                                    Timestamp.newBuilder().setSeconds(1550757112 - 2).setNanos(68000000).build())
                            .build())
                    .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                            .setFieldName("foo10_string")
                            .setFieldType("String")
                            .setStringField("foo string value")
                            .build())
                    .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setStateId(5)
                    .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                            .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                            .build())
                    .build();
    public static final String assetMediaAccess = AssetMediaAccessOuterClass.AssetMediaAccess.class.getCanonicalName();
    public static AssetMediaAccessOuterClass.AssetMediaAccess assetMediaAccessObject =
            AssetMediaAccessOuterClass.AssetMediaAccess.newBuilder()
                    .setSizeMb(476937.53173828125)
                    .setDeviceId(1907480434)
                    .setDescription("\\\\.\\PHYSICALDRIVE1")
                    .setMediaType("External hard disk media")
                    .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                            .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                            .build())
                    .build();

    public static final String deviceAsset = DeviceAssetOuterClass.DeviceAsset.class.getCanonicalName();
    public static DeviceAssetOuterClass.DeviceAsset deviceAssetObject = DeviceAssetOuterClass.DeviceAsset.newBuilder()
            .setDeviceId(1907480434)
            .setDomain("swdev.local")
            .setManufacturer("Dell Inc.")
            .setModelNumber("OptiPlex 9020")
            .setNetBiosName("EHAKKI-DT")
            .setSystemType("x64-based PC")
            .setTotalMemoryBytes(34359738368F)
            .setTotalMemorySlots(4)
            .setPopulatedMemorySlots(4)
            .setSerialNumber("78Z0V52")
            .setVersion("5.5.0")
            .setAmtUuid("a8461754-a1e5-e511-b16b-0894ef11a790")
            .setQstEnabled("0")
            .setAmtVersion("1.1")
            .setWirelessManagementState("1")
            .setChassisType("Convertible")
            .setUuid("55b017d1-653a-a910-d16b-a2a68b76105f")
            .setTimeZone("(UTC-05:00) Eastern Time (US & Canada)")
            .setProductNumber("06B7")
            .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
            .setAction(ActionOuterClass.Action.ADD)
            .setContext(MspContextOuterClass.MspContext.newBuilder()
                    .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                    .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                    .build())
            .build();

    private static final float MAX_CAPACITY_BYTES = 255_715_176_448f;
    private static final String VOLUME_NAME = "Volume name";

    public static final String logicalDevice = LogicalDeviceOuterClass.LogicalDevice.class.getCanonicalName();
    public static LogicalDeviceOuterClass.LogicalDevice logicalDeviceObject =
            LogicalDeviceOuterClass.LogicalDevice.newBuilder()
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId(BIZAPPS_CUSTOMER_ID)
                            .setSystemGuid(NCENTRAL_SERVER_GUID)
                            .build())
                    .setDeleted(DELETED)
                    .setDeviceId(DEVICE_ID)
                    .setMaxCapacityBytes(MAX_CAPACITY_BYTES)
                    .setVolumeName(VOLUME_NAME)
                    .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757113).setNanos(68000000).build())
                    .build();

    private static final String DRIVE_NAME = "Drive name";
    private static final String REMOTE_PATH = "Remote path";
    public static final String mappedDrive = MappedDriveOuterClass.MappedDrive.class.getCanonicalName();
    public static MappedDriveOuterClass.MappedDrive mappedDriveObject = MappedDriveOuterClass.MappedDrive.newBuilder()
            .setAction(ActionOuterClass.Action.ADD)
            .setContext(MspContextOuterClass.MspContext.newBuilder()
                    .setBizAppsCustomerId(BIZAPPS_CUSTOMER_ID)
                    .setSystemGuid(NCENTRAL_SERVER_GUID)
                    .build())
            .setDeleted(DELETED)
            .setDeviceId(DEVICE_ID)
            .setDriveName(DRIVE_NAME)
            .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757113).setNanos(68000000).build())
            .setRemotePath(REMOTE_PATH)
            .build();

    public static final String networkAdapter = NetworkAdapterOuterClass.NetworkAdapter.class.getCanonicalName();
    public static NetworkAdapterOuterClass.NetworkAdapter networkAdapterObject =
            NetworkAdapterOuterClass.NetworkAdapter.newBuilder()
                    .setDeviceId(1454030117)
                    .setAdapterId("3")
                    .setDeleted(false)
                    .setDescription("hyper - v virtual ethernet adapter")
                    .setMacAddress("ab:cd:ef:gh:12:34")
                    .setManufacturer("Microsoft")
                    .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                            .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                            .build())
                    .build();

    public static final String networkAdapterConfiguration =
            NetworkAdapterConfigurationOuterClass.NetworkAdapterConfiguration.class.getCanonicalName();
    public static NetworkAdapterConfigurationOuterClass.NetworkAdapterConfiguration networkAdapterConfigurationObject =
            NetworkAdapterConfigurationOuterClass.NetworkAdapterConfiguration.newBuilder()
                    .setDeviceId(1454030117)
                    .setDeleted(false)
                    .setDescription("hyper - v virtual ethernet adapter")
                    .setMacAddress("ab:cd:ef:gh:12:34")
                    .setIpAddress("1.1.1.1")
                    .setHostName("test.local")
                    .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                            .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                            .build())
                    .build();

    public static final String portStatus = PortStatusOuterClass.PortStatus.class.getCanonicalName();
    public static PortStatusOuterClass.PortStatus portStatusObject = PortStatusOuterClass.PortStatus.newBuilder()
            .setDeviceId(30185527)
            .setDeleted(false)
            .setServiceName("testservice")
            .setPort(777)
            .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
            .setAction(ActionOuterClass.Action.ADD)
            .setContext(MspContextOuterClass.MspContext.newBuilder()
                    .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                    .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                    .build())
            .build();
    public static String sharedFolders = SharedFoldersOuterClass.SharedFolders.class.getCanonicalName();
    public static SharedFoldersOuterClass.SharedFolders sharedFoldersObject =
            SharedFoldersOuterClass.SharedFolders.newBuilder()
                    .setDeviceId(30185527)
                    .setDeleted(false)
                    .setShareName("testshare")
                    .setPath("somepath")
                    .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                            .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                            .build())
                    .build();

    public static final String videoController = VideoControllerOuterClass.VideoController.class.getCanonicalName();
    public static VideoControllerOuterClass.VideoController videoControllerObject =
            VideoControllerOuterClass.VideoController.newBuilder()
                    .setDeviceId(1454030117)
                    .setDeleted(false)
                    .setAdapterRamBytes(8388608)
                    .setDescription("RAGE XL  PCI")
                    .setName("RAGE XL  PCI")
                    .setVideoControllerId("VideoController1")
                    .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                            .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                            .build())
                    .build();

    public static String appliance = ApplianceOuterClass.Appliance.class.getCanonicalName();
    public static ApplianceOuterClass.Appliance applianceObject = ApplianceOuterClass.Appliance.newBuilder().

            setApplianceId(154400920).

            setApplianceName("JSANTOS1-LT").

            setCustomerId(476).

            setDeviceId(32877124).

            setApplianceType("Agent").

            setIsConfigRequired(true).

            setIsReloadRequired(false).

            setIsPublic(false).

            setUri("jsantos1-lt.tul.solarwinds.net").

            setDescription("Network device discovered using Asset Discovery - 32877124").

            setVersion("2021.1.0.197").

            setLastLogin(Timestamp.newBuilder().

            setSeconds(1550757112).

            setNanos(68000000).

            build()).

            setAutoUpdate("Never").

            setIsSystem(false).

            setUpgradeAttempts(0).

            setReboot(false).

            setCreationTime(Timestamp.newBuilder().

            setSeconds(1550757112).

            setNanos(68000000).

            build()).

            setIsModuleConfigRequired(false).

            setReportedTimezoneId("08:00:00").

            setLastUpdated(Timestamp.newBuilder().

            setSeconds(1550757112).

            setNanos(68000000).

            build()).

            setAction(ActionOuterClass.Action.ADD).

            setContext(MspContextOuterClass.MspContext.newBuilder().

            setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc").

            setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5").

            build()).

            build();

    public static String notificationProfile =
            NotificationProfileOuterClass.NotificationProfile.class.getCanonicalName();
    public static NotificationProfileOuterClass.NotificationProfile notificationProfileObject =
            NotificationProfileOuterClass.NotificationProfile.newBuilder()
                    .setProfileId(1841213127)
                    .setDescription("6 Minute Delay")
                    .setDeleted(false)
                    .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                            .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                            .build())
                    .build();
}
