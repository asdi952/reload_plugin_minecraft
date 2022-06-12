package dev_tools.reload_plugins;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.nio.file.WatchKey;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

public class pluginManager {

    public pluginManager(Path _pluginsFile_path, Path _copyPlugins_dir)
    {
        pluginsFile_path = _pluginsFile_path;
        copyPlugins_dir = _copyPlugins_dir;

        if( ! startInotify())
            return;

        load_plugins();
    }

    protected void finalize ()
    {
        stopInotify();
    }
    private boolean startInotify()
    {
        Method m;
        try{
            m = this.getClass().getDeclaredMethod("inotifyCallable", WatchKey.class, Path.class);
        }catch( Exception e){
            System.err.println("An error occurred, start inotify");
            e.printStackTrace();
            return false;
        }

        inotify = new inotifyThread( m, this);
        inotify.start();
        return true;
    }
    private void stopInotify()
    {
        inotify.stop();
    }
    public void inotifyCallable( WatchKey key, Path filename)
    {
        System.out.println("inoCAll");
        //if( filename.compareTo( Paths.get("target")) != 0)
        myPlugin plug = Keys.get(key);
        if(plug == null)
            return;

        plug.inotifyCallable( key, filename, this);
    }
    public void clearPlugins()
    {
        //unload_plugins();
        Bukkit.getPluginManager().clearPlugins();
    }
    public void restart()
    {
        unload_plugins();
        load_plugins();
    }


    public void reloadFull()
    {
        plugins.forEach( plugs -> {
            if( ! plugs.reloadFull())
                System.err.println("Erro unable to fullReload " + plugs.oriPath.toString());

        });
    }
    public void reloadSimple()
    {
        plugins.forEach( plugs -> plugs.reloadSimple());
    }

    public void enableAll()
    {
        plugins.forEach( plug -> plug.enable());
    }
    public void disableAll()
    {
        plugins.forEach( plug -> plug.disable());
    }
    private void load_plugins()
    {
        try {
            File myObj = new File( pluginsFile_path.toString());
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String plug_path = myReader.nextLine();
                System.out.println("File/plug loading: " + plug_path);

                Path ori = Paths.get( plug_path);
                Path copy = Paths.get( copyPlugins_dir.toString(), ori.getFileName().toString());
                //System.out.println( ori.toString());
                //System.out.println( copy.toString());
                myPlugin aux = new myPlugin( ori, copy);

                if( ! aux.load( this)){
                    System.out.println("Plugin: " + aux.oriPath.toString() + " not missing");
                    continue;
                }

                plugins.add( aux);
            }
            myReader.close();
        } catch (Exception e) {
            System.err.println("An error occurred, reading plugin files");
            e.printStackTrace();
        }
    }

    private void unload_plugins()
    {
        plugins.forEach( plug -> plug.unload( this));
        plugins.clear();
    }

    Path pluginsFile_path;
    Path copyPlugins_dir;
    Vector< myPlugin> plugins = new Vector();
    HashMap<WatchKey, myPlugin> Keys = new HashMap<WatchKey,myPlugin>();

    inotifyThread inotify;
}
