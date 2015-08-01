package org.knime.knip.noveltydetection.knfst.alternative;

import org.apache.commons.math3.linear.RealMatrix;

public class NoveltyScores {

        private double[] m_scores;
        private RealMatrix m_coordinates;

        public NoveltyScores(double[] scores, RealMatrix coordinates) {
                m_scores = scores;
                m_coordinates = coordinates;
        }

        public double[] getScores() {
                return m_scores;
        }

        public RealMatrix getCoordinates() {
                return m_coordinates;
        }
}
