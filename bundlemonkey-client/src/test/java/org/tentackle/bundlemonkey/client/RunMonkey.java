/*
 * Test runner.
 */

package org.tentackle.bundlemonkey.client;

import org.testng.annotations.Test;

public class RunMonkey {

  @Test
  public void runMonkey() {
    run(null);
  }

  @Test
  public void runMonkeyDE() {
    run(new String[] {"--locale=de_DE"});
  }


  private synchronized void run(String[] args) {
    @SuppressWarnings("unchecked")
    BundleMonkeyFxClient client = new BundleMonkeyFxClient() {

      @Override
      protected boolean isSystemExitNecessaryToStop() {
        return false;   // no System.exit() by default within the test container
      }

      @Override
      public void stop(int exitValue, Throwable exitThrowable) {
        super.stop(exitValue, exitThrowable);
        if (exitValue != 0) {
          System.exit(exitValue);   // system exit only if error
        }
      }
    };
    client.start(args);
  }

}