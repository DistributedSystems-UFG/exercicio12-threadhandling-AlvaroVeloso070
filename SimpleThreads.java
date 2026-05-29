public class SimpleThreads {

    // Display a message, preceded by the name of the current thread
    static void threadMessage(String message) {
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    }

    private static class CPUIntensiveTask implements Runnable {
        private final long limit;

        CPUIntensiveTask(long limit) {
            this.limit = limit;
        }

        public void run() {
            threadMessage("Iniciando cálculo de primos até " + limit);
            long count = 0;
            for (long n = 2; n <= limit; n++) {
                if (Thread.interrupted()) {
                    threadMessage("Tarefa CPU interrompida! Primos encontrados até agora: " + count);
                    return;
                }
                if (isPrime(n)) {
                    count++;
                }
            }
            threadMessage("Tarefa CPU concluída! Total de primos até " + limit + ": " + count);
        }

        private boolean isPrime(long n) {
            if (n < 2) return false;
            for (long i = 2; i * i <= n; i++) {
                if (n % i == 0) return false;
            }
            return true;
        }
    }

    private static class MessageLoop
        implements Runnable {
        public void run() {
            String importantInfo[] = {
                "Mares eat oats",
                "Does eat oats",
                "Little lambs eat ivy",
                "A kid will eat ivy too"
            };
            try {
                for (int i = 0; i < importantInfo.length; i++) {
                    // Pause for 4 seconds
                    Thread.sleep(4000);
                    // Print a message
                    threadMessage(importantInfo[i]);
                }
            } catch (InterruptedException e) {
                threadMessage("I wasn't done!");
            }
        }
    }

    public static void main(String args[])
        throws InterruptedException {

        // Delay, in milliseconds before we interrupt MessageLoop thread (default one hour)
        long patience = 1000 * 60 * 60;

        // If command line argument present, gives patience in seconds
        if (args.length > 0) {
            try {
                patience = Long.parseLong(args[0]) * 1000;
            } catch (NumberFormatException e) {
                System.err.println("Argument must be an integer.");
                System.exit(1);
            }
        }

        // --- CPU-intensive thread (time limit: 5 seconds) ---
        long cpuPatience = 5000;
        threadMessage("Iniciando thread CPUIntensiveTask");
        Thread cpuThread = new Thread(new CPUIntensiveTask(50_000_000L));
        long cpuStartTime = System.currentTimeMillis();
        cpuThread.start();

        while (cpuThread.isAlive()) {
            threadMessage("Tarefa CPU ainda em execução...");
            cpuThread.join(1000);
            if ((System.currentTimeMillis() - cpuStartTime) > cpuPatience && cpuThread.isAlive()) {
                threadMessage("Tarefa CPU excedeu o tempo limite! Interrompendo...");
                cpuThread.interrupt();
                cpuThread.join();
            }
        }
        threadMessage("Tarefa CPU finalizada.");

        // --- MessageLoop thread ---
        threadMessage("Starting MessageLoop thread");
        long startTime = System.currentTimeMillis();
        Thread t = new Thread(new MessageLoop());

	// Put the MessageLoop thread to run
        t.start();

        threadMessage("Waiting for MessageLoop thread to finish");
	
        // loop until MessageLoop thread exits
        while (t.isAlive()) {
            threadMessage("Still waiting...");
            // Wait maximum of 1 second for MessageLoop thread to finish
            t.join(1000);
            if (((System.currentTimeMillis() - startTime) > patience) && t.isAlive()) {
                threadMessage("Tired of waiting!");
		// Force the interruption of the MainLoop thread
                t.interrupt();
                // ...and wait for it to finish -- shouldn't be long now 
                t.join();
            }
        }
        threadMessage("Finally!");
    }
}
