package org.knime.knip.noveltydetection.nodes.knfstnoveltyscorer;

import java.util.ArrayList;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.knip.noveltydetection.knfst.KNFST;
import org.knime.knip.noveltydetection.knfst.NoveltyScores;

public class KNFSTNoveltyScorerCellFactory extends AbstractCellFactory {

        private KNFST m_model;
        private boolean m_appendNoveltyScore;
        private boolean m_appendNullspaceCoordinates;
        private double m_normalizer;

        public KNFSTNoveltyScorerCellFactory(DataColumnSpec[] newColSpecs, KNFST knfstModel, boolean appendNoveltyScore,
                        boolean appendNullspaceCoordinates, double normalizer) {
                super(newColSpecs);
                m_model = knfstModel;
                m_appendNoveltyScore = appendNoveltyScore;
                m_appendNullspaceCoordinates = appendNullspaceCoordinates;
                m_normalizer = normalizer;
                setParallelProcessing(true);
        }

        @Override
        public DataCell[] getCells(DataRow row) {

                NoveltyScores noveltyScores = m_model.scoreTestData(row);
                double score = noveltyScores.getScores()[0] / m_normalizer;
                double[] nullspaceCoordinates = noveltyScores.getCoordinates().getRow(0);

                ArrayList<DataCell> cells = new ArrayList<DataCell>();
                if (m_appendNoveltyScore) {
                        cells.add(new DoubleCell(score));
                }
                if (m_appendNullspaceCoordinates) {
                        for (double coord : nullspaceCoordinates) {
                                cells.add(new DoubleCell(coord));
                        }
                }

                if (cells.isEmpty()) {
                        return new DataCell[] {};
                }

                return cells.toArray(new DataCell[cells.size()]);

        }

}
