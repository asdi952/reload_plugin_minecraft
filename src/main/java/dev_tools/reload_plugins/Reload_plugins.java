package dev_tools.reload_plugins;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Reload_plugins extends JavaPlugin {

    @Override
    public void onEnable() {

        Path aux = Paths.get( Bukkit.getServer().getWorldContainer().getAbsolutePath());
        Path pluginsFile_path = Paths.get( aux.getParent().toString(), "plugins_file.txt");
        Path copy_plugins_dir = Paths.get( pluginsFile_path.getParent().toString(), "copy_plugins");

        if( ! checkFiles( pluginsFile_path)){
            System.err.println("Err with checkFiles pluginsFile_path");
            return ;
        }
        if( ! checkFolders( copy_plugins_dir)){
            System.err.println("Err with checkFolders copy_plugins_dir");
            return ;
        }

        System.out.println( "enable - reload_plugin ");
        plugMan = new pluginManager( pluginsFile_path, copy_plugins_dir);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if( sender instanceof Player)
        {
            switch (command.getName())
            {
                case "myReload":
                {
                    sendMessage( (Player)sender, "full reload all plugins");
                    plugMan.reloadFull();
                    break;
                }
                case "myRestart":{
                    sendMessage( (Player)sender, "restarted plugins, no idea if works");
                    plugMan.restart();
                    break;
                }
                case "myPlugins":
                {
                    switch (args[0])
                    {
                        case "enable":
                        {
                            sendMessage( (Player)sender, "enabled all plugins");
                            plugMan.enableAll();
                            break;
                        }
                        case "disable":
                        {
                            sendMessage( (Player)sender, "disabled all plugins");
                            plugMan.disableAll();
                            break;
                        }
                    }
                    break;
                }
                case "myClear":
                {
                    plugMan.clearPlugins();
                    break;
                }

            }
        }


        return true;
    }

    void sendMessage( Player player, String msg)
    {
        String aux = player.getName() + ": " + msg;
        System.out.println( aux);
        player.sendMessage( aux);
    }

    private boolean checkFiles( Path path)
    {
        File file = new File( path.toString());
        if( ! file.exists())
        {
            try{
                file.createNewFile();
            }catch( Exception e){
                System.err.println("Error check file");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    private boolean checkFolders( Path path)
    {
        boolean res = true;
        File file = new File( path.toString());
        if( ! file.exists())
        {
            res =file.mkdirs();
        }
        return res;
    }

    pluginManager plugMan;
}
