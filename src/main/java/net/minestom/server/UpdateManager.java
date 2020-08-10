package net.minestom.server;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.EntityManager;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.play.KeepAlivePacket;
import net.minestom.server.thread.PerGroupChunkProvider;
import net.minestom.server.thread.ThreadProvider;
import net.minestom.server.utils.thread.MinestomThread;
import net.minestom.server.utils.validate.Check;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public final class UpdateManager {

    private static final long KEEP_ALIVE_DELAY = 10_000;
    private static final long KEEP_ALIVE_KICK = 30_000;
    private static final ColoredText TIMEOUT_TEXT = ColoredText.of(ChatColor.RED + "Timeout");

    private ExecutorService mainUpdate = new MinestomThread(1, MinecraftServer.THREAD_NAME_MAIN_UPDATE);
    private boolean stopRequested;

    private ThreadProvider threadProvider;

    private ArrayList<Runnable> tickCallbacks = new ArrayList<>();

    {
        //threadProvider = new PerInstanceThreadProvider();
        threadProvider = new PerGroupChunkProvider();
    }

    /**
     * Should only be created in MinecraftServer
     */
    protected UpdateManager() {
    }

    /**
     * Start the server loop in the update thread
     */
    protected void start() {
        mainUpdate.execute(() -> {

            final ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
            final EntityManager entityManager = MinecraftServer.getEntityManager();

            final long tickDistance = MinecraftServer.TICK_MS * 1000000;
            long currentTime;
            while (!stopRequested) {
                currentTime = System.nanoTime();
                final long time = System.currentTimeMillis();

                //Tick Callbacks
                tickCallbacks.forEach(Runnable::run);

                // Server tick (instance/chunk/entity)
                threadProvider.update(time);

                // Waiting players update (newly connected waiting to get into the server)
                entityManager.updateWaitingPlayers();

                // Keep Alive Handling
                final KeepAlivePacket keepAlivePacket = new KeepAlivePacket(time);
                for (Player player : connectionManager.getOnlinePlayers()) {
                    final long lastKeepAlive = time - player.getLastKeepAlive();
                    if (lastKeepAlive > KEEP_ALIVE_DELAY && player.didAnswerKeepAlive()) {
                        player.refreshKeepAlive(time);
                        player.getPlayerConnection().sendPacket(keepAlivePacket);
                    } else if (lastKeepAlive >= KEEP_ALIVE_KICK) {
                        player.kick(TIMEOUT_TEXT);
                    }
                }

                // Sleep until next tick
                long sleepTime = (tickDistance - (System.nanoTime() - currentTime)) / 1000000;
                sleepTime = Math.max(1, sleepTime);

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     * Get the current thread provider
     *
     * @return the current thread provider
     */
    public ThreadProvider getThreadProvider() {
        return threadProvider;
    }

    /**
     * Change the server thread provider
     *
     * @param threadProvider the new thread provider
     * @throws NullPointerException if {@param threadProvider} is null
     */
    public void setThreadProvider(ThreadProvider threadProvider) {
        Check.notNull(threadProvider, "The thread provider cannot be null");
        this.threadProvider = threadProvider;
    }

    /**
     * Signal the thread provider that a chunk has been loaded
     *
     * @param instance the instance of the chunk
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     */
    public synchronized void signalChunkLoad(Instance instance, int chunkX, int chunkZ) {
        if (this.threadProvider == null)
            return;
        this.threadProvider.onChunkLoad(instance, chunkX, chunkZ);
    }

    /**
     * Signal the thread provider that a chunk has been unloaded
     *
     * @param instance the instance of the chunk
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     */
    public synchronized void signalChunkUnload(Instance instance, int chunkX, int chunkZ) {
        if (this.threadProvider == null)
            return;
        this.threadProvider.onChunkUnload(instance, chunkX, chunkZ);
    }

    public void addTickCallback(Runnable callback) {
        tickCallbacks.add(callback);
    }

    public void removeTickCallback(Runnable callback) {
        tickCallbacks.remove(callback);
    }

    /**
     * Stop the server loop
     */
    public void stop() {
        stopRequested = true;
    }
}
