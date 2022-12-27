package com.midServer.SetMid.Service.AWS;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.midServer.SetMid.Service.AWS.Utils.createTagging;
import static com.midServer.SetMid.Service.AWS.Utils.getEC2Client;

public class NetworkAndSubnet {
    public static boolean attachAWSInternetGateway(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                                   final String region, final String vpcId,
                                                   final String igID) {
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        AttachInternetGatewayRequest attachInternetGatewayRequest = new AttachInternetGatewayRequest()
                .withVpcId(vpcId).withInternetGatewayId(igID);
        AttachInternetGatewayResult result = ec2.attachInternetGateway(attachInternetGatewayRequest);
        System.out.println("IG attached successfully");
        return result != null && result.getSdkHttpMetadata().getHttpStatusCode() == 200;
    }

    public static String createAWSInternetGateway(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                                  final String region) {
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        CreateInternetGatewayRequest request = new CreateInternetGatewayRequest();
        return ec2.createInternetGateway(request).getInternetGateway().getInternetGatewayId();
    }

    public static boolean createIGAndAttachIG(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                              final String regionName, String vpcId) {
        AtomicBoolean checkIG = new AtomicBoolean(true);
        getEC2Client(regionName, awsStaticCredentialsProvider).describeInternetGateways()
                .getInternetGateways().forEach(a -> {
            a.getAttachments().forEach(b -> {
                if (b.getState().equalsIgnoreCase("available") && b.getVpcId().equals(vpcId)) checkIG.set(false);
            });
        });
        if (checkIG.get()) {
            String id = createAWSInternetGateway(awsStaticCredentialsProvider, regionName);
            if (id != null) {
                System.out.println("Trying to attach " + vpcId + " to the internet gateway");
                return attachAWSInternetGateway(awsStaticCredentialsProvider, regionName, vpcId, id);
            }
        }
        return false;
    }

    public static boolean deleteAWSVpc(final AWSStaticCredentialsProvider awsStaticCredentialsProvider, final String region,
                                       final String vpcId) {
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        DeleteVpcRequest request = new DeleteVpcRequest().withVpcId(vpcId);
        DeleteVpcResult result = ec2.deleteVpc(request);
        System.out.println(
                "VPC with name:" + vpcId + " deleted successfully");
        return result != null && result.getSdkHttpMetadata().getHttpStatusCode() == 200;
    }

    public static boolean deleteAWSInternetGateway(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                                   final String region, final String internetGateway) {
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        DeleteInternetGatewayRequest request = new DeleteInternetGatewayRequest().withInternetGatewayId(internetGateway);
        DeleteInternetGatewayResult result = ec2.deleteInternetGateway(request);
        System.out.println("IG deleted successfully");
        return result != null && result.getSdkHttpMetadata().getHttpStatusCode() == 200;
    }

    public static boolean detachAWSInternetGateway(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                                   final String region, final String netId,
                                                   final String internetGateway) {
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        DetachInternetGatewayRequest detachInternetGatewayRequest = new DetachInternetGatewayRequest()
                .withVpcId(netId).withInternetGatewayId(internetGateway);
        DetachInternetGatewayResult result = ec2.detachInternetGateway(detachInternetGatewayRequest);
        System.out.println("IG detached successfully");
        return result != null && result.getSdkHttpMetadata().getHttpStatusCode() == 200;
    }

    public static List<InternetGateway> getAWSInternetGateway(final AWSStaticCredentialsProvider awsStaticCredentialsProvider, final String region) {
        List<InternetGateway> internetGatewayList = new ArrayList<>();
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        DescribeInternetGatewaysRequest describeInternetGatewaysRequest = new DescribeInternetGatewaysRequest();
        DescribeInternetGatewaysResult describeInternetGatewaysResult = null;
        do {
            if (describeInternetGatewaysResult != null && describeInternetGatewaysResult.getNextToken() != null) {
                describeInternetGatewaysRequest = new DescribeInternetGatewaysRequest().withNextToken(describeInternetGatewaysResult.getNextToken());
            }
            describeInternetGatewaysResult = ec2.describeInternetGateways(describeInternetGatewaysRequest);
            internetGatewayList.addAll(describeInternetGatewaysResult.getInternetGateways());
        } while (describeInternetGatewaysResult.getNextToken() != null);
        return internetGatewayList;
    }

    public static List<Vpc> getAWSVpc(final AWSStaticCredentialsProvider awsStaticCredentialsProvider, final String region) {
        List<Vpc> vpcList = new ArrayList<>();
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        DescribeVpcsRequest request = new DescribeVpcsRequest();
        DescribeVpcsResult response = null;
        do {
            if (response != null && response.getNextToken() != null) {
                request = new DescribeVpcsRequest().withNextToken(response.getNextToken());
            }
            response = ec2.describeVpcs(request);
            vpcList.addAll(response.getVpcs());
        } while (response.getNextToken() != null);
        return vpcList;
    }

    public static String createAWSVpc(final AWSStaticCredentialsProvider awsStaticCredentialsProvider, final String region,
                                      final String cidrBlock) {
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        CreateVpcRequest request = new CreateVpcRequest().withCidrBlock(cidrBlock);
        CreateVpcResult response = ec2.createVpc(request);
        System.out.println("network with cidrBlock-> " + cidrBlock + " and" + "  id->" + response.getVpc().getVpcId());
        return response.getVpc().getVpcId();
    }

