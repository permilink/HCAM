package org.cqupt.crypto.hcaa.fabric.execution;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.cqupt.crypto.hcaa.fabric.FabricClient;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.identity.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

//@Component("HyperLedger-Fabric")
public class Fabrics {

    private static final Properties pros = new Properties();

    private static ManagedChannel channel = null;

    public void init(){
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource resource = resolver.getResource("src/main/resources/fabric.config");
            File configFile = resource.getFile();

            if (configFile.exists()) {
                pros.load(Files.newBufferedReader(configFile.toPath()));
            } else {
                System.err.println("Configuration file not found: " + configFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ManagedChannel newGrpcConnection(String tlsCertPath, String peerEndPtAddr) throws IOException, CertificateException, CertificateException {
        BufferedReader tlsCertReader = Files.newBufferedReader(Paths.get(tlsCertPath));
        X509Certificate tlsCert = Identities.readX509Certificate(tlsCertReader);

        return NettyChannelBuilder.forTarget(peerEndPtAddr)
                .sslContext(GrpcSslContexts.forClient().trustManager(tlsCert).build())
                .build();
    }

    public static Identity newIdentity(String userCertPath, String mspId) throws IOException, CertificateException {
        BufferedReader certReader = Files.newBufferedReader(Paths.get(userCertPath));
        X509Certificate certificate = Identities.readX509Certificate(certReader);

        return new X509Identity(mspId, certificate);
    }

    public static Signer newSigner(String pk) throws IOException, InvalidKeyException, InvalidKeyException {
        BufferedReader keyReader = Files.newBufferedReader(getPrivateKeyPath(pk));
        PrivateKey privateKey = Identities.readPrivateKey(keyReader);

        return Signers.newPrivateKeySigner(privateKey);
    }

    private static Path getPrivateKeyPath(String pk) throws IOException {
        try (Stream<Path> keyFiles = Files.list(Paths.get(pk))) {
            return keyFiles.findFirst().orElseThrow(null);
        }
    }

    public static Gateway fabricGateway(String tlsCertPath, String userCertPath, String userPriKeyPath, String mspId,String peerEndPtAddr) {

        Gateway.Builder builder = null;
        try {
            channel = newGrpcConnection(tlsCertPath,peerEndPtAddr);

            // Default timeouts for different gRPC calls
            builder = Gateway.newInstance().identity(newIdentity(userCertPath,mspId)).signer(newSigner(userPriKeyPath)).connection(channel)
                    // Default timeouts for different gRPC calls
                    .evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                    .endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
                    .submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                    .commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));
        } catch (IOException | InvalidKeyException | CertificateException e) {
            e.printStackTrace();
        }
        return builder.connect();
    }

    public static void closeChannel() {
        if (Objects.nonNull(channel)) {
            try {
                channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getChannel() {
        return pros.getProperty("channel_name");
    }

    public static int getDeployPort() {
        return Integer.parseInt(pros.getProperty("deploy_port"));
    }

    public static String logPlacehdr() {
        return "----- {} -----";
    }

    private static String getMsp() {
        return pros.getProperty("msp_id");
    }

    private static Path getUserCertPath() {
        return Paths.get(pros.getProperty("user_cert_path"));
    }

    private static Path getUserPriKeyPath() {
        return Paths.get(pros.getProperty("user_privatekey_path"));
    }

    private static Path getPeerCertPath() {
        System.out.println("fabric peer_tls_cert_path is "+pros.getProperty("peer_tls_cert_path"));
        return Paths.get(pros.getProperty("peer_tls_cert_path"));
    }

    private static String getPeerEndPtAddr() {
        return pros.getProperty("peer_endpoint_address");
    }

}
