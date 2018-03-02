/*
 * This file is part of Tornado: A heterogeneous programming framework: 
 * https://github.com/beehive-lab/tornado
 *
 * Copyright (c) 2013-2018, APT Group, School of Computer Science,
 * The University of Manchester. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Authors: James Clarkson
 *
 */
package uk.ac.manchester.tornado.benchmarks.rodinia.kmean;

import java.util.Random;

import uk.ac.manchester.tornado.api.Atomic;
import uk.ac.manchester.tornado.api.Parallel;
import uk.ac.manchester.tornado.benchmarks.rodinia.kmean.DataLoader.KmeansData;
import uk.ac.manchester.tornado.runtime.api.TaskSchedule;

public class Kmean {

    private static final boolean USE_RSME = true;
    private static final float threshold = 0.001f;

    private final int clustersMax;
    private final int clustersMin;

    private static final int numberOfLoops = 1;

    private final float[] data;
    private final int numFeatures;
    private final int numPoints;

    // private final int numClusters;
    private final float[] clusters;
    private final float[] newClusters;

    private final int[] clusterSizes;
    private final int[] newClusterSizes;

    private final int[] membership;

    private float rmse;
    private float delta;

    private final TaskSchedule graph;

    public final class IntResult {

        @Atomic
        public int value;

        public IntResult() {
            reset();
        }

        public void reset() {
            value = 0;
        }
    }

    private final IntResult membershipChanges;

    public Kmean(KmeansData input, int minClusters, int maxClusters) {
        data = input.getData();
        numFeatures = input.getNumFeatures();

        numPoints = input.getNumPoints();

        clustersMin = minClusters;
        clustersMax = maxClusters;

        clusters = new float[clustersMax * numFeatures];
        newClusters = new float[clustersMax * numFeatures];

        clusterSizes = new int[clustersMax];
        newClusterSizes = new int[clustersMax];

        membership = new int[numPoints];
        membershipChanges = new IntResult();

        //@formatter:off
        graph = new TaskSchedule("s0")
                .task("t0", Kmean::mapToNearestCluster, data,
                        numPoints, numFeatures, clusters, maxClusters, membership,
                        membershipChanges);
        //@formatter:on
    }

    public void init(int numClusters) {
        Random rand = new Random();
        rand.setSeed(7);

        for (int i = 0; i < numClusters; i++) {
            final int n = rand.nextInt(numPoints);
            for (int j = 0; j < numFeatures; j++) {
                clusters[(i * numFeatures) + j] = data[(n * numFeatures) + j];
                newClusters[(i * numFeatures) + j] = 0f;
            }
            clusterSizes[i] = 0;
            newClusterSizes[i] = 0;
        }

        for (int i = 0; i < numPoints; i++) {
            membership[i] = -1;
        }

    }

    private float calculateRMSE(final int numClusters) {
        float sum = 0.0f;
        final int[] membership = new int[numPoints];
        mapToNearestCluster(data, numPoints, numFeatures, clusters,
                numClusters, membership, membershipChanges);

        for (int i = 0; i < numPoints; i++) {
            final int cluster = membership[i];
            sum += euclidDist(numFeatures, data, i * numFeatures, clusters,
                    cluster * numFeatures);
        }

        return (float) Math.sqrt(sum / numPoints);
    }

    private final static float euclidDist(final int numFeatures,
            final float[] a, int aIndex, final float[] b, int bIndex) {
        float value = 0f;
        for (int i = 0; i < numFeatures; i++) {
            final float dist = a[aIndex + i] - b[bIndex + i];
            value += dist * dist;
        }
        return value;
    }

    public static void mapToNearestCluster(final float[] data,
            final int numPoints, final int numFeatures,
            final float[] clusters, final int numClusters,
            final int[] membership, IntResult result) {

        for (@Parallel int i = 0; i < numPoints; i++) {
            int index = -1;
            float minDist = Float.MAX_VALUE;
            int membershipChanges = 0;

            for (int j = 0; j < numClusters; j++) {
                float dist = 0f;

                final float diff = euclidDist(numFeatures, data, i
                        * numFeatures, clusters, j * numFeatures);
                dist += diff * diff;

                if (dist < minDist) {
                    minDist = dist;
                    index = j;
                }
            }

            if (membership[i] != index) {
                membershipChanges++;
            }

            membership[i] = index;
            result.value = membershipChanges;
        }

    }