    public static String createNetwork(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                       final String regionName, final String cidrBlock, final String networkName) {
        String networkId = checkNetwork(awsStaticCredentialsProvider, regionName, networkName);
        if (networkId == null) {
            networkId = createAWSVpc(awsStaticCredentialsProvider, regionName, cidrBlock);
            System.out.println("network with cidrBlock-> " + cidrBlock + " and" + "  id->" + networkId);
            createTagging(awsStaticCredentialsProvider, regionName, networkId, "Name", networkName);
            return networkId;
        }
        System.out.println("Network created with id as:- " + networkId + " having cidrBlock as-> " + cidrBlock);
        return networkId;
    }

    public static String checkNetwork(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                      final String regionName, final String networkName) {
        AtomicReference<String> id = new AtomicReference<>();
        getAWSVpc(awsStaticCredentialsProvider, regionName).parallelStream().forEach(vpc -> {
            if (vpc.getTags().parallelStream().anyMatch(tag -> tag.getKey().equals("Name") && tag.getValue().equals(networkName))) {
                id.getAndSet(vpc.getVpcId());
            }
        });
        return id.get();

    }

    public static boolean deleteNetwork(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                        final String regionName, final String vpcId) {
        String networkId = checkNetwork(awsStaticCredentialsProvider, regionName, vpcId);
        if (networkId != null) {
            getAWSInternetGateway(awsStaticCredentialsProvider, regionName).parallelStream().forEach(temp -> temp.getAttachments().parallelStream().forEach(attach -> {
                if (attach.getVpcId().equalsIgnoreCase(networkId)) {
                    detachAWSInternetGateway(awsStaticCredentialsProvider, regionName, networkId, temp.getInternetGatewayId());
                    deleteAWSInternetGateway(awsStaticCredentialsProvider, regionName, temp.getInternetGatewayId());
                }
            }));
            return deleteAWSVpc(awsStaticCredentialsProvider, regionName, networkId);
        } else {
            System.out.println("VPC with name:" + vpcId + " not present");
            return true;

        }

    }

    public static String createAWSSubnet(final AWSStaticCredentialsProvider awsStaticCredentialsProvider, final String region,
                                         final String vpcId, final String cidrBlock, final String zone) {
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        CreateSubnetRequest request = new CreateSubnetRequest().withVpcId(vpcId).withCidrBlock(cidrBlock)
                .withAvailabilityZone(zone);
        CreateSubnetResult response = ec2.createSubnet(request);
        System.out.println("subnet to networkWithId->" + vpcId + "  and with cidrBlock-> 10.0.0.16/28 and" + "  id->" + response.getSubnet().getSubnetId());
        return response.getSubnet().getSubnetId();
    }

    public static List<Subnet> getAWSSubnet(final AWSStaticCredentialsProvider awsStaticCredentialsProvider, final String region) {
        List<Subnet> subnetList = new ArrayList<>();
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        DescribeSubnetsRequest request = new DescribeSubnetsRequest();
        DescribeSubnetsResult response = null;
        do {
            if (response != null && response.getNextToken() != null) {
                request = new DescribeSubnetsRequest().withNextToken(response.getNextToken());
            }
            response = ec2.describeSubnets(request);
            subnetList.addAll(response.getSubnets());
        } while (response.getNextToken() != null);
        return subnetList;
    }

    public static String checkSubnet(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                     final String regionName, final String subnetName) {

        AtomicReference<String> id = new AtomicReference<>();
        getAWSSubnet(awsStaticCredentialsProvider, regionName).parallelStream().forEach(subnet -> {
            if (subnet.getTags().parallelStream().anyMatch(tag -> tag.getKey().equals("Name") && tag.getValue().equals(subnetName))) {
                id.getAndSet(subnet.getSubnetId());
            }
        });
        return id.get();
    }

    public static boolean deleteAWSSubnet(final AWSStaticCredentialsProvider awsStaticCredentialsProvider, final String region,
                                          final String subNetId) {
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        DeleteSubnetRequest request = new DeleteSubnetRequest().withSubnetId(subNetId);
        DeleteSubnetResult result = ec2.deleteSubnet(request);
        System.out.println("subnet id->" + subNetId + "  deleted");
        return result != null && result.getSdkHttpMetadata().getHttpStatusCode() == 200;
    }

    public static String deleteSubnet(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                      final String regionName, final String subnetName) {
        String subNetID = checkSubnet(awsStaticCredentialsProvider, regionName, subnetName);
        if (subNetID != null) {
            deleteAWSSubnet(awsStaticCredentialsProvider, regionName, subNetID);
        }
        return subNetID;
    }


    public static String createSubnet(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                      final String regionName, final String networkCidrBlock, final String subnetCidrBlock, final String networkName,
                                      final String subnetName, final String zone) {
        String subNetID = checkSubnet(awsStaticCredentialsProvider, regionName, subnetName);
        if (subNetID == null) {
            String vpcId = createNetwork(awsStaticCredentialsProvider, regionName, networkCidrBlock, networkName);
            createIGAndAttachIG(awsStaticCredentialsProvider, regionName, vpcId);
            subNetID = createAWSSubnet(awsStaticCredentialsProvider, regionName, vpcId, subnetCidrBlock, zone);

            System.out.println("subnet created with id :- " + subNetID + " having cidrBlock as -> " + subnetCidrBlock +
                    " which is attached to network with id as -> " + vpcId);
            if (createTagging(awsStaticCredentialsProvider, regionName, subNetID, "Name", subnetName)) {
                return subNetID;
            }
            return subNetID;
        }
        return subNetID;
    }

    public static void main(String[] args) {
        //createNetwork();
    }
}
