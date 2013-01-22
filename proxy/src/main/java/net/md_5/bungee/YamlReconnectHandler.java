package net.md_5.bungee;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.yaml.snakeyaml.Yaml;

public class YamlReconnectHandler implements ReconnectHandler
{

    private final Yaml yaml = new Yaml();
    private final File file = new File("locations.yml");
    /*========================================================================*/
    private Map<String, String> data;

    @SuppressWarnings("unchecked")
    public YamlReconnectHandler()
    {
        try
        {
            file.createNewFile();
            try (FileReader rd = new FileReader(file))
            {
                data = yaml.loadAs(rd, Map.class);
            }
        } catch (IOException ex)
        {
            ProxyServer.getInstance().getLogger().log(Level.WARNING, "Could not load reconnect locations", ex);
        }

        if (data == null)
        {
            data = new ConcurrentHashMap<>();
        } else
        {
            data = new ConcurrentHashMap<>(data);
        }
    }

    @Override
    public String getServer(ProxiedPlayer player)
    {
        ListenerInfo listener = player.getPendingConnection().getListener();
        if (listener.isForceDefault())
        {
            return listener.getDefaultServer();
        }
        String forced = listener.getForcedHosts().get(player.getPendingConnection().getVirtualHost().getHostName());
        String server = (forced == null) ? data.get(key(player)) : forced;
        return (server != null) ? server : listener.getDefaultServer();
    }

    @Override
    public void setServer(ProxiedPlayer player)
    {
        data.put(key(player), player.getServer().getInfo().getName());
    }

    private String key(ProxiedPlayer player)
    {
        InetSocketAddress host = player.getPendingConnection().getVirtualHost();
        return player.getName() + ";" + host.getHostString() + ":" + host.getPort();
    }

    @Override
    public void save()
    {
        try (FileWriter wr = new FileWriter(file))
        {
            yaml.dump(data, wr);
        } catch (IOException ex)
        {
            ProxyServer.getInstance().getLogger().log(Level.WARNING, "Could not save reconnect locations", ex);
        }
    }
}
