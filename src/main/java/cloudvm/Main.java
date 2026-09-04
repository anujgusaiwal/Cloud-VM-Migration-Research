package cloudvm;
import cloudvm.monitoring.LoadMonitor;

import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;

import java.util.ArrayList;
import java.util.List;

public class Main {

    // ==============================
    // SIMULATION CONFIGURATION
    // ==============================

    private static final int HOSTS = 5;
    private static final int VMS = 10;
    private static final int CLOUDLETS = 20;

    private static final int HOST_PES = 4;
    private static final long HOST_MIPS = 1000;

    private static final int VM_PES = 1;
    private static final long VM_MIPS = 1000;

    private CloudSimPlus simulation;
    private Datacenter datacenter;
    private DatacenterBroker broker;

    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;


    public static void main(String[] args) {

        Main simulation = new Main();

        simulation.start();

    }


    public Main() {

        // Create CloudSim simulation
        simulation = new CloudSimPlus();

        // Create cloud infrastructure
        datacenter = createDatacenter();

        // Create broker
        broker = new DatacenterBrokerSimple(simulation);

        // Create VMs
        vmList = createVMs();

        // Create tasks
        cloudletList = createCloudlets();

        // Submit VMs
        broker.submitVmList(vmList);

        // Submit tasks
        broker.submitCloudletList(cloudletList);
    }


    public void start() {

        System.out.println("======================================");
        System.out.println(" Cloud VM Migration Research Project");
        System.out.println("======================================");

        System.out.println();

        System.out.println("Starting simulation...");

        simulation.start();

        System.out.println();

        System.out.println("Simulation finished.");

        // VM load monitoring
        LoadMonitor.printVmLoads(vmList);

        // Overload detection
        LoadMonitor.printOverloadedVms(vmList);

        System.out.println();

        printResults();
    }


    // =========================================
    // CREATE DATACENTER
    // =========================================

    private Datacenter createDatacenter() {

        List<Host> hostList = new ArrayList<>();

        for (int i = 0; i < HOSTS; i++) {

            Host host = createHost();

            hostList.add(host);
        }

        return new DatacenterSimple(simulation, hostList);
    }


    // =========================================
    // CREATE HOST
    // =========================================

    private Host createHost() {

        List<Pe> peList = new ArrayList<>();

        for (int i = 0; i < HOST_PES; i++) {

            peList.add(
                    new PeSimple(HOST_MIPS)
            );
        }

        long ram = 8192;        // MB
        long bandwidth = 10000; // Mbps
        long storage = 1_000_000; // MB

        return new HostSimple(
                ram,
                bandwidth,
                storage,
                peList
        );
    }


    // =========================================
    // CREATE VIRTUAL MACHINES
    // =========================================

    private List<Vm> createVMs() {

        List<Vm> list = new ArrayList<>();

        for (int i = 0; i < VMS; i++) {

            Vm vm = new VmSimple(
                    VM_MIPS,
                    VM_PES
            );

            vm.setRam(1024);
            vm.setBw(1000);
            vm.setSize(10_000);

            list.add(vm);
        }

        return list;
    }


    // =========================================
    // CREATE CLOUDLETS / TASKS
    // =========================================

    private List<Cloudlet> createCloudlets() {

        List<Cloudlet> list = new ArrayList<>();

        UtilizationModelFull utilization =
                new UtilizationModelFull();

        for (int i = 0; i < CLOUDLETS; i++) {

            long length = 10_000;

            Cloudlet cloudlet =
                    new CloudletSimple(
                            length,
                            1
                    );

            cloudlet.setUtilizationModelCpu(utilization);

            cloudlet.setFileSize(1024);
            cloudlet.setOutputSize(1024);

            list.add(cloudlet);
        }

        return list;
    }


    // =========================================
    // PRINT RESULTS
    // =========================================

    private void printResults() {

        System.out.println("--------------------------------------");
        System.out.println("Simulation Results");
        System.out.println("--------------------------------------");

        System.out.println(
                "Hosts       : " + HOSTS
        );

        System.out.println(
                "VMs         : " + VMS
        );

        System.out.println(
                "Cloudlets   : " + CLOUDLETS
        );

        System.out.println();

        System.out.println(
                "Completed Cloudlets : "
                        + broker.getCloudletFinishedList().size()
        );

        System.out.println();

        System.out.println("Simulation completed successfully.");
    }
}