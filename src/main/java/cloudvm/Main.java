package cloudvm;

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

    // =========================================
    // SIMULATION CONFIGURATION
    // =========================================

    private static final int HOSTS = 5;
    private static final int VMS = 10;
    private static final int CLOUDLETS = 20;

    private static final int HOST_PES = 4;
    private static final long HOST_MIPS = 1000;

    private static final int VM_PES = 1;
    private static final long VM_MIPS = 1000;

    // Overload threshold = 80%
    private static final double OVERLOAD_THRESHOLD = 0.80;

    // How often to print VM load
    private static final double MONITOR_INTERVAL = 5.0;

    private CloudSimPlus simulation;
    private Datacenter datacenter;
    private DatacenterBroker broker;

    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;

    private double lastMonitorTime = -1;


    // =========================================
    // MAIN
    // =========================================

    public static void main(String[] args) {

        Main simulation = new Main();

        simulation.start();
    }


    // =========================================
    // CONSTRUCTOR
    // =========================================

    public Main() {

        // Create CloudSim simulation
        simulation = new CloudSimPlus();

        // Create cloud infrastructure
        datacenter = createDatacenter();

        // Create broker
        broker = new DatacenterBrokerSimple(simulation);

        // Create VMs
        vmList = createVMs();

        // Create imbalanced workload
        cloudletList = createCloudlets();

        // Submit VMs
        broker.submitVmList(vmList);

        // Submit Cloudlets
        broker.submitCloudletList(cloudletList);

        // Enable runtime monitoring
        addLoadMonitoring();
    }


    // =========================================
    // START SIMULATION
    // =========================================

    public void start() {

        System.out.println("======================================");
        System.out.println(" Cloud VM Migration Research Project");
        System.out.println("======================================");

        System.out.println();

        System.out.println("Starting simulation...");

        simulation.start();

        System.out.println();

        System.out.println("Simulation finished.");

        System.out.println();

        printResults();
    }


    // =========================================
    // RUNTIME VM LOAD MONITORING
    // =========================================

    private void addLoadMonitoring() {

        simulation.addOnClockTickListener(info -> {

            double currentTime = info.getTime();

            /*
             * Print VM loads approximately every 5 seconds.
             *
             * We use >= instead of:
             *
             * currentTime % 5 == 0
             *
             * because simulation time is represented
             * using floating-point values.
             */

            if (currentTime >= lastMonitorTime + MONITOR_INTERVAL) {

                lastMonitorTime = currentTime;

                System.out.println();
                System.out.println("--------------------------------------");

                System.out.printf(
                        "VM LOAD AT SIMULATION TIME %.2f%n",
                        currentTime
                );

                System.out.println("--------------------------------------");

                for (Vm vm : vmList) {

                    double cpuUtilization =
                            vm.getCpuPercentUtilization();

                    System.out.printf(
                            "VM %d -> CPU Load: %.2f%%",
                            vm.getId(),
                            cpuUtilization * 100
                    );

                    if (cpuUtilization >= OVERLOAD_THRESHOLD) {
                        System.out.print("  <-- OVERLOADED");
                    }

                    System.out.println();
                }
            }
        });
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

        return new DatacenterSimple(
                simulation,
                hostList
        );
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

        long ram = 8192;            // MB
        long bandwidth = 10000;     // Mbps
        long storage = 1_000_000;   // MB

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
    // CREATE IMBALANCED CLOUDLETS
    // =========================================

    private List<Cloudlet> createCloudlets() {

        List<Cloudlet> list = new ArrayList<>();

        /*
         * We intentionally create an imbalanced workload.
         *
         * VM 0 -> 10 heavy tasks
         * VM 1 -> 4 medium tasks
         * VM 2 -> 3 light tasks
         * VM 3 -> 2 very light tasks
         * VM 4 -> 1 smallest task
         *
         * VM 5-9 -> No tasks
         *
         * This gives us an intentionally imbalanced
         * workload for studying load balancing.
         */

        for (int i = 0; i < CLOUDLETS; i++) {

            long length;

            Vm targetVm;

            if (i < 10) {

                // Heavy workload
                length = 20_000;

                targetVm = vmList.get(0);

            } else if (i < 14) {

                // Medium workload
                length = 10_000;

                targetVm = vmList.get(1);

            } else if (i < 17) {

                // Light workload
                length = 5_000;

                targetVm = vmList.get(2);

            } else if (i < 19) {

                // Very light workload
                length = 3_000;

                targetVm = vmList.get(3);

            } else {

                // Smallest workload
                length = 1_000;

                targetVm = vmList.get(4);
            }

            Cloudlet cloudlet =
                    new CloudletSimple(
                            length,
                            1
                    );

            // Cloudlet uses CPU
            cloudlet.setUtilizationModelCpu(
                    new UtilizationModelFull()
            );

            cloudlet.setFileSize(1024);
            cloudlet.setOutputSize(1024);

            /*
             * Explicitly assign this task
             * to the selected VM.
             */
            cloudlet.setVm(targetVm);

            list.add(cloudlet);
        }

        return list;
    }


    // =========================================
    // PRINT FINAL RESULTS
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

        System.out.println(
                "Simulation completed successfully."
        );
    }
}