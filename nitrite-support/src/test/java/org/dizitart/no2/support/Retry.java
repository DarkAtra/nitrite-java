package org.dizitart.no2.support;

import lombok.extern.slf4j.Slf4j;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class Retry implements TestRule {
    private final int retryCount;

    public Retry(int retryCount) {
        this.retryCount = retryCount;
    }

    public Statement apply(Statement base, Description description) {
        return statement(base, description);
    }

    private Statement statement(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Throwable caughtThrowable = null;

                // implement retry logic here
                for (int i = 0; i < retryCount; i++) {
                    try {
                        base.evaluate();
                        return;
                    } catch (Throwable t) {
                        caughtThrowable = t;
                        log.warn(description.getDisplayName() + ": run " + (i + 1) + " failed");
                    }
                }
                log.error(description.getDisplayName() + ": giving up after " + retryCount + " failures");
                if (caughtThrowable != null) {
                    throw caughtThrowable;
                }
            }
        };
    }
}