    public static void updateClusters(final float[] data, final int numPoints,
            final int numFeatures, final int[] membership,
            final float[] clusters, final int[] clusterSizes) {
        for (@Parallel int i = 0; i < numPoints; i++) {
            final int clusterId = membership[i];
            clusterSizes[clusterId]++;

            for (int j = 0; j < numFeatures; j++) {
                clusters[(clusterId * numFeatures) + j] += data[(i * numFeatures)
                        + j];
            }

        }
    }

    public static void calculateClusters(final int numFeatures,
            final int numClusters, final float[] clusters,
            final int[] clusterSizes, final float[] newClusters,
            final int[] newClusterSizes) {
        for (int i = 0; i < numClusters; i++) {
            for (int j = 0; j < numFeatures; j++) {
                if (newClusterSizes[i] > 0) {
                    clusters[(i * numFeatures) + j] = newClusters[(i * numFeatures)
                            + j]
                            / newClusterSizes[i];
                }
                newClusters[(i * numFeatures) + j] = 0f;
            }
            clusterSizes[i] = newClusterSizes[i];
            newClusterSizes[i] = 0;
        }
    }

    public void kmeans(int numClusters) {

        init(numClusters);

        int loop = 0;
        do {

            membershipChanges.reset();
            mapToNearestCluster(data, numPoints, numFeatures, clusters,
                    numClusters, membership, membershipChanges);
            // graph.schedule().waitOn();

            updateClusters(data, numPoints, numFeatures, membership,
                    newClusters, newClusterSizes);

            calculateClusters(numFeatures, numClusters, clusters, clusterSizes,
                    newClusters, newClusterSizes);

            // assume delta should be percentage of points
            // changing membership this iteration...?
            delta = ((float) membershipChanges.value) / numPoints;
            System.out.printf("loop: id=%d, delta=%f\n", loop, delta);
            loop++;
        } while ((delta > threshold) && (loop < 500));

        System.out.printf("Iterated %d times\n", loop);
    }

    public void printCentres() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clustersMax; i++) {
            sb.append(String.format("cluster: id=%d, ", i));
            for (int j = 0; j < numFeatures; j++) {
                sb.append(String.format("%6.2f ", clusters[(i * numFeatures)
                        + j]));
            }
            sb.append(String.format("\n"));
        }

        System.out.println(sb.toString());
    }

    public void printMembership() {

    }

    public void printSizes() {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < clustersMax; i++) {
            sb.append(String.format("cluster: id=%d, points=%d\n", i,
                    clusterSizes[i]));
        }

        System.out.println(sb.toString());
    }

    public void run() {
        float minRMSERef = Float.MAX_VALUE;

        if (numPoints < clustersMin) {
            System.out
                    .printf("Error: min_nclusters(%d) > npoints (%s) -- cannot proceed\n",
                            clustersMin, numPoints);
            return;
        }

        for (int nclusters = clustersMin; nclusters <= clustersMax; nclusters++) {
            if (nclusters > numPoints) {
                break;
            }

            final long start = System.nanoTime();
            for (int i = 0; i < numberOfLoops; i++) {
                kmeans(nclusters);

                if (USE_RSME) {
                    rmse = calculateRMSE(nclusters);
                    if (rmse < minRMSERef) {
                        minRMSERef = rmse;
                    }
                }
            }
            final long stop = System.nanoTime();
            final double elapsed = (stop - start) * 1e-9;
            System.out
                    .printf("Kmeans: iterations=%d, clusters=%d, time=%.4f, rsme=%.4f\n",
                            numberOfLoops, nclusters, elapsed, rmse);

        }
    }
}