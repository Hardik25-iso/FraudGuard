package com.fraudguard;

/**
 * This is a workaround Launcher class for JavaFX 11+.
 * By having a main class that does NOT extend javafx.application.Application,
 * it bypasses the strict Java module checks and allows the application to run
 * seamlessly inside IntelliJ and other IDEs using the classpath dependencies.
 */
public class FraudGuardLauncher {
    
    public static void main(String[] args) {
        // Delegate to the actual Spring Boot + JavaFX application class
        FraudGuardApplication.main(args);
    }
}
