/**
 * Surefire Splitter
 * Copyright (C) 2016 drrb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Surefire Splitter. If not, see <http://www.gnu.org/licenses />.
 */
package com.github.drrb.surefiresplitter;

import com.github.drrb.surefiresplitter.spi.AllocationConfigProvider;
import com.github.drrb.surefiresplitter.spi.Provider;
import com.github.drrb.surefiresplitter.spi.ReportRepoProvider;

import java.io.IOException;
import java.util.*;

public class ServiceLookup {

    private final ClassLoader classLoader;

    public ServiceLookup(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public List<AllocationConfigProvider> getAllocationConfigProviders() {
        return lookUp("Surefire Splitter config plugins", AllocationConfigProvider.class, new SystemPropertiesConfigProvider());
    }

    public List<ReportRepoProvider> getReportRepoProviders() {
        return lookUp("Surefire Splitter report history plugins", ReportRepoProvider.class);
    }

    private <T extends Provider> List<T> lookUp(String description, Class<T> providerType, T defaultProvider) {
        return lookUp(description, providerType, Collections.singletonList(defaultProvider));
    }

    private <T extends Provider> List<T> lookUp(String description, Class<T> providerType) {
        return lookUp(description, providerType, Collections.<T>emptyList());
    }

    private <T extends Provider> List<T> lookUp(String description, Class<T> providerType, List<T> defaultProviders) {
        List<T> allProviders = new LinkedList<>();
        allProviders.addAll(defaultProviders);
        List<T> configuredProviders = lookUp(providerType);
        allProviders.addAll(configuredProviders);
        if (allProviders.isEmpty()) {
            System.out.println("Found no " + description + "!");
        } else {
            System.out.println("Found " + description + ":");
            for (T provider : allProviders) {
                System.out.printf(" - %s (%s)%n", provider.getDescription(), provider.isAvailable() ? "enabled" : "disabled");
            }
        }
        return allProviders;

    }

    public <T> List<T> lookUp(Class<T> providerType) {
        try {
            Set<String> serviceNames = ProviderDetector.getServiceNames(providerType, classLoader);
            List<T> services = new ArrayList<>(serviceNames.size());
            for (String serviceName : serviceNames) {
                try {
                    services.add(this.<T>getInstance(serviceName));
                } catch (LoadFailed loadFailed) {
                    loadFailed.printStackTrace();
                }
            }
            return Collections.unmodifiableList(services);
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getInstance(String className) throws LoadFailed {
        try {
            Class<T> type = (Class<T>) Class.forName(className);
            return type.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new LoadFailed(className, e);
        }
    }

    private static class LoadFailed extends Exception {
        public LoadFailed(String serviceType, Throwable cause) {
            super(String.format("Couldn't load service of type %s", serviceType), cause);
        }
    }
}
