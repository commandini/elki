package de.lmu.ifi.dbs.algorithm;

import de.lmu.ifi.dbs.algorithm.result.KNNJoinResult;
import de.lmu.ifi.dbs.algorithm.result.Result;
import de.lmu.ifi.dbs.data.NumberVector;
import de.lmu.ifi.dbs.database.Database;
import de.lmu.ifi.dbs.database.SpatialIndexDatabase;
import de.lmu.ifi.dbs.distance.Distance;
import de.lmu.ifi.dbs.index.tree.spatial.SpatialDistanceFunction;
import de.lmu.ifi.dbs.index.tree.spatial.SpatialEntry;
import de.lmu.ifi.dbs.index.tree.spatial.SpatialNode;
import de.lmu.ifi.dbs.logging.LogLevel;
import de.lmu.ifi.dbs.logging.ProgressLogRecord;
import de.lmu.ifi.dbs.utilities.Description;
import de.lmu.ifi.dbs.utilities.HyperBoundingBox;
import de.lmu.ifi.dbs.utilities.KNNList;
import de.lmu.ifi.dbs.utilities.Progress;
import de.lmu.ifi.dbs.utilities.QueryResult;
import de.lmu.ifi.dbs.utilities.Util;
import de.lmu.ifi.dbs.utilities.optionhandling.IntParameter;
import de.lmu.ifi.dbs.utilities.optionhandling.constraints.GreaterConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Joins in a given spatial database to each object its k-nearest neighbors.
 * This algorithm only supports spatial databases based on a spatial index
 * structure.
 *
 * @author Elke Achtert (<a href="mailto:achtert@dbs.ifi.lmu.de">achtert@dbs.ifi.lmu.de</a>)
 * @param <V> the type of NumberVector handled by this Algorithm
 * @param <D> the type of Distance used by this Algorithm
 */
