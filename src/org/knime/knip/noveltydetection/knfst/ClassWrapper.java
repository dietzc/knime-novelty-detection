package org.knime.knip.noveltydetection.knfst;

import java.util.ArrayList;

/*
 * This class is used to determine how many different classes the datasets contains 
 * and how many samples belong to each class
 */
public class ClassWrapper {
        private String name;
        private int count;

        public ClassWrapper(String name, int count) {
                this.name = name;
                this.count = count;
        }

        public String getName() {
                return this.name;
        }

        public int getCount() {
                return this.count;
        }

        /*
         * Returns an array of ClassWrapper that contains the number of samples for each class
         * Parameter: labels: String array containing the labels of the samples (NOTE: Labels MUST be ordered by class)
         * Output: ClassWrapper array that contains the number of samples for each class
         */
        public static ClassWrapper[] classes(String[] labels) {
                ArrayList<ClassWrapper> ret = new ArrayList<ClassWrapper>();
                int count = 0;
                String nameOld = labels[0];
                for (int i = 0; i < labels.length; i++) {
                        String nameNew = labels[i];
                        // check for new class
                        if (!nameOld.equals(nameNew)) {
                                ret.add(new ClassWrapper(nameOld, count));
                                nameOld = nameNew;
                                count = 1;
                        } else {
                                count++;
                        }
                }
                // add last class to list
                ret.add(new ClassWrapper(nameOld, count));

                return ret.toArray(new ClassWrapper[ret.size()]);
        }

        public boolean equals(Object obj) {
                if (obj instanceof ClassWrapper) {
                        ClassWrapper cl = (ClassWrapper) obj;
                        if (this.getName().equals(cl.getName()) && this.getCount() == cl.getCount()) {
                                return true;
                        } else {
                                return false;
                        }
                } else {
                        return false;
                }

        }
}
