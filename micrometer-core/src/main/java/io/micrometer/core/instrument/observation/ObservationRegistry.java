/*
 * Copyright 2022 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.instrument.observation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.micrometer.core.lang.Nullable;

/**
 * Implementations of this interface are responsible for managing state of an {@link Observation}.
 *
 * @author Jonatan Ivanov
 * @author Tommy Ludwig
 * @author Marcin Grzejszczak
 *
 * @since 2.0.0
 */
public interface ObservationRegistry {

    /**
     * No-op implementation of {@link ObservationRegistry}.
     */
    ObservationRegistry NOOP = new ObservationRegistry() {

        private final ObservationConfig observationConfig = new ObservationConfig() {
            @Override
            public ObservationConfig observationHandler(ObservationHandler<?> handler) {
                return this;
            }

            @Override
            public ObservationConfig observationPredicate(ObservationPredicate predicate) {
                return this;
            }

            @Override
            public ObservationConfig tagsProvider(Observation.GlobalTagsProvider<?> tagsProvider) {
                return this;
            }

            @Override
            public boolean isObservationEnabled(String name, Observation.Context context) {
                return false;
            }

            @Override
            Collection<ObservationHandler<?>> getObservationHandlers() {
                return Collections.emptyList();
            }

            @Override
            Collection<Observation.GlobalTagsProvider<?>> getTagsProviders() {
                return Collections.emptyList();
            }
        };

        @Override
        public Observation getCurrentObservation() {
            return NoopObservation.INSTANCE;
        }

        @Override
        public void setCurrentObservation(Observation current) {

        }

        @Override
        public ObservationConfig observationConfig() {
            return this.observationConfig;
        }
    };

    /**
     * When previously set will allow to retrieve the {@link Observation} at any point in time.
     *
     * Example: if an {@link Observation} was put in {@link Observation.Scope} then this
     * method will return the current present {@link Observation} within the scope.
     *
     * @return current observation or {@code null} if it's not present
     */
    @Nullable
    Observation getCurrentObservation();

    /**
     * Sets the observation as current.
     *
     * @param current observation
     */
    void setCurrentObservation(@Nullable Observation current);

    /**
     * Configuration options for this registry.
     *
     * @return observation configuration
     */
    ObservationConfig observationConfig();


    /**
     * Checks whether this {@link ObservationRegistry} is no-op.
     *
     * @return {@code true} when this is a no-op observation registry
     */
    default boolean isNoOp() {
        return this == NOOP;
    }

    /**
     * Access to configuration options for this registry.
     */
    class ObservationConfig {

        private final List<ObservationHandler<?>> observationHandlers = new CopyOnWriteArrayList<>();

        private final List<ObservationPredicate> observationPredicates = new CopyOnWriteArrayList<>();

        private final List<Observation.GlobalTagsProvider<?>> tagsProviders = new CopyOnWriteArrayList<>();

        /**
         * Register a handler for the {@link Observation observations}.
         *
         * @param handler handler to add to the current configuration
         * @return This configuration instance
         */
        public ObservationConfig observationHandler(ObservationHandler<?> handler) {
            this.observationHandlers.add(handler);
            return this;
        }

        /**
         * Register a predicate to define whether {@link Observation observation} should be created or a
         * {@link NoopObservation} instead.
         *
         * @param predicate predicate
         * @return This configuration instance
         */
        public ObservationConfig observationPredicate(ObservationPredicate predicate) {
            this.observationPredicates.add(predicate);
            return this;
        }

        /**
         * Register a tags provider for the {@link Observation observations}.
         *
         * @param tagsProvider tags provider to add to the current configuration
         * @return This configuration instance
         */
        public ObservationConfig tagsProvider(Observation.GlobalTagsProvider<?> tagsProvider) {
            this.tagsProviders.add(tagsProvider);
            return this;
        }

        /**
         * Check to assert whether {@link Observation} should be created or {@link NoopObservation} instead.
         *
         * @param name observation technical name
         * @param context context
         * @return {@code true} when observation is enabled
         */
        public boolean isObservationEnabled(String name, @Nullable Observation.Context context) {
            return this.observationPredicates.stream().allMatch(predicate -> predicate.test(name, context));
        }

        // package-private for minimal visibility
        Collection<ObservationHandler<?>> getObservationHandlers() {
            return observationHandlers;
        }

        Collection<Observation.GlobalTagsProvider<?>> getTagsProviders() {
            return tagsProviders;
        }
    }
}
