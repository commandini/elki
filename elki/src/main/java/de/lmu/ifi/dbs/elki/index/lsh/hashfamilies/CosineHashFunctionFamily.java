package de.lmu.ifi.dbs.elki.index.lsh.hashfamilies;

import java.util.ArrayList;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.database.relation.RelationUtil;
import de.lmu.ifi.dbs.elki.distance.distancefunction.CosineDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.index.lsh.hashfunctions.CosineLocalitySensitiveHashFunction;
import de.lmu.ifi.dbs.elki.index.lsh.hashfunctions.LocalitySensitiveHashFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.randomprojections.RandomHyperplaneProjectionFamily;
import de.lmu.ifi.dbs.elki.math.linearalgebra.randomprojections.RandomProjectionFamily;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.CommonConstraints;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.RandomParameter;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2015
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class CosineHashFunctionFamily implements LocalitySensitiveHashFunctionFamily<NumberVector> {
  /**
   * Projection family to use.
   */
  private RandomProjectionFamily proj;

  /**
   * The number of projections to use for each hash function.
   */
  private int k;

  public CosineHashFunctionFamily(int k, RandomFactory random) {
    super();
    this.proj = new RandomHyperplaneProjectionFamily(random);
    this.k = k;
  }

  @Override
  public TypeInformation getInputTypeRestriction() {
    return TypeUtil.NUMBER_VECTOR_FIELD;
  }

  @Override
  public ArrayList<? extends LocalitySensitiveHashFunction<? super NumberVector>> generateHashFunctions(Relation<? extends NumberVector> relation, int l) {
    int dim = RelationUtil.dimensionality(relation);
    ArrayList<LocalitySensitiveHashFunction<? super NumberVector>> ps = new ArrayList<>(l);
    for(int i = 0; i < l; i++) {
      RandomProjectionFamily.Projection projection = proj.generateProjection(dim, k);
      ps.add(new CosineLocalitySensitiveHashFunction(projection));
    }
    return ps;
  }

  @Override
  public boolean isCompatible(DistanceFunction<?> df) {
    return df instanceof CosineDistanceFunction;
  }
  /**
   * Parameterization class.
   * 
   * 
   * @apiviz.exclude
   */
  public  static class Parameterizer extends AbstractParameterizer {
    /**
     * Parameter for fixing the random seed.
     */
    public static final OptionID RANDOM_ID = new OptionID("lsh.projection.random", "Random seed for generating the projections.");

    /**
     * Number of projections to use in each hash function.
     */
    public static final OptionID NUMPROJ_ID = new OptionID("lsh.projection.projections", "Number of projections to use for each hash function.");

    /**
     * Random generator to use.
     */
    RandomFactory random;

    /**
     * Width of each bin.
     */
    double width;

    /**
     * The number of projections to use for each hash function.
     */
    int k;

    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
      RandomParameter randP = new RandomParameter(RANDOM_ID, RandomFactory.DEFAULT);
      if(config.grab(randP)) {
        random = randP.getValue();
      }

      IntParameter lP = new IntParameter(NUMPROJ_ID);
      lP.addConstraint(CommonConstraints.GREATER_EQUAL_ONE_INT);
      if(config.grab(lP)) {
        k = lP.intValue();
      }
    }
    @Override
    protected CosineHashFunctionFamily makeInstance() {
      return new CosineHashFunctionFamily(k,random);
    }
  }
}