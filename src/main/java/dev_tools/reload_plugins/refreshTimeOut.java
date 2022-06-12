package dev_tools.reload_plugins;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class refreshTimeOut implements Runnable{
    public refreshTimeOut( ) {
        running.set( false);
    }

    public void start( Runnable _action, int milli)
    {
        synchronized ( mutex)
        {
            if( running.get())
            {
                refresh_timer();
            }
            else
            {
                action = _action;
                period = milli;

                running.set(true);
                th = new Thread( this);
                th.start();
            }
        }
    }
    public void stop()
    {
        running.set( false);
    }

    public void refresh_timer()
    {
        refresh.set( true);
    }

    public void run()
    {
        while( running.get())
        {
            refresh.set( false);
            try{ Thread.sleep(period); } catch( Exception e){
                System.err.println("err in run");
                e.printStackTrace();
            }

            synchronized ( mutex)
            {
                if( ! refresh.get())
                {
                    action.run();
                    if( ! refresh.get()) // case in run() has call function refresh_timer, will give loop again
                        running.set(false);
                }
            }
        }
        System.err.println("timout run");
    }
    private Thread th;
    private Runnable action;
    private int period;
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean refresh = new AtomicBoolean();
    private Object mutex = new Object();
}
