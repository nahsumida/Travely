public class ConnectionStartMessage implements Message {

    private boolean shouldStart;

    public ConnectionStartMessage(boolean start)
    {
        this.shouldStart = start;
    }

    public boolean getShouldStart()
    {
        return this.shouldStart;
    }
}