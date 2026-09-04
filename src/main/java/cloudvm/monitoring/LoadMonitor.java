package cloudvm.monitoring;

import org.cloudsimplus.vms.Vm;

import java.util.List;

public class LoadMonitor {

    // CPU utilization threshold
    private static final double OVERLOAD_THRESHOLD = 0.80;

    /**
     * Prints the current CPU utilization of every VM.
     */
    public static void printVmLoads(List<Vm> vmList) {

        System.out.println();
        System.out.println("======================================");
        System.out.println(" VM LOAD MONITORING");
        System.out.println("======================================");

        for (Vm vm : vmList) {

            double cpuLoad = vm.getCpuPercentUtilization();

            System.out.printf(
                    "VM %d -> CPU Load: %.2f%%%n",
                    vm.getId(),
                    cpuLoad * 100
            );
        }
    }

    /**
     * Checks whether a VM is overloaded.
     */
    public static boolean isOverloaded(Vm vm) {

        double cpuLoad = vm.getCpuPercentUtilization();

        return cpuLoad >= OVERLOAD_THRESHOLD;
    }

    /**
     * Prints overloaded VMs.
     */
    public static void printOverloadedVms(List<Vm> vmList) {

        System.out.println();
        System.out.println("======================================");
        System.out.println(" OVERLOADED VMs");
        System.out.println("======================================");

        boolean found = false;

        for (Vm vm : vmList) {

            if (isOverloaded(vm)) {

                System.out.printf(
                        "VM %d -> OVERLOADED (%.2f%%)%n",
                        vm.getId(),
                        vm.getCpuPercentUtilization() * 100
                );

                found = true;
            }
        }

        if (!found) {
            System.out.println("No overloaded VMs detected.");
        }
    }
}