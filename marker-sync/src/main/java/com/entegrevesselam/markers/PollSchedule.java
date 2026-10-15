package com.entegrevesselam.markers;
/** Main-thread scheduler with an injected clock value, never a sleeping game thread. */
final class PollSchedule {
    private long next; private final long expiry; private boolean busy;
    PollSchedule(long start, long expiry) { next = start + 5000; this.expiry = expiry; }
    boolean begin(long now, boolean foreground, boolean target) {
        if (busy || !foreground || !target || now < next || now >= expiry) return false;
        next = now + 5000; busy = true; return true;
    }
    void complete() { busy = false; }
}
