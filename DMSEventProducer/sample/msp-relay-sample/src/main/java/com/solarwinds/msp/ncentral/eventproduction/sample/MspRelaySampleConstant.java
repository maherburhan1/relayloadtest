package com.solarwinds.msp.ncentral.eventproduction.sample;

public final class MspRelaySampleConstant {
    /**
     * Default MSP Relay Sample Server host to connect to.
     */
    //private static final String DEFAULT_SERVER_HOST = "relay.dev.nkld.builtwith.solar";
    protected static final String DEFAULT_SERVER_HOST = "us.stg.relay.system-monitor.com";

    /**
     * Default MSP Relay Sample Server port to connect to.
     */
    //    private static final int DEFAULT_SERVER_PORT = 80;
    protected static final int DEFAULT_SERVER_PORT = 443;
    /**
     * Default PEM Encoded X509 Server Certificate.
     * <p>
     * Following certificate was provided by the MSP Relay developer. It was specifically created for the server with
     * hostname: BRN-VSR-MQA-340 where the sample relay is independently installed.
     */

    public static final String DEFAULT_CLIENT_CERTIFICATE =
            "-----BEGIN CERTIFICATE-----\n" + "MIIDljCCAn6gAwIBAgIUbH5QH2BffgVg4mAymTHzcuPU8Q4wDQYJKoZIhvcNAQEL\n"
                    + "BQAwUzETMBEGA1UEChMKU29sYXJXaW5kczEUMBIGA1UECxMLRW5naW5lZXJpbmcx\n"
                    + "JjAkBgNVBAMTHWRldmVsb3AudmF1bHQuYnVpbHR3aXRoLnNvbGFyMB4XDTIyMDMw\n"
                    + "ODIwMDYyNFoXDTIzMDMwODIwMDY1M1owIDEeMBwGA1UEAxMVTU9DSy1SRUxBWS1T\n"
                    + "RVJWRVItMzQwMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwev/7aOR\n"
                    + "4iAXrxn0eetNQRCw+0dVpCnLZFM/USdGnL/zQ+S06uCb5FOY0F1p1NZl2BsmfFkZ\n"
                    + "pI1d/+JhamcXkWpLldNor2c3LqpOil64aUJL2y8p2BBn52j6wvjwse9PeKTtohQH\n"
                    + "Zd1vaU0E9ho0BqXQsCwA+tNOnoiUt5+1Sjd2OQdlJZFsT2as/6HaaNqSpHD1LDBA\n"
                    + "WHzAFR9lHYczdpi0fkiiUbxzPclAqIailkDB64Uh48fwOoVCA1KDuMbZTial8zrO\n"
                    + "fJ96m5CNTaOPivcKMjLMBg6FGPos3TgSLNBAt+rvi1f4RHQeaZNdAWInBaDCuP1q\n"
                    + "eEVYuPSwKF2j6wIDAQABo4GUMIGRMA4GA1UdDwEB/wQEAwIDqDAdBgNVHSUEFjAU\n"
                    + "BggrBgEFBQcDAQYIKwYBBQUHAwIwHQYDVR0OBBYEFFeCDsQbMlHaV0nNpJ2o24ES\n"
                    + "KrAkMB8GA1UdIwQYMBaAFKwO9/JZPY6SEckbj3I30b9ZAztpMCAGA1UdEQQZMBeC\n"
                    + "FU1PQ0stUkVMQVktU0VSVkVSLTM0MDANBgkqhkiG9w0BAQsFAAOCAQEAk7PmwwKu\n"
                    + "988wmJ8M4XpRaJ1Rluy6BmB3YJ8BBn3GC86uHecVBt5cIAelytt1465DYRqFLi8C\n"
                    + "2YZtGsZpD9m/zYgD6mWnT8Slt78RJvclm803bUbn0r/r60wky6Z8nQpcWzAbV1hd\n"
                    + "53+rZ1Bloydr+Q9o4Kq+SzY1RUOpORnAndn2gDwkXbla4y5AucT/YLqmu/bVhcQu\n"
                    + "BhjeZzo8WrlteKC4W2g8df02+rAmD1CCUPKOl+SffITjSqZtMWVaG/tgEEGiDSjg\n"
                    + "8dRwzZrzEJZKCrV+XlzBvLtzlasUCGjint3h0MyRzHJMqHbiplIQr8bJoO/XvATD\n" + "+PM3MuIwztDNrA==\n"
                    + "-----END CERTIFICATE-----";

    /**
     * Default PEM Encoded PKCS#8 Client Private Key.
     * <p>
     * Following key was provided by the MSP Relay developer. It was specifically created for for the server with
     * hostname: BRN-VSR-MQA-340 where the sample relay is independently installed.
     */

