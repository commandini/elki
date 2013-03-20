package de.lmu.ifi.dbs.elki.math.geodesy;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2013
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.lmu.ifi.dbs.elki.JUnit4Test;

/**
 * Unit test that cross-validates some distance computations with different
 * implementations. Note that it is common to see some difference in these
 * computations, unfortunately - even within R: "sp", "SDMTools" and "geosphere"
 * give different values. But in the end, it's always just an approximation.
 * 
 * @author Erich Schubert
 */
public class TestEarthModels implements JUnit4Test {
  // New York City
  final double[] NEW_YORK = new double[] { 40.67, -73.94 };

  // Munich City
  final double[] MUNICH = new double[] { 48.133333, 11.566667 };

  // Close to Munich city.
  final double[] MUNICH_AIRPORT = new double[] { 48.353889, 11.786111 };

  // Beijing
  final double[] BEIJING = new double[] { 39.913889, 116.391667 };

  // Sydney
  final double[] SYDNEY = new double[] { -33.859972, 151.211111 };

  // South pole
  final double[] SOUTH = new double[] { -90., 123. };

  // North pole
  final double[] NORTH = new double[] { 90., -11. };

  // Null
  final double[] NULL = new double[] { 0., 0. };

  // The full data set
  final double[][] DATA = new double[][] { NEW_YORK, MUNICH, MUNICH_AIRPORT, BEIJING, SYDNEY, SOUTH, NORTH, NULL };

  // Names, for reporting
  final String[] NAMES = new String[] { "New York", "Munich", "Munich Airport", "Beijing", "Sydney", "South Pole", "North Pole", "Null" };

  // Distance matrix, as computed by R "sp" package.
  final double[][] R_SP_WGS84 = new double[][] { //
  { 0.00000000e+00, 6.50366148e+06, 6.50686263e+06, 1.10191059e+07, //
  1.59817953e+07, 1.44857385e+07, 5.49643716e+06, 8.65713703e+06, }, //
  { 6.50366148e+06, 0.00000000e+00, 2.94317181e+04, 7.77130051e+06, //
  1.63129394e+07, 1.53127242e+07, 4.66770838e+06, 5.44172321e+06, }, //
  { 6.50686263e+06, 2.94317181e+04, 0.00000000e+00, 7.74340170e+06, //
  1.62922795e+07, 1.53371802e+07, 4.64319863e+06, 5.46970119e+06, }, //
  { 1.10191059e+07, 7.77130051e+06, 7.74340170e+06, 0.00000000e+00, //
  8.90595105e+06, 1.44020175e+07, 5.58032509e+06, 1.22298940e+07, }, //
  { 1.59817953e+07, 1.63129394e+07, 1.62922795e+07, 8.90595105e+06, //
  0.00000000e+00, 6.25152406e+06, 1.37320772e+07, 1.52087327e+07, }, //
  { 1.44857385e+07, 1.53127242e+07, 1.53371802e+07, 1.44020175e+07, //
  6.25152406e+06, 0.00000000e+00, 1.99703264e+07, 9.99356093e+06, }, //
  { 5.49643716e+06, 4.66770838e+06, 4.64319863e+06, 5.58032509e+06, //
  1.37320772e+07, 1.99703264e+07, 0.00000000e+00, 9.99356093e+06, }, //
  { 8.65713703e+06, 5.44172321e+06, 5.46970119e+06, 1.22298940e+07, //
  1.52087327e+07, 9.99356093e+06, 9.99356093e+06, 0.00000000e+00, }, //
  };

  // Distance matrix, as computed by R "SDMTools" with avoiding segfaults
  final double[][] SDM_WGS84 = new double[][] { //
  { 0.00000000e+00, 6.49607649e+06, 6.49934560e+06, 1.10186899e+07, //
  1.59618991e+07, 1.45058923e+07, 5.49803918e+06, 8.64487707e+06, }, //
  { 6.49607649e+06, 0.00000000e+00, 2.94333400e+04, 7.76413413e+06, //
  1.63019586e+07, 1.53352188e+07, 4.66871262e+06, 5.44849943e+06, }, //
  { 6.49934560e+06, 2.94333400e+04, 0.00000000e+00, 7.73629144e+06, //
  1.62815962e+07, 1.53597436e+07, 4.64418788e+06, 5.47647977e+06, }, //
  { 1.10186899e+07, 7.76413413e+06, 7.73629144e+06, 0.00000000e+00, //
  8.91481946e+06, 1.44219335e+07, 5.58199793e+06, 1.22110932e+07, }, //
  { 1.59618991e+07, 1.63019586e+07, 1.62815962e+07, 8.91481946e+06, //
  0.00000000e+00, 6.25383635e+06, 1.37500951e+07, 1.51936337e+07, }, //
  { 1.45058923e+07, 1.53352188e+07, 1.53597436e+07, 1.44219335e+07, //
  6.25383635e+06, 0.00000000e+00, 2.00039315e+07, 1.00019657e+07, }, //
  { 5.49803918e+06, 4.66871262e+06, 4.64418788e+06, 5.58199793e+06, //
  1.37500951e+07, 2.00039315e+07, 0.00000000e+00, 1.00019657e+07, }, //
  { 8.64487707e+06, 5.44849943e+06, 5.47647977e+06, 1.22110932e+07, //
  1.51936337e+07, 1.00019657e+07, 1.00019657e+07, 0.00000000e+00, }, //
  };

