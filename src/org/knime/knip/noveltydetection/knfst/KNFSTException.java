package org.knime.knip.noveltydetection.knfst;

@SuppressWarnings("serial")
public class KNFSTException extends Exception {

        public KNFSTException() {
        }

        public KNFSTException(String message) {
                super(message);
        }

        public KNFSTException(Throwable arg0) {
                super(arg0);
        }

        public KNFSTException(String arg0, Throwable arg1) {
                super(arg0, arg1);
        }

        public KNFSTException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
                super(arg0, arg1, arg2, arg3);
        }

}
