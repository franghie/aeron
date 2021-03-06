/*
 * Copyright 2017 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.aeron.cluster.service;

import io.aeron.Aeron;

/**
 * Interface for a {@link ClusteredService} to interact with cluster hosting it.
 */
public interface Cluster
{
    enum Role
    {
        /**
         * The cluster node is a candidate in a cluster election.
         */
        CANDIDATE(0),

        /**
         * The cluster node is the leader of the current leadership term.
         */
        LEADER(1),

        /**
         * The cluster node is a follower of the current leader.
         */
        FOLLOWER(2);

        static final Role[] ROLES;

        static
        {
            final Role[] roles = values();
            ROLES = new Role[roles.length];
            for (final Role role : roles)
            {
                final int code = role.code();
                if (null != ROLES[code])
                {
                    throw new IllegalStateException("Code already in use: " + code);
                }

                ROLES[code] = role;
            }
        }

        private final int code;

        Role(final int code)
        {
            this.code = code;
        }

        /**
         * The code which matches the role in the cluster.
         *
         * @return the code which matches the role in the cluster.
         */
        public final int code()
        {
            return code;
        }

        /**
         * Get the role from a code read from a counter.
         *
         * @param code for the {@link Role}.
         * @return the {@link Role} of the cluster node.
         */
        public static Role get(final int code)
        {
            if (code < 0 || code > (ROLES.length - 1))
            {
                throw new IllegalStateException("Invalid role counter code: " + code);
            }

            return ROLES[code];
        }
    }

    /**
     * The role the cluster node is playing.
     *
     * @return the role the cluster node is playing.
     */
    Role role();

    /**
     * Is the node currently in replay of existing messages.
     *
     * @return true if the node currently in replay of existing messages otherwise false.
     */
    boolean isReplay();

    /**
     * Get the {@link Aeron} client used by the cluster.
     *
     * @return the {@link Aeron} client used by the cluster.
     */
    Aeron aeron();

    /**
     * Get the {@link ClientSession} for a given cluster session id.
     *
     * @param clusterSessionId to be looked up.
     * @return the {@link ClientSession} that matches the clusterSessionId.
     */
    ClientSession getClientSession(long clusterSessionId);

    /**
     * Current Epoch time in milliseconds.
     *
     * @return Epoch time in milliseconds.
     */
    long timeMs();

    /**
     * Schedule a timer for a given deadline and provide a correlation id to identify the timer when it expires.
     * <p>
     * If the correlationId is for an existing scheduled timer then it will be reschedule to the new deadline.
     *
     * @param correlationId to identify the timer when it expires.
     * @param deadlineMs after which the timer will fire.
     */
    void scheduleTimer(long correlationId, long deadlineMs);

    /**
     * Cancel a previous scheduled timer.
     *
     * @param correlationId for the scheduled timer.
     */
    void cancelTimer(long correlationId);
}
