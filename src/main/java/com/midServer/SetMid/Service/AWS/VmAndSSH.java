package com.midServer.SetMid.Service.AWS;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.midServer.SetMid.Service.AWS.Utils.*;

public class VmAndSSH {


    public static String createAWSSSHkeys(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                          final String region, final String keyName) throws Exception {
        try {
            final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
            List<KeyPairInfo> key = ec2.describeKeyPairs().getKeyPairs().parallelStream().filter(keyPairInfo -> keyPairInfo.getKeyName().equalsIgnoreCase(keyName)).collect(Collectors.toList());
            if (!key.isEmpty()) deleteAWSSSHkeys(awsStaticCredentialsProvider, region, keyName);
            CreateKeyPairResult result = ec2.createKeyPair(new CreateKeyPairRequest().withKeyName(keyName));
            System.out.println("KeyPair created successfully");
            createFolder();
            Files.deleteIfExists(new File(Utils.location + File.separator + keyName + ".pem").toPath());
            BufferedWriter writer = new BufferedWriter(new FileWriter(Utils.location + File.separator + keyName + ".pem"));
            writer.write(result.getKeyPair().getKeyMaterial());
            writer.flush();
            writer.close();
            return result.getKeyPair().getKeyMaterial();
        } catch (Exception e) {
            System.out.println("try a different name");
            System.out.println("The exception is : " + e.getMessage());
            throw e;
        }
    }

    public static boolean deleteAWSSSHkeys(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                           final String region, final String keyName) {
        DeleteKeyPairResult result = getEC2Client(region, awsStaticCredentialsProvider).deleteKeyPair(new DeleteKeyPairRequest(keyName));
        System.out.println("Deleted KeyPair successfully");
        return result != null && result.getSdkHttpMetadata().getHttpStatusCode() == 200;
    }


    public static List<Image> getAWSImages(final AWSStaticCredentialsProvider awsStaticCredentialsProvider, final String region,
                                           final Filter[] imageFilter) {
        final AmazonEC2 ec2 = getEC2Client(region, awsStaticCredentialsProvider);
        DescribeImagesRequest request = new DescribeImagesRequest();
        if (imageFilter != null) {
            request = new DescribeImagesRequest().withFilters(imageFilter);
        }
        DescribeImagesResult response = ec2.describeImages(request);
        return response.getImages();
    }

    public static String createInstance(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                        final String regionName, final String subnetId, final String securityGroupName,
                                        final String securityDescription, final String keyPairName,
                                        final InstanceType instanceType, final String vmName) {
        try {

//Amazon Linux 2 AMI (HVM), SSD Volume Type - ami-0e5b6b6a9f3db6db8 (64-bit x86) / ami-0fceb10a0f8f300b2 (64-bit Arm)
//Red Hat Enterprise Linux 8 (HVM), SSD Volume Type - ami-0b28dfc7adc325ef4 (64-bit x86) / ami-07465754c59218cdb (64-bit Arm)
            /*new AWSEventSecurityGroupUtils().checkAndCreateSecurityGroup(awsStaticCredentialsProvider, regionName,
                    securityGroupName, securityDescription);*/
            System.out.println(" creating a new KeyPair ");
            createAWSSSHkeys(awsStaticCredentialsProvider, regionName, keyPairName);

            //running the instance
            Filter[] filters = {
                    new Filter("architecture", Collections.singletonList("x86_64")),
                    new Filter("is-public", Collections.singletonList("true")),
                    new Filter("name", Utils.platform)
            };
            String imageID = getAWSImages(awsStaticCredentialsProvider, regionName, filters).get(0).getImageId();

            List<Instance> instanceList = runAWSVMInstance(awsStaticCredentialsProvider, regionName,
                    imageID, instanceType, keyPairName, subnetId, securityGroupName);
            String instanceId = instanceList.get(0).getInstanceId();
            createTagging(awsStaticCredentialsProvider, regionName, instanceId, "Name", vmName);
            System.out.println("VM created successfully with id:::" + instanceId);
            while (!instanceList.get(0).getState().getName().equals("Running")) {
                TimeUnit.SECONDS.sleep(10);
            }
            TimeUnit.SECONDS.sleep(30);
            System.out.println("VM is up and running");
            return instanceList.get(0).getPublicIpAddress();
        } catch (Exception error) {
            System.out.println("The exception in createInstance : " + error.getMessage());
        }
        return null;

    }

    public static List<Instance> runAWSVMInstance(final AWSStaticCredentialsProvider awsStaticCredentialsProvider,
                                                  final String region, final String imageId, final InstanceType instanceType,
                                                  final String keyName, final String subnetId, final String sgName) {
        //running the instance

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest
                .withImageId(imageId)
                .withInstanceType(instanceType)
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(keyName)
                .withNetworkInterfaces(new InstanceNetworkInterfaceSpecification().withDeviceIndex(0).withAssociatePublicIpAddress(true).withSubnetId(subnetId))
                //.withSecurityGroups(sgName)
        ;
        RunInstancesResult runInstancesResult = AmazonEC2ClientBuilder.standard()
                .withCredentials(awsStaticCredentialsProvider)
                .withRegion(region)
                .build().runInstances(runInstancesRequest);
        return runInstancesResult.getReservation().getInstances();
    }

}
