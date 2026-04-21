package synchronization;

public interface Consensus {
    // Propose value v and return agreed-upon value
    int decide(int v);
}
