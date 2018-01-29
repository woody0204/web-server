import java.util.*;

/**
 * This class handles the range header sent from the client.
 * @author Xiaoxiong Li, Di Wu
 *
 */
public class RangeHeader {
    public class Range {
        private int startIdx;
        private int endIdx;
        public Range(int start, int end) {
            startIdx = start;
            endIdx = end;
        }
        public int getStart() {
            return startIdx;
        }
        public int getEnd() {
            return endIdx;
        }
    }
    public ArrayList<Range> ranges;
    public RangeHeader(String header) {
        //parse the header to get ranges
    }
    public boolean isValid() {
        //determine if the ranges are valid
        return false;
    }
}
