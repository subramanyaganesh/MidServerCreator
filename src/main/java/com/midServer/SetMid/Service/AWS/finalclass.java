package com.midServer.SetMid.Service.AWS;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.InstanceType;
import com.midServer.SetMid.Model.Constants;
import com.midServer.SetMid.Model.InstanceModel;
import com.midServer.SetMid.Model.ServerModel;
import com.midServer.SetMid.Service.ServerUtilService;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static com.midServer.SetMid.Service.InstanceService.getInstanceVersion;
import static com.midServer.SetMid.Service.InstanceService.setValuesToCommand;

public class finalclass {
    public static void main(String[] args) throws Exception {
        String AWS_ACCESS_KEY = "";
        String AWS_SECRET_KEY = "";
        String regionName = "us-west-2";
        String keyName = "keyName";
        String instanceName = "discoinstance";
        String username = "admin";
        String password = "Admin@123";
        String vnet = "midNet";
        String subnet = "midSub1";
        String vmname = "midvm";
        String vnetCIDR = "40.0.0.0/16";
        String subnetCIDR = "40.0.2.0/24";
        //final InstanceType VM_INSTANCE_TYPE = InstanceType.M2Xlarge;
        final InstanceType VM_INSTANCE_TYPE = InstanceType.T2Micro;
        String sshKeyPath = Utils.location + File.separator + keyName + ".pem";
        String MID_DISPLAY_NAME = "mymid1";
        AWSStaticCredentialsProvider awsStaticCredentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY));
        List<AvailabilityZone> listzone = Utils.getAWSZone(awsStaticCredentialsProvider, regionName).stream().filter(a -> a.getZoneName().contains(regionName)).collect(Collectors.toList());
        String ZONE = listzone.get(0).getZoneName();
        String subnetId = NetworkAndSubnet.createSubnet(awsStaticCredentialsProvider, regionName, vnetCIDR, subnetCIDR, vnet, subnet, ZONE);

        VmAndSSH.createInstance(awsStaticCredentialsProvider, regionName, subnetId, "", "", keyName, VM_INSTANCE_TYPE, vmname);
        InstanceModel instanceModel = InstanceModel.builder()
                .instanceName(instanceName)
                .instanceUsername(username)
                .password(password)
                .midserverName("autoMid")
                .midserverPassword("Midserver@123")
                .build();
        instanceModel.setInstanceUrl("https://" + instanceModel.getInstanceName() + ".service-now.com/");
        instanceModel.setVersion(getInstanceVersion(instanceModel));

        //String command = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(SetMidApplication.class.getClassLoader().getResource("config/WinMid")).toURI())));
        //deleteMidInstances(instanceModel, MID_DISPLAY_NAME);
        ServerModel serverModel = ServerModel.builder()
                .command(setValuesToCommand(instanceModel, 1, "ec2-user", MID_DISPLAY_NAME, Constants.command))
                .ipAddress("40.0.1.168")
                .serverUsername("ec2-user")
                .pemFilePath(sshKeyPath)
                .build();
        System.out.println("Command to be executed ::: " + serverModel.getCommand());
        String stringBuilder = new ServerUtilService().createServerSession(serverModel);
    }
}
