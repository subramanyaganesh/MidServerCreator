package com.midServer.SetMid.Service.AWS;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class Utils {
    public static final InstanceType VM_INSTANCE_TYPE = InstanceType.T2Nano;
    public static final List<String> platform = Arrays.asList("amzn2-ami-hvm-2.0.20211005.0-x86_64-gp2");
    static String foldername = "AutoKey";
    public static final File location = new File(System.getProperty("user.home") + "/Documents/" + foldername);

    public static void createFolder() {
        if (!Files.exists(location.toPath())) {
            if (new File(location.getAbsolutePath()).mkdirs()) {
                System.out.println("Created new Folder :::" + location.getAbsolutePath());
            }
        }
    }
    public static List<AvailabilityZone> getAWSZone(final AWSStaticCredentialsProvider awsStaticCredentialsProvider, final String region) {
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        DescribeAvailabilityZonesResult zonesResponse = ec2.describeAvailabilityZones();
        return zonesResponse.getAvailabilityZones();
    }
    public static AmazonEC2Client getEC2Client(final String regionName, final AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
        AmazonEC2Client eC2Client = new AmazonEC2Client(awsStaticCredentialsProvider);
        if (regionName != null) {
            eC2Client.setEndpoint("ec2." + regionName + ".amazonaws.com");
        }
        return eC2Client;
    }

    public static boolean createTagging(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                        final String region,
                                        final String resourceName, final String tagName, final String tagValue) {
        Tag tag = new Tag().withKey(tagName).withValue(tagValue);
        CreateTagsRequest createTagsRequest = new CreateTagsRequest().withResources(resourceName).withTags(tag);
        CreateTagsResult result = getEC2Client(region, awsStaticCredentialsProvider).createTags(createTagsRequest);
        return result != null && result.getSdkHttpMetadata().getHttpStatusCode() == 200;
    }
}
