package com.midServer.SetMid.Service.AWS;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.midServer.SetMid.Service.AWS.Utils.createTagging;
import static com.midServer.SetMid.Service.AWS.Utils.getEC2Client;

public class nic {
    public boolean deleteAWSNic(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                final String region, final String nicId) {
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        DeleteNetworkInterfaceRequest request = new DeleteNetworkInterfaceRequest().withNetworkInterfaceId(nicId);
        DeleteNetworkInterfaceResult result = ec2.deleteNetworkInterface(request);
        System.out.println("nic with nicid: " + nicId + " deleted successfully");
        return result != null && result.getSdkHttpMetadata().getHttpStatusCode() == 200;
    }

    public static List<NetworkInterface> getAWSNic(final AWSStaticCredentialsProvider awsStaticCredentialsProvider, final String region) {
        List<NetworkInterface> networkInterfaceList = new ArrayList<>();
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        DescribeNetworkInterfacesRequest request = new DescribeNetworkInterfacesRequest();
        DescribeNetworkInterfacesResult response = null;
        do {
            if (response != null && response.getNextToken() != null) {
                request = new DescribeNetworkInterfacesRequest().withNextToken(response.getNextToken());
            }
            response = ec2.describeNetworkInterfaces(request);
            networkInterfaceList.addAll(response.getNetworkInterfaces());
        } while (response.getNextToken() != null);
        return networkInterfaceList;
    }

    public static String createAWSNic(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                               final String region, final String subnetId) {
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        CreateNetworkInterfaceRequest request = new CreateNetworkInterfaceRequest().withDescription(
                "Java created network interface").withSubnetId(subnetId);
        CreateNetworkInterfaceResult response = ec2.createNetworkInterface(request);
        System.out.println("nic with subnetId->" + subnetId + " and" + "  id->" + response.getNetworkInterface().getNetworkInterfaceId());
        return response.getNetworkInterface().getNetworkInterfaceId();
    }

    public static String createNIC(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                            final String regionName, final String subNetId, final String nicName) {
        String nicID = checkNic(awsStaticCredentialsProvider, regionName, nicName, subNetId);
        if (nicID == null) {
            nicID = createAWSNic(awsStaticCredentialsProvider, regionName, subNetId);
            System.out.println("nic with subnetId->" + subNetId + " and" + "  id->" + nicID);
            if (createTagging(awsStaticCredentialsProvider, regionName, nicID, "Name", nicName)) {
                return nicID;
            }
        }
        return nicID;
    }


    public static String checkNic(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                           final String regionName, final String nicName, final String subnetId) {
        AtomicReference<String> id = new AtomicReference<>();
        getAWSNic(awsStaticCredentialsProvider, regionName).parallelStream().forEach(networkInterface -> {
            if (networkInterface.getTagSet().parallelStream().anyMatch(tag -> tag.getKey().equals("Name") && tag.getValue().equals(nicName))
                    && networkInterface.getSubnetId().equalsIgnoreCase(subnetId)
            ) {
                id.getAndSet(networkInterface.getNetworkInterfaceId());
            }
        });
        return id.get();
    }

    public boolean deleteNIC(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                             final String regionName, final String nicName, final String subnetName) {

        String subnetId = NetworkAndSubnet.checkSubnet(awsStaticCredentialsProvider, regionName, subnetName);
        String nicID = checkNic(awsStaticCredentialsProvider, regionName, nicName, subnetId);
        if (nicID != null) {
            return deleteAWSNic(awsStaticCredentialsProvider, regionName, nicID);
        } else {
            System.out.println("Nic does not exist");
        }
        return false;
    }
}
