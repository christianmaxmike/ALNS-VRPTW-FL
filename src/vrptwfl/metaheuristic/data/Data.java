package vrptwfl.metaheuristic.data;

import java.util.ArrayList;
import java.util.List;

public class Data {

    private int[] xcoords;
    private int[] ycoords;
    private int[] demands;
    private int[] earliestStartTimes;
    private int[] latestStartTimes;
    private int[] serviceDurations;

    public int[] getXcoords() {
        return xcoords;
    }

    public Data(int[] xcoords, int[] ycoords, int[] demands, int[] earliestStartTimes, int[] latestStartTimes, int[] serviceDurations) {
        this.xcoords = xcoords;
        this.ycoords = ycoords;
        this.demands = demands;
        this.earliestStartTimes = earliestStartTimes;
        this.latestStartTimes = latestStartTimes;
        this.serviceDurations = serviceDurations;
    }

    public void setXcoords(int[] xcoords) {
        this.xcoords = xcoords;
    }

    public int[] getYcoords() {
        return ycoords;
    }

    public void setYcoords(int[] ycoords) {
        this.ycoords = ycoords;
    }

    public int[] getDemands() {
        return demands;
    }

    public void setDemands(int[] demands) {
        this.demands = demands;
    }

    public int[] getEarliestStartTimes() {
        return earliestStartTimes;
    }

    public void setEarliestStartTimes(int[] earliestStartTimes) {
        this.earliestStartTimes = earliestStartTimes;
    }

    public int[] getLatestStartTimes() {
        return latestStartTimes;
    }

    public void setLatestStartTimes(int[] latestStartTimes) {
        this.latestStartTimes = latestStartTimes;
    }

    public int[] getServiceDurations() {
        return serviceDurations;
    }

    public void setServiceDurations(int[] serviceDurations) {
        this.serviceDurations = serviceDurations;
    }

}
