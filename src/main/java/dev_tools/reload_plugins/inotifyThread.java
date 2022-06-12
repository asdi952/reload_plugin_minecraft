package dev_tools.reload_plugins;

import java.nio.file.WatchService;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class inotifyThread  implements Runnable{

    public inotifyThread( Method _meth, Object _obj)
    {
        meth = _meth;
        obj = _obj;
    }
    public void start()
    {
        try{
            watcher = FileSystems.getDefault().newWatchService();
        }catch( Exception e){
            System.err.println("Erro watcher creating failed");
            e.printStackTrace();
            return;
        }

        running.set( true);
        th = new Thread( this);
        th.start();
    }
    public void stop()
    {
        running.set( false);
    }

    public WatchKey register( Path dir)
    {
        System.out.println( "register -" + dir.toAbsolutePath().toString());
        WatchKey key;
        try{
            key = dir.register( watcher, StandardWatchEventKinds.ENTRY_CREATE);
        }catch( Exception e){
            System.out.println( "Error on register oripath on watcher" + watcher.toString());
            e.printStackTrace();
            return null;
        }
        return key;
    }
    public void unregister( WatchKey key)
    {
        if( key.isValid())
            key.cancel();
    }

    public void run()
    {

        while( running.get())
        {
            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                WatchEvent<Path> ev = (WatchEvent<Path>)event;
                Path filename = ev.context();
                //watchedDirectory.resolve(createdFile);

                try{
                    meth.invoke(obj, key, filename);
                }catch(Exception e){
                    System.err.println( "err in inotifyTHread invokig mthod");
                    e.printStackTrace();
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                System.err.println("err key could not reset");
                running.set( false);
                break;
            }
        }
    }
    final AtomicBoolean running = new AtomicBoolean();
    Thread th;
    Method meth;
    Object obj;
    WatchService watcher;
}
