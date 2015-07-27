package org.knime.knip.noveltydetection.nodes.localnoveltyscorer;

import java.util.Comparator;

public class ValueIndexPairComparator implements Comparator<ValueIndexPair> {

        @Override
        public int compare(ValueIndexPair o1, ValueIndexPair o2) {
                // TODO Auto-generated method stub
                return (o1.getValue() > o2.getValue()) ? 1 : ((o1.getValue() == o2.getValue()) ? 0 : -1);
        }

}