  // Distance matrix, cosine distance in "geosphere" package:
  final double[][] GEOSPHERE_COSINE = new double[][] { //
  { 0.000000000000000e+00, 6.486356338365966e+06, //
  6.489535193201737e+06, 1.099371420315689e+07, //
  1.599344217645813e+07, 1.452986159024009e+07, //
  5.485253480114362e+06, 8.660734833379321e+06, //
  }, //
  { 6.486356338365966e+06, 0.000000000000000e+00, //
  2.941993519886598e+04, 7.750900176348899e+06, //
  1.632910711431917e+07, 1.535974752803661e+07, //
  4.655367542317842e+06, 5.467217714777914e+06, //
  }, //
  { 6.489535193201737e+06, 2.941993519886598e+04, //
  0.000000000000000e+00, 7.723050836613588e+06, //
  1.630864631433774e+07, 1.538427227092249e+07, //
  4.630842799431969e+06, 5.495190660363145e+06, //
  }, //
  { 1.099371420315689e+07, 7.750900176348899e+06, //
  7.723050836613588e+06, 9.493543207645416e-02, //
  8.948985711203361e+06, 1.444578576429031e+07, //
  5.569329306064145e+06, 1.222413440358347e+07, //
  }, //
  { 1.599344217645813e+07, 1.632910711431917e+07, //
  1.630864631433774e+07, 8.948985711203361e+06, //
  0.000000000000000e+00, 6.242495113738451e+06, //
  1.377261995661600e+07, 1.520028800598781e+07, //
  }, //
  { 1.452986159024009e+07, 1.535974752803661e+07, //
  1.538427227092249e+07, 1.444578576429031e+07, //
  6.242495113738451e+06, 0.000000000000000e+00, //
  2.001511507035445e+07, 1.000755753517723e+07, //
  }, //
  { 5.485253480114362e+06, 4.655367542317842e+06, //
  4.630842799431969e+06, 5.569329306064145e+06, //
  1.377261995661600e+07, 2.001511507035445e+07, //
  0.000000000000000e+00, 1.000755753517723e+07, //
  }, //
  { 8.660734833379321e+06, 5.467217714777914e+06, //
  5.495190660363145e+06, 1.222413440358347e+07, //
  1.520028800598781e+07, 1.000755753517723e+07, //
  1.000755753517723e+07, 0.000000000000000e+00, //
  }, //
  };

  // Distance matrix, cosine distance in "geosphere" package:
  final double[][] GEOSPHERE_HAVERSINE = new double[][] { //
  { 0.000000000000000e+00, 6.486356338365965e+06, //
  6.489535193201736e+06, 1.099371420315688e+07, //
  1.599344217645813e+07, 1.452986159024009e+07, //
  5.485253480114363e+06, 8.660734833379321e+06, //
  }, //
  { 6.486356338365965e+06, 0.000000000000000e+00, //
  2.941993519893691e+04, 7.750900176348901e+06, //
  1.632910711431917e+07, 1.535974752803662e+07, //
  4.655367542317841e+06, 5.467217714777914e+06, //
  }, //
  { 6.489535193201736e+06, 2.941993519893691e+04, //
  0.000000000000000e+00, 7.723050836613588e+06, //
  1.630864631433774e+07, 1.538427227092249e+07, //
  4.630842799431968e+06, 5.495190660363145e+06, //
  }, //
  { 1.099371420315688e+07, 7.750900176348901e+06, //
  7.723050836613588e+06, 0.000000000000000e+00, //
  8.948985711203359e+06, 1.444578576429031e+07, //
  5.569329306064145e+06, 1.222413440358347e+07, //
  }, //
  { 1.599344217645813e+07, 1.632910711431917e+07, //
  1.630864631433774e+07, 8.948985711203359e+06, //
  0.000000000000000e+00, 6.242495113738450e+06, //
  1.377261995661600e+07, 1.520028800598781e+07, //
  }, //
  { 1.452986159024009e+07, 1.535974752803662e+07, //
  1.538427227092249e+07, 1.444578576429031e+07, //
  6.242495113738450e+06, 0.000000000000000e+00, //
  2.001511507035445e+07, 1.000755753517723e+07, //
  }, //
  { 5.485253480114363e+06, 4.655367542317841e+06, //
  4.630842799431968e+06, 5.569329306064145e+06, //
  1.377261995661600e+07, 2.001511507035445e+07, //
  0.000000000000000e+00, 1.000755753517723e+07, //
  }, //
  { 8.660734833379321e+06, 5.467217714777914e+06, //
  5.495190660363145e+06, 1.222413440358347e+07, //
  1.520028800598781e+07, 1.000755753517723e+07, //
  1.000755753517723e+07, 0.000000000000000e+00, //
  }, //
  };

