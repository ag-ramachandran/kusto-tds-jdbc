package com.microsoft.kusto.jdbc;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws Exception{
        try {
            ConfidentialClientApplication app = ConfidentialClientApplication.builder(
                            "",
                            ClientCredentialFactory.createFromSecret(""))
                    .authority(String.format("https://login.microsoftonline.com/%s",""))
                    .build();
            ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(
                            Collections.singleton("https://.dev.kusto.windows.net/.default"))
                    .build();
            CompletableFuture<IAuthenticationResult> future = app.acquireToken(clientCredentialParam);
            IAuthenticationResult authResult = future.get();
            System.out.println("Got Auth token : " + authResult.accessToken());
            SQLServerDataSource ds = new SQLServerDataSource();
            ds.setServerName("xxx.dev.kusto.windows.net");
            ds.setLoginTimeout(30);
            ds.setDatabaseName("demoDB");
            ds.setHostNameInCertificate("*.kusto.windows.net");
            ds.setEncrypt(true);
            ds.setAccessToken(authResult.accessToken());
            Connection connection = ds.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT TOP 10 * FROM PbStreams");
            while (resultSet.next()){
                System.out.println(resultSet.getString("payload"));
            }
        } catch (MalformedURLException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}