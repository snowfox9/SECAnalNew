package org.snowfox.secAnal;

import org.snowfox.secAnal.runnable.DisposalCollector;

public class RunCrawler {

    // http://www.sec.gov/edgar/searchedgar/companysearch.html

    // CIK, start, count consequently
    public static String getInfoSite = "http://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&CIK=%010d&type=8-k&dateb=&owner=exclude&start=%d&count=%d&output=atom";

    public static void main(String[] args)
    {
        MongoConnector.initiate();

        int NUM_THREADS = ThreadCounter.MAX_THREAD;
        Thread[] threads = new Thread[NUM_THREADS];

        for(int i=0; i<NUM_THREADS; i++)
        {
            Runnable r = new DisposalCollector();
            threads[i] = new Thread(r);
            threads[i].run();

            System.out.println("Thread " + i + " started.");
        }

        while(true)
        {
            try
            {
                if(!threads[ThreadCounter.currentNum].isAlive())
                {
                    System.out.println("Thread # " + ThreadCounter.currentNum + " restarted");

                    Runnable r = new DisposalCollector();
                    Thread t = new Thread(r);
                    threads[ThreadCounter.currentNum] = t;
                    t.start();
                }

                ThreadCounter.add();
            } catch (Exception e)
            {
                e.printStackTrace();
                break;
            }
        }
    }

    public static class ThreadCounter {

        protected static final int MAX_THREAD = 20;
        public static int currentNum = 0;

        public synchronized static void add()
        {
            currentNum++;
            if(currentNum == MAX_THREAD) currentNum = 0;
        }

        public synchronized static void deduct()
        {
            currentNum--;
            if(currentNum == -1) currentNum = MAX_THREAD;
        }

    }

}