    public static final String DEFAULT_CLIENT_PRIVATE_KEY =
            "-----BEGIN PRIVATE KEY-----\n" + "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDB6//to5HiIBev\n"
                    + "GfR5601BELD7R1WkKctkUz9RJ0acv/ND5LTq4JvkU5jQXWnU1mXYGyZ8WRmkjV3/\n"
                    + "4mFqZxeRakuV02ivZzcuqk6KXrhpQkvbLynYEGfnaPrC+PCx7094pO2iFAdl3W9p\n"
                    + "TQT2GjQGpdCwLAD6006eiJS3n7VKN3Y5B2UlkWxPZqz/odpo2pKkcPUsMEBYfMAV\n"
                    + "H2UdhzN2mLR+SKJRvHM9yUCohqKWQMHrhSHjx/A6hUIDUoO4xtlOJqXzOs58n3qb\n"
                    + "kI1No4+K9woyMswGDoUY+izdOBIs0EC36u+LV/hEdB5pk10BYicFoMK4/Wp4RVi4\n"
                    + "9LAoXaPrAgMBAAECggEBAKJk4eO90NMOkQbfs23N8NoGAk/LgxqArYUMeb+aJnXq\n"
                    + "AOvgQOlYwvPIVfZX4+KK0KrSwb3sV58Hi6pXh8C1KHVsEHQdoFMTGpEHqdISVFhB\n"
                    + "XojkzCPWSUzvCt/v3JEFmQWtCBtsrm1NrJkAGKGHe/39Ke/+SavC/NmiAvhq1ESb\n"
                    + "DDyLked5Dm8VpcFn6uyKP0fLPPcB6i+lhWkdM8izF0X0HilRp10Uwzm8FI6d9rhN\n"
                    + "UbnbkXl9hkHxbfa13vkmmkboQgB9J5s7FDJxe1XXTItlIv1Re78owTCZ2aUd6Z1w\n"
                    + "HLq+vENLB/zd2NIs7NwUIElUFvczSRDFzJjHwJXxsKECgYEA4h0iWJFBXyVqrHAt\n"
                    + "XVM7aiRLX8lj7P7n3Gk1+i4VXJfBljGIejZfGzIHVId4CeQvNISK9z0wa0L9oR6d\n"
                    + "iEOLPddOA472FFbQ1FsvlfDSZCAG9tpQsqE9292hGIrn6Y5b7pLVWqwr4zNgZO+j\n"
                    + "n+Mq0RUp1cQp4BYpcBjM+MQCsOkCgYEA242brHKHDAQPqf2dWTsGpZXE7jKn5jEt\n"
                    + "uSC6XemQMkWtiboin0hlSBTdrzdVHkcemCTWTwK7FUgF1UQuir+M7CUHCHee95Ze\n"
                    + "eDbuDSYxM51huvKpAmnJAA+SKbyVQYV46Pbqw/vOd9M50RQvYutnviN+NdRNzBjh\n"
                    + "fiZaStvNybMCgYEAt3dcPvDdUg5SF8iNhoPaWa1JW1dVNR8/kp/rpCtwOckI0o0u\n"
                    + "Z7tmP/K/795bj+sOQlapSyLi/qVElk75E296s5rXOhvtn2Mx3px+Z/MXQxaZkAJk\n"
                    + "KAyc3KFf7UIIGoHgJhwN8Zxxn9A4Dq5C3aC9RRke8MxSDfjtdn8pEVA5vsECgYB1\n"
                    + "S7lgrysxIzZPJjybQGqzqkk0zQFgfOOlpTOMGs073nfxPe1jVaUull89Tf1+mJZi\n"
                    + "yENqoNXcb8cWAkAvRlj+jGWug2H9emtsJESX59Cfd7SP0Ta0IFoPW0JGZAcFo05O\n"
                    + "gCQ6olvTLpkT94m9enDJZ2L1VUBfCufloo3YsofF+wKBgQC1eTTx1bY2k8iIGpAx\n"
                    + "GaR7AtQRxlluxlD/SIp3TrjF46Ykp/kighi/vtvZVJw6jHQQRo28RbtYu36VKqne\n"
                    + "D9SVm+fcEycU7TMyucDkZ0enf92pSZMYOhfJPqN+wPGLq5MY+2RZce1Kx7V6t4wJ\n"
                    + "w9p8pgSV1o8zZl/QIPGo5OnbEA==\n" + "-----END PRIVATE KEY-----";

    /**
     * Default path to the PEM Encoded X509 Server Certificate (Certificate Authority).
     */
    protected static final String DEFAULT_SERVER_CERTIFICATE_PATH =
            "dms/service/src/com/nable/dms/config/develop_vault_built_with_solar.pem";

    protected static final String HELP =
            "Use the following options:\n" + "\t-Dhost=<HOSTNAME>    - the MSP Relay Sample Server host to connect to\n"
                    + "\t-Dport=<NUMBER>    - the MSP Relay Sample Server port to connect to\n"
                    + "\t-DclientCertificate=<FILE_PATH>    - the PEM Encoded X509 Client Certificate file path\n"
                    + "\t-DclientKey=<FILE_PATH>    - the PEM Encoded PKCS#8 Client Private Key file path\n"
                    + "\t-DserverCertificate=<FILE_PATH>    - the PEM Encoded X509 Server Certificate file path\n"
                    + "\t-DoverrideAuthority=<HOSTNAME>    - the (partial) hostname used for override authority\n"
                    + "\t-DeventType=<Event Type>(CLIENT, DEVICE, MISSINGCUSTOMER or ALL which include all\n"
                    + "\t-Devent";

    protected static final String BUSINESS_APPLICATIONS_CUSTOMER_ID =
            "BizApps ID: " + MspRelaySampleClient.class.getSimpleName();
    protected static final String N_CENTRAL_SYSTEM_GUID = "System GUID: " + MspRelaySampleClient.class.getSimpleName();
    protected static final int CLIENT_PARENT_ID = 123456789;
    protected static final String CLIENT_NAME_PREFIX = "Client Name: " + MspRelaySampleClient.class.getSimpleName();
    protected static final String EVENT_TYPE_CLIENT =
            "com.solarwinds.msp.ncentral.proto.entity.entity.ClientOuterClass.Client";
    protected static final String EVENT_TYPE_INVALID = "Wrong event type";

}
