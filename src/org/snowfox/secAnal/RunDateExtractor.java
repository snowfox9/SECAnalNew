package org.snowfox.secAnal;

import org.snowfox.secAnal.runnable.DisposalDateCollector;

public class RunDateExtractor {

    public static void main(String[] args)
    {
        Runnable r = new DisposalDateCollector();
        Thread t = new Thread(r);

        t.run();

        while(true)
        {
            if(!t.isAlive())
            {
                break;
            }
        }
    }

}