public class KNNJoin<V extends NumberVector<V, ?>, D extends Distance<D>, N extends SpatialNode<N, E>, E extends SpatialEntry>
    extends DistanceBasedAlgorithm<V, D> {

    /**
     * Parameter that specifies the k-nearest neighbors to be assigned,
     * must be an integer greater than 0.
     * <p>Default value: {@code 1} </p>
     * <p>Key: {@code -k} </p>
     */
    public final IntParameter K_PARAM =
        new IntParameter("k",
                         "<int>specifies the k-nearest neighbors to be assigned, must be greater than 0",
                         new GreaterConstraint(0),
                         1);

    /**
     * The knn lists for each object.
     */
    private KNNJoinResult<V, D> result;

    /**
     * Adds parameter k to the optionhandler
     * additionally to the parameters provided by super-classes.
     */
    public KNNJoin() {
        super();
        optionHandler.put(K_PARAM);
    }

    /**
     * Runs the algorithm.
     *
     * @param database the database to run the algorithm on
     * @throws IllegalStateException if the algorithm has not been initialized properly (e.g. the
     *                               setParameters(String[]) method has been failed to be called).
     */
    protected void runInTime(Database<V> database) throws IllegalStateException {
        if (!(database instanceof SpatialIndexDatabase)) {
            throw new IllegalStateException(
                "Database must be an instance of "
                + SpatialIndexDatabase.class.getName());
        }
        if (!(getDistanceFunction() instanceof SpatialDistanceFunction)) {
            throw new IllegalStateException(
                "Distance Function must be an instance of "
                + SpatialDistanceFunction.class.getName());
        }
        int k = getParameterValue(K_PARAM);
        SpatialIndexDatabase<V, N, E> db = (SpatialIndexDatabase<V, N, E>) database;
        SpatialDistanceFunction<V, D> distFunction = (SpatialDistanceFunction<V, D>) getDistanceFunction();
        distFunction.setDatabase(db, isVerbose(), isTime());

        HashMap<Integer, KNNList<D>> knnLists = new HashMap<Integer, KNNList<D>>();

        try {
            // data pages of s
            List<E> ps_candidates = db.getLeaves();
            Progress progress = new Progress(this.getClass().getName(), db.size());
            if (this.debug) {
                debugFine("# ps = " + ps_candidates.size());

            }
            // data pages of r
            List<E> pr_candidates = new ArrayList<E>(ps_candidates);
            if (this.debug) {
                debugFine("# pr = " + pr_candidates.size());
            }
            int processed = 0;
            int processedPages = 0;
            boolean up = true;
            for (E pr_entry : pr_candidates) {
                HyperBoundingBox pr_mbr = pr_entry.getMBR();
                N pr = db.getIndex().getNode(pr_entry);
                D pr_knn_distance = distFunction.infiniteDistance();
                if (this.debug) {
                    debugFine(" ------ PR = " + pr);
                }
                // create for each data object a knn list
                for (int j = 0; j < pr.getNumEntries(); j++) {
                    knnLists.put(pr.getEntry(j).getID(), new KNNList<D>(k, getDistanceFunction().infiniteDistance()));
                }

                if (up) {
                    for (E ps_entry : ps_candidates) {
                        HyperBoundingBox ps_mbr = ps_entry.getMBR();
                        D distance = distFunction.distance(pr_mbr, ps_mbr);

                        if (distance.compareTo(pr_knn_distance) <= 0) {
                            N ps = db.getIndex().getNode(ps_entry);
                            pr_knn_distance = processDataPages(pr, ps,
                                                               knnLists, pr_knn_distance);
                        }
                    }
                    up = false;
                }

                else {
                    for (int s = ps_candidates.size() - 1; s >= 0; s--) {
                        E ps_entry = ps_candidates.get(s);
                        HyperBoundingBox ps_mbr = ps_entry.getMBR();
                        D distance = distFunction.distance(pr_mbr, ps_mbr);

                        if (distance.compareTo(pr_knn_distance) <= 0) {
                            N ps = db.getIndex().getNode(ps_entry);
                            pr_knn_distance = processDataPages(pr, ps,
                                                               knnLists, pr_knn_distance);
                        }
                    }
                    up = true;
                }

                processed += pr.getNumEntries();

                if (isVerbose()) {
                    progress.setProcessed(processed);
                    progress(new ProgressLogRecord(LogLevel.PROGRESS, "\r" + progress.toString()
                                                                      + " Number of processed data pages: "
                                                                      + processedPages++,
                                                   progress.getTask(), progress.status()));
                }
            }
            result = new KNNJoinResult<V, D>(knnLists);
        }

        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Processes the two data pages pr and ps and determines the k-nearest
     * neighors of pr in ps.
     *
     * @param pr              the first data page
     * @param ps              the second data page
     * @param knnLists        the knn lists for each data object
     * @param pr_knn_distance the current knn distance of data page pr
     * @return the k-nearest neighbor distance of pr in ps
     */
    private D processDataPages(N pr,
                               N ps,
                               HashMap<Integer, KNNList<D>> knnLists,
                               D pr_knn_distance) {

        // noinspection unchecked
        boolean infinite = getDistanceFunction().isInfiniteDistance(
            pr_knn_distance);
        for (int i = 0; i < pr.getNumEntries(); i++) {
            Integer r_id = pr.getEntry(i).getID();
            KNNList<D> knnList = knnLists.get(r_id);

            for (int j = 0; j < ps.getNumEntries(); j++) {
                Integer s_id = ps.getEntry(j).getID();

                D distance = getDistanceFunction().distance(r_id, s_id);
                if (knnList.add(new QueryResult<D>(s_id, distance))) {
                    // set kNN distance of r
                    if (infinite) {
                        pr_knn_distance = knnList.getMaximumDistance();
                    }
                    pr_knn_distance = Util.max(knnList.getMaximumDistance(),
                                               pr_knn_distance);
                }
            }
        }
        return pr_knn_distance;
    }

    /**
     * Returns the result of the algorithm.
     *
     * @return the result of the algorithm
     */
    public Result<V> getResult() {
        return result;
    }

    /**
     * Returns a description of the algorithm.
     *
     * @return a description of the algorithm
     */
    public Description getDescription() {
        return new Description(
            "KNN-Join",
            "K-Nearest Neighbor Join",
            "Algorithm to find the k-nearest neighbors of each object in a spatial database.",
            "");
    }
}