  // Distance matrix, cosine distance in "geosphere" package:
  final double[][] GEOSPHERE_VINCENTY_SPHERE = new double[][] { //
  { 0.000000000000000e+00, 6.486356338365965e+06, //
  6.489535193201737e+06, 1.099371420315689e+07, //
  1.599344217645813e+07, 1.452986159024009e+07, //
  5.485253480114362e+06, 8.660734833379321e+06, //
  }, //
  { 6.486356338365966e+06, 0.000000000000000e+00, //
  2.941993519893652e+04, 7.750900176348899e+06, //
  1.632910711431917e+07, 1.535974752803661e+07, //
  4.655367542317842e+06, 5.467217714777914e+06, //
  }, //
  { 6.489535193201737e+06, 2.941993519893681e+04, //
  0.000000000000000e+00, 7.723050836613588e+06, //
  1.630864631433774e+07, 1.538427227092249e+07, //
  4.630842799431969e+06, 5.495190660363145e+06, //
  }, //
  { 1.099371420315689e+07, 7.750900176348899e+06, //
  7.723050836613588e+06, 0.000000000000000e+00, //
  8.948985711203361e+06, 1.444578576429031e+07, //
  5.569329306064145e+06, 1.222413440358347e+07, //
  }, //
  { 1.599344217645813e+07, 1.632910711431917e+07, //
  1.630864631433774e+07, 8.948985711203361e+06, //
  0.000000000000000e+00, 6.242495113738451e+06, //
  1.377261995661600e+07, 1.520028800598781e+07, //
  }, //
  { 1.452986159024009e+07, 1.535974752803661e+07, //
  1.538427227092249e+07, 1.444578576429031e+07, //
  6.242495113738451e+06, 0.000000000000000e+00, //
  2.001511507035445e+07, 1.000755753517723e+07, //
  }, //
  { 5.485253480114362e+06, 4.655367542317842e+06, //
  4.630842799431969e+06, 5.569329306064145e+06, //
  1.377261995661600e+07, 2.001511507035445e+07, //
  0.000000000000000e+00, 1.000755753517723e+07, //
  }, //
  { 8.660734833379321e+06, 5.467217714777914e+06, //
  5.495190660363145e+06, 1.222413440358347e+07, //
  1.520028800598781e+07, 1.000755753517723e+07, //
  1.000755753517723e+07, 0.000000000000000e+00, //
  }, //
  };

  // Distance matrix, cosine distance in "geosphere" package:
  final double[][] GEOSPHERE_VINCENTY_WGS84 = new double[][] { //
  { 0.000000000000000e+00, 6.503767848090770e+06, //
  6.506974602807214e+06, 1.101910369872154e+07, //
  1.599270764104164e+07, 1.450589228275554e+07, //
  5.498039175797141e+06, 8.661075093360968e+06, //
  }, //
  { 6.503767848090770e+06, 0.000000000000000e+00, //
  2.944678565227988e+04, 7.771413604903577e+06, //
  1.632562828361734e+07, 1.533521883690098e+07, //
  4.668712621651703e+06, 5.449076622477260e+06, //
  }, //
  { 6.506974602807214e+06, 2.944678565227961e+04, //
  0.000000000000000e+00, 7.743520548885130e+06, //
  1.630501450760304e+07, 1.535974357438163e+07, //
  4.644187884171052e+06, 5.477072159975932e+06, //
  }, //
  { 1.101910369872154e+07, 7.771413604903577e+06, //
  7.743520548885130e+06, 0.000000000000000e+00, //
  8.918923554119145e+06, 1.442193352759095e+07, //
  5.581997930961725e+06, 1.223306246456788e+07, //
  }, //
  { 1.599270764104165e+07, 1.632562828361734e+07, //
  1.630501450760304e+07, 8.918923554119145e+06, //
  0.000000000000000e+00, 6.253836350029638e+06, //
  1.375009510852304e+07, 1.521100587039189e+07, //
  }, //
  { 1.450589228275554e+07, 1.533521883690098e+07, //
  1.535974357438163e+07, 1.442193352759095e+07, //
  6.253836350029638e+06, 0.000000000000000e+00, //
  2.000393145855268e+07, 1.000196572927634e+07, //
  }, //
  { 5.498039175797141e+06, 4.668712621651703e+06, //
  4.644187884171051e+06, 5.581997930961725e+06, //
  1.375009510852304e+07, 2.000393145855268e+07, //
  0.000000000000000e+00, 1.000196572927634e+07, //
  }, //
  { 8.661075093360968e+06, 5.449076622477260e+06, //
  5.477072159975932e+06, 1.223306246456788e+07, //
  1.521100587039189e+07, 1.000196572927634e+07, //
  1.000196572927634e+07, 0.000000000000000e+00, //
  }, //
  };

