package dev_tools.reload_plugins;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import java.io.File;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;

public class myPlugin{
    public myPlugin(Path _oriPath, Path _copyPath){
        oriPath = _oriPath;
        copyPath = _copyPath;
    }

    public boolean load(pluginManager man)
    {
        File ori = oriPath.toFile(); // checking if file exists
        if( ! ori.exists())
        {
            System.err.println("FIle: " + ori + " does not exist");
            return false;
        }

        if( ! refresh_files()) // duplicate files to copy_folder..
            return false;

        File copy = copyPath.toFile();
        try{
            plug = Bukkit.getPluginManager().loadPlugin( copy);
            Bukkit.getPluginManager().enablePlugin( plug);
        }
        catch(Exception e){
            System.err.println( "Error on load plugin" );
            e.printStackTrace();
        }

        //firstKey = register_plug( oriPath.getParent().getParent(), man);

        if( ! register_firstKey( man, oriPath.getParent().getParent()))
            return false;

        return true;
    }
    public void unload(pluginManager man)
    {
        unregister_plug( man);
        disable(); // there's no unload in bukkit so use disable, makeshift
    }

    private void unregister_plug( pluginManager man)
    {
        unregister_firstKey( man);
        //unregister_secondKey( man);
    }

    /*public void inotifyCallable( WatchKey key, Path filename, pluginManager man)
    {

        if( key == firstKey)
        {
            System.err.println("enter firstkey");
            if( filename.compareTo( Paths.get("target")) != 0)
                return;
            System.err.println("pass firstkey");
            if( ! register_secondKey( man, oriPath.getParent()))
                return;
        }
        else if( key == secondKey)
        {
            System.err.println("enter secondkey");
            if( filename.compareTo( oriPath.getFileName()) != 0)
                return;
            System.err.println("pass secondkey");

            timeOut.start(()->{
                unregister_secondKey( man);
                reloadFull();
                System.err.println("Ended timout");
            }, 200);

        }
    }*/
    public void inotifyCallable( WatchKey key, Path filename, pluginManager man)
    {
        if( filename.compareTo( Paths.get("target")) != 0)
            return;

        callableTicks.set( 20);
        timeOut.start(()->{
            File ori =oriPath.toFile();
            if(  ori.exists())
            {
                reloadFull();
            }else
            {
                System.out.println("inotifyCallable - file does not exist");
                if( callableTicks.decrementAndGet() == 0)
                    return;

                timeOut.refresh_timer();
            }

        }, 500);
    }
    AtomicInteger callableTicks = new AtomicInteger();

    private boolean register_firstKey( pluginManager man, Path path)
    {
        if(( firstKey = man.inotify.register( path)) == null)
            return false;

        man.Keys.put( firstKey, this);
        return true;
    }
    /*private boolean register_secondKey( pluginManager man, Path path)
    {
        if(( secondKey = man.inotify.register( path)) == null)
            return false;

        man.Keys.put( secondKey, this);
        return true;
    }*/
    private void unregister_firstKey( pluginManager man)
    {
        if( ! firstKey.isValid())
            return;

        man.Keys.remove( firstKey);
        man.inotify.unregister( firstKey);
    }
    /*private void unregister_secondKey( pluginManager man)
    {
        if( ! secondKey.isValid())
            return;

        man.Keys.remove( secondKey);
        man.inotify.unregister( secondKey);
    }*/

    public void enable()
    {
        Bukkit.getPluginManager().enablePlugin( plug);
    }
    public void disable()
    {
        Bukkit.getPluginManager().disablePlugin( plug);
    }

    public boolean reloadSimple()
    {
        boolean res = true;
        disable();
        try{
            plug = Bukkit.getPluginManager().loadPlugin( copyPath.toFile());
        }
        catch(Exception e){
            System.err.println( "Error on reloadSimple" );
            e.printStackTrace();
            res = false;
        }
        enable();

        return res;
    }
    public boolean reloadFull()
    {
        boolean res = true;

        disable();
        //unregister_secondKey( man);
        if( refresh_files())
        {

            //unregister_plug( man);
            try{
                plug = Bukkit.getPluginManager().loadPlugin( copyPath.toFile());
            }
            catch(Exception e){
                System.err.println( "Error on reloadfull" );
                e.printStackTrace();
                res = false;
            }
            System.err.println( "refred files in reload full");
        }
        enable();

        System.out.println( "fullreload -> " + oriPath.toString() + " " + res);
        return res;
    }

    private boolean refresh_files()
    {
        try{
            Files.copy(oriPath, copyPath, StandardCopyOption.REPLACE_EXISTING);
        }catch( Exception e){
            System.out.println("Error on refresh files");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    Plugin plug;
    Path oriPath;
    Path copyPath;
    WatchKey firstKey;
    WatchKey secondKey;
    refreshTimeOut timeOut = new refreshTimeOut();
}