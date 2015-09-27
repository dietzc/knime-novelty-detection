package org.knime.knip.noveltydetection.knfst;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class ApacheCommonsMath {

        public static void main(String[] args) {
                double[][] elements = { {1, 2, 3}, {1, 2, 3}, {1, 2, 3}};
                RealMatrix A = MatrixUtils.createRealMatrix(elements);

                SingularValueDecomposition svd = new SingularValueDecomposition(A);
                Test.printMatrix(svd.getS());
                Test.printMatrix(svd.getV());
                System.out.println(svd.getRank());
        }
}