  @Test
  public void testWGS84SpheroidEarth() {
    // WGS84 Vincenty to WGS84 Haversine: .2% error on test set.
    testEarthModel(WGS84SpheroidEarthModel.STATIC, R_SP_WGS84, .00168, 0);
    // WGS84 Vincenty to WGS84 Vincenty: seems we only have 7 digits above!
    testEarthModel(WGS84SpheroidEarthModel.STATIC, SDM_WGS84, .001927, 0);
    // WGS84 Vincenty to WGS84 Vincenty: seems we only have 7 digits above!
    testEarthModel(WGS84SpheroidEarthModel.STATIC, GEOSPHERE_VINCENTY_WGS84, 6.1763e-12, 1e-8);
  }

  @Test
  public void testHaversineEarth() {
    // Spherical Haversine to WGS84 Haversine: .5% error on test set.
    testEarthModel(SphericalHaversineEarthModel.STATIC, R_SP_WGS84, .00481, 0);
    // Spherical Haversine to WGS84 Vincenty: .4% error on test set.
    testEarthModel(SphericalHaversineEarthModel.STATIC, SDM_WGS84, .00382, 0);
    // WGS84 Vincenty to WGS84 Vincenty: seems we only have 7 digits above!
    testEarthModel(SphericalHaversineEarthModel.STATIC, GEOSPHERE_HAVERSINE, 6.662e-16, 0);
  }

  @Test
  public void testCosineEarth() {
    // Spherical Cosine to WGS84 Haversine: .5% error on test set.
    testEarthModel(SphericalCosineEarthModel.STATIC, R_SP_WGS84, .00481, 0.1);
    // Spherical Cosine to WGS84 Vincenty: .3% error on test set.
    testEarthModel(SphericalCosineEarthModel.STATIC, SDM_WGS84, .00382, 0.1);
    // Spherical Cosine to Cosine: .3% error on test set.
    testEarthModel(SphericalCosineEarthModel.STATIC, GEOSPHERE_COSINE, 1.042e-11, 0.1);
  }

  @Test
  public void testSphericalEarth() {
    // Spherical Vincenty to WGS84 Haversine: .5% error on test set.
    testEarthModel(SphericalEarthModel.STATIC, R_SP_WGS84, .00481, 0);
    // Spherical Vincenty to WGS84 Vincenty: .3% error on test set.
    testEarthModel(SphericalEarthModel.STATIC, SDM_WGS84, .00382, 0);
    // Spherical Vincenty to Spherical Vincenty: .3% error on test set.
    testEarthModel(SphericalEarthModel.STATIC, GEOSPHERE_VINCENTY_SPHERE, 1.9985e-14, 0);
  }

  protected void testEarthModel(EarthModel model, final double[][] ref, final double relerror, final double abserror) {
    double maxrel = 0.0, maxabs = 0.0;
    for (int i = 0; i < DATA.length; i++) {
      for (int j = i; j < DATA.length; j++) {
        double d = model.distanceDeg(DATA[i][0], DATA[i][1], DATA[j][0], DATA[j][1]);
        double test = (d > 0) ? (ref[i][j] / d) : (ref[i][j] - d + 1.0);
        if (Math.abs(test - 1.0) > relerror) {
          assertEquals("Distances do not agree for " + NAMES[i] + " to " + NAMES[j] + " " + Math.abs(test - 1.0), ref[i][j], d, abserror);
          if (Math.abs(ref[i][j] - d) > maxabs) {
            maxabs = Math.abs(ref[i][j] - d);
          }
        } else {
          if (Math.abs(test - 1.0) > maxrel) {
            maxrel = Math.abs(test - 1.0);
          }
        }
      }
    }
    assertEquals("Relative error bound not tight.", maxrel, relerror, 1e-3 * relerror);
    assertEquals("Absolute error bound not tight.", maxrel, relerror, 1e-3 * relerror);
  }
}
