package com.firstclub.membership;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;

public class TestRunner {
    public static void main(String[] args) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        DiscoverySelector selector = DiscoverySelectors.selectPackage("com.firstclub.membership");
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selector)
                .build();

        LauncherFactory.create().execute(request, listener);
        listener.getSummary().printTo(new PrintWriter(System.out, true));

        if (listener.getSummary().getTestsFailedCount() > 0) {
            System.exit(1);
        }
    }
}
