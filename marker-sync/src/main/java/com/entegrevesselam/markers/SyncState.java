package com.entegrevesselam.markers;
import java.util.Objects;
/** Main-thread state; the snapshot cache and the applied context are deliberately separate. */
final class SyncState {
    record Context(String server, String dimension, String realm) {}
    record Request(long epoch) {}
    private Context context, appliedContext;
    private long epoch, revision = -1, appliedRevision = -1, lastStart = Long.MIN_VALUE;
    private Request inFlight;
    private Object connection;
    private boolean authorized, closed, invitationOffered;
    void context(Context value) {
        if (!Objects.equals(context, value)) { context = value; epoch++; }
        if (value == null) { authorized = false; revision = -1; appliedContext = null; }
    }
    Request begin(long now, boolean foreground, boolean token) {
        if (closed || context == null || !foreground || !token || inFlight != null
            || (lastStart != Long.MIN_VALUE && now - lastStart < 60000)) return null;
        lastStart = now;
        return inFlight = new Request(epoch);
    }
    private boolean finish(Request request) {
        if (request == null || request != inFlight) return false;
        inFlight = null;
        return !closed && context != null && request.epoch == epoch;
    }
    boolean accept(Request request, long nextRevision) {
        if (!finish(request) || nextRevision < revision || nextRevision < 0) return false;
        revision = nextRevision; authorized = true; return true;
    }
    void failed(Request request) { finish(request); }
    void denied(Request request) {
        if (finish(request)) { authorized = false; appliedContext = null; }
    }
    boolean needsApply() { return visible() && (!Objects.equals(context, appliedContext) || revision != appliedRevision); }
    void applied() { appliedContext = context; appliedRevision = revision; }
    boolean visible() { return !closed && context != null && authorized; }
    void connection(Object value) { if (connection != value) { connection = value; invitationOffered = false; } }
    boolean syncing() { return inFlight != null; }
    boolean offerPairing(boolean foreground, boolean paired, boolean pending) {
        if (closed || context == null || !foreground || paired || pending || invitationOffered) return false;
        invitationOffered = true;
        return true;
    }
    void resetDevice() { epoch++; authorized = false; revision = -1; appliedContext = null; lastStart = Long.MIN_VALUE; }
    void close() { closed = true; epoch++; authorized = false; }
}
