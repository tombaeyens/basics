/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ai.shape.magicless.app.test;

import ai.shape.magicless.app.util.Lists;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SparseMatrixTest {

  public static class Wereld {
    int hoogte;
    int breedte;
    Map<Positie, Integer> overvallenMatrix;

    public Wereld(int hoogte, int breedte) {
      this.hoogte = hoogte;
      this.breedte = breedte;
      this.overvallenMatrix = new HashMap<>();
    }

    public int aantalOvervallenOpPositie(int x, int y) {
      int aantalOvervallen = 0;
      for (Integer overvallenOpPositie: overvallenMatrix.values()) {
        aantalOvervallen = aantalOvervallen + overvallenOpPositie;
      }
      return aantalOvervallen;
    }

    public boolean erIsEenOvervalGebeurdOpPositie(int x, int y) {
      Integer overvallen = overvallenMatrix.get(Lists.of(x,y));
      return overvallen!=null && overvallen>0;
    }

    public int meesteOvervallenInRegio() {
      int regioGrootte = 10;

      Map<Regio,Integer> regios = new HashMap<>();
      for (Positie positie: overvallenMatrix.keySet()) {
        int overvallenOpPositie = overvallenMatrix.get(positie);

        int xMinMin = Math.max(positie.x-regioGrootte+1, 0);
        int xMinMax = Math.min(positie.x+regioGrootte, breedte);
        int yMinMin = Math.max(positie.y-regioGrootte+1, 0);
        int yMinMax = Math.min(positie.y+regioGrootte, hoogte);

        for (int xMin=xMinMin; xMin<xMinMax; xMin++) {
          for (int yMin=yMinMin; yMin<yMinMax; yMin++) {
            Regio regio = new Regio(xMin, yMin, xMin + regioGrootte, yMin + regioGrootte);
            Integer overvallenInRegio = regios.get(regio);
            if (overvallenInRegio==null) {
              overvallenInRegio=0;
            }
            regios.put(regio, overvallenInRegio + overvallenOpPositie);
          }
        }
      }

      int maxOvervallenInRegio = 0;
      for (Integer overvallenInRegio: regios.values()) {
        if (overvallenInRegio>maxOvervallenInRegio) {
          maxOvervallenInRegio = overvallenInRegio;
        }
      }

      return maxOvervallenInRegio;
    }

  }

  public static class Positie {
    int x;
    int y;
    public Positie(int x, int y) {
      this.x = x;
      this.y = y;
    }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Positie positie = (Positie) o;
      return x == positie.x &&
        y == positie.y;
    }
    @Override
    public int hashCode() {
      return Objects.hash(x, y);
    }
  }

  public static class Regio {
    int xMin;
    int yMin;
    int xMax;
    int yMax;
    public Regio(int xMin, int yMin, int xMax, int yMax) {
      this.xMin = xMin;
      this.yMin = yMin;
      this.xMax = xMax;
      this.yMax = yMax;
    }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Regio regio = (Regio) o;
      return xMin == regio.xMin &&
        yMin == regio.yMin &&
        xMax == regio.xMax &&
        yMax == regio.yMax;
    }
    @Override
    public int hashCode() {
      return Objects.hash(xMin, yMin, xMax, yMax);
    }
  }

  @Test
  public void testMatrix() {

  }
}
